/*
 * Copyright 2023 Ant Group Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.secretflow.easypsi.service.impl;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.secretflow.easypsi.common.constant.CacheConstants;
import org.secretflow.easypsi.common.constant.ResourceType;
import org.secretflow.easypsi.common.dto.UserContextDTO;
import org.secretflow.easypsi.common.errorcode.AuthErrorCode;
import org.secretflow.easypsi.common.exception.EasyPsiException;
import org.secretflow.easypsi.common.util.DateTimes;
import org.secretflow.easypsi.common.util.RsaUtils;
import org.secretflow.easypsi.common.util.Sha256Utils;
import org.secretflow.easypsi.common.util.UUIDUtils;
import org.secretflow.easypsi.manager.integration.fabric.FabricManager;
import org.secretflow.easypsi.persistence.entity.*;
import org.secretflow.easypsi.persistence.repository.FabricLogRepository;
import org.secretflow.easypsi.persistence.repository.ProjectJobRepository;
import org.secretflow.easypsi.persistence.repository.UserAccountsRepository;
import org.secretflow.easypsi.persistence.repository.UserTokensRepository;
import org.secretflow.easypsi.service.*;
import org.secretflow.easypsi.service.model.auth.LoginRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * User auth service implementation class
 *
 * @author : xiaonan.fhn
 * @date 2023/5/25
 */
@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final static Logger LOGGER = LoggerFactory.getLogger(AuthServiceImpl.class);

    @Autowired
    private UserService userService;

    @Autowired
    private UserAccountsRepository userAccountsRepository;

    @Autowired
    private UserTokensRepository userTokensRepository;

    @Autowired
    private EnvService envService;

    @Autowired
    private SysResourcesBizService resourcesBizService;

    @Autowired
    private ProjectJobRepository projectJobRepository;

    @Value("${easypsi.account-error-max-attempts:5}")
    private Integer maxAttempts;

    @Value("${easypsi.account-error-lock-time-minutes:30}")
    private Integer lockTimeMinutes;

    @Autowired
    private FabricManager fabricManager;

    @Autowired
    private FabricLogRepository fabricLogRepository;

    @Autowired
    private RsaEncryptionKeyService rsaEncryptionKeyService;

    @Autowired(required = false)
    @Qualifier("fabricThreadPool")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Resource
    private CacheManager cacheManager;

    @Override
    @Transactional(rollbackFor = Exception.class, noRollbackFor = EasyPsiException.class)
    public UserContextDTO login(LoginRequest request) {
        String passwordHash = request.getPasswordHash();
        if (StringUtils.isNotBlank(request.getPublicKey())) {
            RsaEncryptionKeyDO rsaEncryptionKeyDO = rsaEncryptionKeyService.findByPublicKey(request.getPublicKey());
            if (Objects.nonNull(rsaEncryptionKeyDO)) {
                passwordHash = RsaUtils.decrypt(passwordHash, rsaEncryptionKeyDO.getPrivateKey());
            }
        }

        //check password and lock
        AccountsDO user = accountLockedCheck(request.getName(), passwordHash);

        String token = UUIDUtils.newUUID();
        UserContextDTO userContextDTO = new UserContextDTO();
        userContextDTO.setName(user.getName());
        userContextDTO.setOwnerId(user.getOwnerId());
        userContextDTO.setOwnerType(user.getOwnerType());
        userContextDTO.setToken(token);
        userContextDTO.setPlatformType(envService.getPlatformType());
        userContextDTO.setPlatformNodeId(envService.getPlatformNodeId());

        // fill resource codes
        List<ProjectJobDO> jobDOS = projectJobRepository.findByNodeId(user.getOwnerId());
        userContextDTO.setNoviceUser(CollectionUtils.isEmpty(jobDOS));
        // Determine whether the user has modified the password
        userContextDTO.setInitial(user.getInitial());

        Set<String> resourceCodeSet = resourcesBizService.queryResourceCodeByUserName(user.getOwnerType().toPermissionUserType(), ResourceType.INTERFACE, user.getName());
        userContextDTO.setInterfaceResources(resourceCodeSet);

        userTokensRepository.deleteByName(user.getName());

        TokensDO tokensDO = TokensDO.builder()
                .name(user.getName())
                .token(token)
                .gmtToken(LocalDateTime.now())
                .sessionData(userContextDTO.toJsonStr())
                .build();
        userTokensRepository.saveAndFlush(tokensDO);
        return userContextDTO;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void logout(String name, String token) {
        userTokensRepository.deleteByNameAndToken(name, token);
        threadPoolTaskExecutor.execute(() -> {
            if (fabricManager.isOpen()) {
                FabricLogDO fabricLogDO = new FabricLogDO();
                try {
                    //start truncate log
                    truncateLog(fabricLogDO);
                    //upload to chain
                    if (Objects.nonNull(fabricLogDO) && StringUtils.isNotBlank(fabricLogDO.getLogHash())) {
                        fabricManager.submitTransaction(fabricLogDO);
                        fabricLogDO.setResult(1);
                        fabricLogDO.setMessage("success");
                    }
                } catch (Exception exception) {
                    fabricLogDO.setResult(2);
                    fabricLogDO.setMessage(exception.getMessage().length() > 500 ? exception.getMessage().substring(0, 500) : exception.getMessage());
                    log.error("truncate log to fabric  error,{}", exception.getMessage());
                }
                fabricLogRepository.save(fabricLogDO);
            }
        });
    }


    /**
     * Truncate log and build fabric params
     *
     * @param fabricLogDO
     */

    private static void truncateLog(FabricLogDO fabricLogDO) {
        // get root logger
        LoggerContext loggerContext = (LoggerContext) org.slf4j.LoggerFactory.getILoggerFactory();
        ch.qos.logback.classic.Logger rootLogger = loggerContext.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        //get current appender
        RollingFileAppender<ILoggingEvent> rollingAppender = (RollingFileAppender<ILoggingEvent>) rootLogger.getAppender("ROLLING_FILE");
        //get current file
        File oldFile = new File(rollingAppender.getFile());
        if (Objects.isNull(oldFile)) {
            return;
        }
        //get policy rule
        TimeBasedRollingPolicy<ILoggingEvent> policy = (TimeBasedRollingPolicy<ILoggingEvent>) rollingAppender.getRollingPolicy();
        //get encoder rule
        PatternLayoutEncoder encoder = (PatternLayoutEncoder) rollingAppender.getEncoder();
        //get appender name
        String appenderName = rollingAppender.getName();
        //stop current log and delete appender
        rollingAppender.stop();
        rootLogger.detachAppender(rollingAppender);
        String oldFileName = oldFile.getName();

        int dotIndex = oldFileName.lastIndexOf('.');
        String baseName = (dotIndex != -1) ? oldFileName.substring(0, dotIndex) : oldFileName;
        String extension = (dotIndex != -1) ? oldFileName.substring(dotIndex) : StringUtils.EMPTY;
        String fileName = baseName + "-" + DateTimes.localDateTimeString(LocalDateTime.now(), DateTimes.LOCAL_DATE_TIME_FORMATTER2) + extension;
        File newFile = new File(oldFile.getParent(), fileName);
        if (oldFile.renameTo(newFile)) {
            RollingFileAppender<ILoggingEvent> rollingFileAppender = new RollingFileAppender<>();
            rollingFileAppender.setName(appenderName);
            policy.start();
            rollingFileAppender.setRollingPolicy(policy);
            encoder.start();
            rollingFileAppender.setEncoder(encoder);
            rollingFileAppender.setContext(loggerContext);
            rootLogger.addAppender(rollingFileAppender);
            rollingFileAppender.start();
            fabricLogDO.setLogPath(newFile.getPath());
            fabricLogDO.setLogHash(Sha256Utils.hash(newFile.getPath()));
        }
    }

    /**
     * account lock check
     *
     * @param userName
     */
    private AccountsDO accountLockedCheck(String userName, String passwordHash) {
        LocalDateTime currentTime = LocalDateTime.now();
        //current user is need lock
        AccountsDO user = userService.queryUserByName(userName);
        if (ObjectUtils.isEmpty(user)) {
            Cache cache = cacheManager.getCache(CacheConstants.USER_LOCK_CACHE);
            HashMap<String, Integer> lockInfo = cache.get(userName, HashMap.class);
            int failedAttempts = 0;
            if (lockInfo != null) {
                failedAttempts = lockInfo.get("failedAttempts");
                if (failedAttempts >= maxAttempts) {
                    throw EasyPsiException.of(AuthErrorCode.USER_IS_LOCKED, String.valueOf(lockTimeMinutes));
                }
            } else {
                lockInfo = new HashMap<>();
            }
            lockInfo.put("failedAttempts", ++failedAttempts);
            cache.put(userName, lockInfo);
            throw EasyPsiException.of(AuthErrorCode.USER_PASSWORD_ERROR, String.valueOf(maxAttempts - --failedAttempts));
        }
        //checkPassword success
        if (user.getPasswordHash().equals(passwordHash)) {
            //lock invalid
            user.setLockedInvalidTime(null);
            user.setFailedAttempts(null);
            userAccountsRepository.save(user);
            return user;
        }

        user.setFailedAttempts(Objects.isNull(user.getFailedAttempts()) ? 1 : user.getFailedAttempts() + 1);
        if (user.getFailedAttempts() >= maxAttempts) {
            user.setLockedInvalidTime(currentTime.plusMinutes(lockTimeMinutes));
            userService.userLock(user);
            throw EasyPsiException.of(AuthErrorCode.USER_IS_LOCKED, String.valueOf(lockTimeMinutes));
        }
        userService.userLock(user);
        throw EasyPsiException.of(AuthErrorCode.USER_PASSWORD_ERROR, String.valueOf(maxAttempts - user.getFailedAttempts()));
    }

}

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

package org.secretflow.secretpad.service.impl;

import org.secretflow.secretpad.common.constant.ResourceType;
import org.secretflow.secretpad.common.dto.UserContextDTO;
import org.secretflow.secretpad.common.errorcode.AuthErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.common.util.DateTimes;
import org.secretflow.secretpad.common.util.Sha256Utils;
import org.secretflow.secretpad.common.util.UUIDUtils;
import org.secretflow.secretpad.manager.integration.fabric.FabricManager;
import org.secretflow.secretpad.persistence.entity.AccountsDO;
import org.secretflow.secretpad.persistence.entity.FabricLogDO;
import org.secretflow.secretpad.persistence.entity.ProjectJobDO;
import org.secretflow.secretpad.persistence.entity.TokensDO;
import org.secretflow.secretpad.persistence.repository.FabricLogRepository;
import org.secretflow.secretpad.persistence.repository.ProjectJobRepository;
import org.secretflow.secretpad.persistence.repository.UserAccountsRepository;
import org.secretflow.secretpad.persistence.repository.UserTokensRepository;
import org.secretflow.secretpad.service.AuthService;
import org.secretflow.secretpad.service.EnvService;
import org.secretflow.secretpad.service.SysResourcesBizService;
import org.secretflow.secretpad.service.UserService;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
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

    @Value("${secretpad.account-error-max-attempts:5}")
    private Integer maxAttempts;

    @Value("${secretpad.account-error-lock-time-minutes:30}")
    private Integer lockTimeMinutes;

    @Autowired
    private FabricManager fabricManager;

    @Autowired
    private FabricLogRepository fabricLogRepository;

    @Autowired(required = false)
    @Qualifier("fabricThreadPool")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Override
    @Transactional(rollbackFor = Exception.class, noRollbackFor = SecretpadException.class)
    public UserContextDTO login(String name, String passwordHash) {
        //check password and lock
        AccountsDO user = accountLockedCheck(name, passwordHash);

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
        AccountsDO lockedUser = userService.findLockedUser();
        //check all user locked
        if (Objects.nonNull(lockedUser)) {
            Duration duration = Duration.between(currentTime, lockedUser.getLockedInvalidTime());
            Long minutes = duration.toMinutes();
            if (minutes <= 0) {
                //lock invalid
                lockedUser.setGmtModified(LocalDateTime.now());
                lockedUser.setLockedInvalidTime(null);
                lockedUser.setFailedAttempts(null);
                userAccountsRepository.save(lockedUser);
            } else {
                throw SecretpadException.of(AuthErrorCode.USER_IS_LOCKED, String.valueOf(minutes));
            }
        }

        //current user is need lock
        AccountsDO user = userService.getUserByName(userName);
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
            throw SecretpadException.of(AuthErrorCode.USER_IS_LOCKED, String.valueOf(lockTimeMinutes));
        }
        userService.userLock(user);
        throw SecretpadException.of(AuthErrorCode.USER_PASSWORD_ERROR, String.valueOf(maxAttempts - user.getFailedAttempts()));
    }

}

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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.secretflow.easypsi.common.errorcode.AuthErrorCode;
import org.secretflow.easypsi.common.errorcode.UserErrorCode;
import org.secretflow.easypsi.common.exception.EasyPsiException;
import org.secretflow.easypsi.common.util.RsaUtils;
import org.secretflow.easypsi.persistence.entity.AccountsDO;
import org.secretflow.easypsi.persistence.entity.RsaEncryptionKeyDO;
import org.secretflow.easypsi.persistence.repository.UserAccountsRepository;
import org.secretflow.easypsi.persistence.repository.UserTokensRepository;
import org.secretflow.easypsi.service.RsaEncryptionKeyService;
import org.secretflow.easypsi.service.UserService;
import org.secretflow.easypsi.service.model.auth.UserUpdatePwdRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * User service implementation class
 *
 * @author lihaixin
 * @date 2023/12/14
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserAccountsRepository userAccountsRepository;

    @Autowired
    private UserTokensRepository userTokensRepository;

    @Value("${easypsi.reset-password-error-max-attempts:5}")
    private Integer resetPasswordMaxAttempts;

    @Value("${easypsi.reset-password-error-lock-time-minutes:60}")
    private Integer resetPasswordLockMinutes;

    @Autowired
    private RsaEncryptionKeyService rsaEncryptionKeyService;

    @Override
    @Transactional(rollbackFor = Exception.class, noRollbackFor = EasyPsiException.class)
    public void updatePwd(UserUpdatePwdRequest request) {
        if (StringUtils.isNotBlank(request.getPublicKey())) {
            RsaEncryptionKeyDO rsaEncryptionKeyDO = rsaEncryptionKeyService.findByPublicKey(request.getPublicKey());
            if (Objects.nonNull(rsaEncryptionKeyDO)) {
                request.setOldPasswordHash(RsaUtils.decrypt(request.getOldPasswordHash(), rsaEncryptionKeyDO.getPrivateKey()));
                request.setNewPasswordHash(RsaUtils.decrypt(request.getNewPasswordHash(), rsaEncryptionKeyDO.getPrivateKey()));
                request.setConfirmPasswordHash(RsaUtils.decrypt(request.getConfirmPasswordHash(), rsaEncryptionKeyDO.getPrivateKey()));
            }
        }

        AccountsDO userDO = getUserByName(request.getName());
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime gmtPasswdResetRelease = userDO.getGmtPasswdResetRelease();
        Boolean isUnlock = Boolean.FALSE;

        if (Objects.nonNull(gmtPasswdResetRelease)) {
            Duration duration = Duration.between(currentTime, gmtPasswdResetRelease);
            if (duration.toMinutes() > 0) {
                if (userDO.getPasswdResetFailedAttempts() >= resetPasswordMaxAttempts) {
                    Long minutes = duration.toMinutes();
                    throw EasyPsiException.of(AuthErrorCode.USER_IS_LOCKED, String.valueOf(minutes));
                }
            } else {
                isUnlock = Boolean.TRUE;
                //release reset password lock
                userDO.setGmtPasswdResetRelease(null);
                userDO.setPasswdResetFailedAttempts(null);
                userDO.setGmtModified(LocalDateTime.now());
                userAccountsRepository.save(userDO);
            }
        }
        if (!StringUtils.equals(request.getNewPasswordHash(), request.getConfirmPasswordHash())) {
            throw EasyPsiException.of(UserErrorCode.USER_UPDATE_PASSWORD_ERROR_INCONSISTENT);
        }

        if (StringUtils.equals(request.getOldPasswordHash(), request.getNewPasswordHash())) {
            throw EasyPsiException.of(UserErrorCode.USER_UPDATE_PASSWORD_ERROR_SAME);
        }
        //checkPassword success
        if (userDO.getPasswordHash().equals(request.getOldPasswordHash())) {
            //lock invalid
            if (!isUnlock) {
                userDO.setGmtPasswdResetRelease(null);
                userDO.setPasswdResetFailedAttempts(null);
                userDO.setGmtModified(LocalDateTime.now());
                userDO.setPasswordHash(request.getNewPasswordHash());
                userDO.setInitial(false);
                userAccountsRepository.save(userDO);
                //after remove need remove user all token
                userTokensRepository.deleteByName(request.getName());
            }
            return;
        }

        userDO.setPasswdResetFailedAttempts(Objects.isNull(userDO.getPasswdResetFailedAttempts()) ? 1 : userDO.getPasswdResetFailedAttempts() + 1);
        if (userDO.getPasswdResetFailedAttempts() >= resetPasswordMaxAttempts) {
            userDO.setGmtPasswdResetRelease(currentTime.plusMinutes(resetPasswordLockMinutes));
            userLock(userDO);
            throw EasyPsiException.of(AuthErrorCode.RESET_PASSWORD_IS_LOCKED, String.valueOf(resetPasswordLockMinutes));
        }
        userLock(userDO);
        throw EasyPsiException.of(AuthErrorCode.USER_PASSWORD_ERROR, String.valueOf(resetPasswordMaxAttempts - userDO.getPasswdResetFailedAttempts()));
    }


    @Override
    public AccountsDO getUserByName(String userName) {
        Optional<AccountsDO> userOptional = userAccountsRepository.findByName(userName);
        if (userOptional.isEmpty()) {
            throw EasyPsiException.of(AuthErrorCode.USER_NOT_FOUND);
        }
        return userOptional.get();
    }

    @Override
    public AccountsDO queryUserByName(String userName) {
        Optional<AccountsDO> userOptional = userAccountsRepository.findByName(userName);
        return userOptional.orElse(null);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void userLock(AccountsDO accountsDO) {
        accountsDO.setGmtModified(LocalDateTime.now());
        userAccountsRepository.save(accountsDO);
    }
}

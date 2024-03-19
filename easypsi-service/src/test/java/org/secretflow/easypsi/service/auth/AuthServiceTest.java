/*
 * Copyright 2024 Ant Group Co., Ltd.
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

package org.secretflow.easypsi.service.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.secretflow.easypsi.common.dto.UserContextDTO;
import org.secretflow.easypsi.common.util.RsaUtils;
import org.secretflow.easypsi.common.util.Sha256Utils;
import org.secretflow.easypsi.common.util.UUIDUtils;
import org.secretflow.easypsi.manager.integration.fabric.FabricManager;
import org.secretflow.easypsi.persistence.entity.AccountsDO;
import org.secretflow.easypsi.persistence.entity.RsaEncryptionKeyDO;
import org.secretflow.easypsi.persistence.entity.TokensDO;
import org.secretflow.easypsi.persistence.repository.FabricLogRepository;
import org.secretflow.easypsi.persistence.repository.ProjectJobRepository;
import org.secretflow.easypsi.persistence.repository.UserAccountsRepository;
import org.secretflow.easypsi.persistence.repository.UserTokensRepository;
import org.secretflow.easypsi.service.*;
import org.secretflow.easypsi.service.model.auth.LoginRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Auth Service Test
 *
 * @author lihaixin
 * @date 2024/03/11
 */
@SpringBootTest(classes = {AuthService.class, UserService.class, UserTokensRepository.class})
public class AuthServiceTest {

    @MockBean
    private UserService userService;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserAccountsRepository userAccountsRepository;

    @MockBean
    private UserTokensRepository userTokensRepository;

    @MockBean
    private EnvService envService;

    @MockBean
    private SysResourcesBizService resourcesBizService;

    @MockBean
    private ProjectJobRepository projectJobRepository;

    @Value("${easypsi.account-error-max-attempts:5}")
    private Integer maxAttempts;

    @Value("${easypsi.account-error-lock-time-minutes:30}")
    private Integer lockTimeMinutes;

    @MockBean
    private FabricManager fabricManager;

    @MockBean
    private FabricLogRepository fabricLogRepository;

    @MockBean
    private RsaEncryptionKeyService rsaEncryptionKeyService;

    @MockBean
    @Qualifier("fabricThreadPool")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;


    public static String userName = "admin";
    public static String password = "admin";

    public static String rsaPublicKey;
    public static String rsaPrivateKey;

    @BeforeEach
    public void setUp() {
        Map<String, String> keyMap = RsaUtils.generateRSAKeys();
        rsaPublicKey = keyMap.get(RsaUtils.PUBLIC_KEY_NAME);
        rsaPrivateKey = keyMap.get(RsaUtils.PRIVATE_KEY_NAME);
        password = Sha256Utils.hash(password);
    }

    @Test
    public void testLoginSuccess() {
        //not rsa
        LoginRequest request = new LoginRequest();
        request.setName(userName);
        //rsa password
        request.setPasswordHash(RsaUtils.encrypt(password, rsaPublicKey));
        Mockito.when(userService.getUserByName(request.getName())).thenReturn(buildAccountsDO());
        Mockito.when(rsaEncryptionKeyService.findByPublicKey(request.getPublicKey())).thenReturn(buildRsaEncryptionKeyDO());

        //success
        authService.login(request);


        //fail
        LoginRequest failRequest = new LoginRequest();
        authService.login(failRequest);
    }

    private AccountsDO buildAccountsDO() {
        AccountsDO accountsDO = new AccountsDO();
        accountsDO.setName(userName);
        accountsDO.setPasswordHash(password);
        return accountsDO;
    }

    private RsaEncryptionKeyDO buildRsaEncryptionKeyDO() {
        RsaEncryptionKeyDO rsaEncryptionKeyDO = new RsaEncryptionKeyDO();
        rsaEncryptionKeyDO.setPublicKey(rsaPublicKey);
        rsaEncryptionKeyDO.setPrivateKey(rsaPrivateKey);
        return rsaEncryptionKeyDO;
    }

}
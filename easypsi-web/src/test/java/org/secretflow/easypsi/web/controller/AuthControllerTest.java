/*
 *   Copyright 2023 Ant Group Co., Ltd.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package org.secretflow.easypsi.web.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.secretflow.easypsi.common.dto.UserContextDTO;
import org.secretflow.easypsi.common.util.JsonUtils;
import org.secretflow.easypsi.common.util.RsaUtils;
import org.secretflow.easypsi.common.util.Sha256Utils;
import org.secretflow.easypsi.common.util.UserContext;
import org.secretflow.easypsi.persistence.entity.AccountsDO;
import org.secretflow.easypsi.persistence.entity.ProjectJobDO;
import org.secretflow.easypsi.persistence.entity.RsaEncryptionKeyDO;
import org.secretflow.easypsi.persistence.repository.ProjectJobRepository;
import org.secretflow.easypsi.persistence.repository.UserAccountsRepository;
import org.secretflow.easypsi.persistence.repository.UserTokensRepository;
import org.secretflow.easypsi.service.RsaEncryptionKeyService;
import org.secretflow.easypsi.service.model.auth.LoginRequest;
import org.secretflow.easypsi.web.constant.AuthConstants;
import org.secretflow.easypsi.web.utils.FakerUtils;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

/**
 * Authorization controller test
 *
 * @author cml
 * @date 2023/07/27
 * @since 4.3
 */
class AuthControllerTest extends ControllerTest {

    @MockBean
    private UserAccountsRepository userAccountsRepository;

    @MockBean
    private UserTokensRepository userTokensRepository;

    @MockBean
    private ProjectJobRepository projectJobRepository;

    @MockBean
    private RsaEncryptionKeyService rsaEncryptionKeyService;


    @Test
    void login() throws Exception {
        assertResponse(() -> {
            Map<String, String> keyMap = RsaUtils.generateRSAKeys();
            String rsaPublicKey = keyMap.get(RsaUtils.PUBLIC_KEY_NAME);
            String rsaPrivateKey = keyMap.get(RsaUtils.PRIVATE_KEY_NAME);
            String passwordHash = Sha256Utils.hash("admin");
            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setName("admin");
            //rsa password
            loginRequest.setPasswordHash(RsaUtils.encrypt(passwordHash, rsaPublicKey));
            loginRequest.setPublicKey(rsaPublicKey);
            RsaEncryptionKeyDO rsaEncryptionKeyDO = new RsaEncryptionKeyDO();
            rsaEncryptionKeyDO.setPublicKey(rsaPublicKey);
            rsaEncryptionKeyDO.setPrivateKey(rsaPrivateKey);
            AccountsDO accountsDO = FakerUtils.fake(AccountsDO.class);
            accountsDO.setName(loginRequest.getName());
            accountsDO.setPasswordHash(passwordHash);
            Mockito.when(rsaEncryptionKeyService.findByPublicKey(loginRequest.getPublicKey())).thenReturn(rsaEncryptionKeyDO);
            when(userAccountsRepository.findByName(loginRequest.getName())).thenReturn(Optional.of(accountsDO));
            Mockito.when(projectJobRepository.findByNodeId(Mockito.anyString())).thenReturn(buildProjectJobDOs());
            return MockMvcRequestBuilders.post(getMappingUrl(AuthController.class, "login", HttpServletResponse.class, LoginRequest.class))
                    .content(JsonUtils.toJSONString(loginRequest));
        });
    }

    private List<ProjectJobDO> buildProjectJobDOs() {
        List<ProjectJobDO> jobDOS = new ArrayList<>();
        ProjectJobDO jobDO = new ProjectJobDO();
        jobDOS.add(jobDO);
        return jobDOS;
    }

    @Test
    void logout() throws Exception {
        assertResponse(() -> {
            doNothing().when(userTokensRepository).deleteByNameAndToken(Mockito.anyString(), Mockito.anyString());
            return MockMvcRequestBuilders.post(getMappingUrl(AuthController.class, "logout", HttpServletRequest.class))
                    .header(AuthConstants.TOKEN_NAME, "tokens").content(JsonUtils.toJSONString("token"));
        });
    }

    @Test
    void get() throws Exception {
        assertResponse(() -> {
            UserContext.setBaseUser(FakerUtils.fake(UserContextDTO.class));
            UserContext.getUser();
            return MockMvcRequestBuilders.post(getMappingUrl(AuthController.class, "get"));
        });
    }
}
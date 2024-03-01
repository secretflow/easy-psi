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

package org.secretflow.secretpad.web.controller;

import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.persistence.entity.AccountsDO;
import org.secretflow.secretpad.persistence.entity.ProjectJobDO;
import org.secretflow.secretpad.persistence.repository.ProjectJobRepository;
import org.secretflow.secretpad.persistence.repository.UserAccountsRepository;
import org.secretflow.secretpad.persistence.repository.UserTokensRepository;
import org.secretflow.secretpad.service.model.auth.LoginRequest;
import org.secretflow.secretpad.web.constant.AuthConstants;
import org.secretflow.secretpad.web.utils.FakerUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.List;
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

    @Test
    void login() throws Exception {
        assertResponse(() -> {
            LoginRequest loginRequest = FakerUtils.fake(LoginRequest.class);
            AccountsDO accountsDO = FakerUtils.fake(AccountsDO.class);
            accountsDO.setPasswordHash(loginRequest.getPasswordHash());
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
                    .header(AuthConstants.TOKEN_NAME,"tokens").content(JsonUtils.toJSONString("token"));
        });
    }
}
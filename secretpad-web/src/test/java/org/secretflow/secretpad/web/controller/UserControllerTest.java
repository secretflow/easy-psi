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
import org.secretflow.secretpad.persistence.repository.UserAccountsRepository;
import org.secretflow.secretpad.persistence.repository.UserTokensRepository;
import org.secretflow.secretpad.service.model.auth.UserUpdatePwdRequest;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Optional;

import static org.mockito.Mockito.doNothing;

/**
 * User controller test
 *
 * @author lihaixin
 * @date 2023/12/14
 */
class UserControllerTest extends ControllerTest {
    @MockBean
    private UserAccountsRepository userAccountsRepository;

    @MockBean
    private UserTokensRepository userTokensRepository;


    @Test
    void updatePwd() throws Exception {
        assertResponse(() -> {
            UserUpdatePwdRequest userUpdatePwdRequest = new UserUpdatePwdRequest();
            userUpdatePwdRequest.setName("user");
            userUpdatePwdRequest.setOldPasswordHash("03f961ad4bbfe252460b9f20de1e860322f72d6657266ed15e7e690a8fb3a2a3");
            userUpdatePwdRequest.setNewPasswordHash("8762b128946472920aa9c98d0d305a5101fd1958fbe3f34ec544adde24a6f983");
            userUpdatePwdRequest.setConfirmPasswordHash("8762b128946472920aa9c98d0d305a5101fd1958fbe3f34ec544adde24a6f983");
            Mockito.when(userAccountsRepository.findByName(Mockito.anyString())).thenReturn(accountsDO());
            Mockito.when(userAccountsRepository.save(Mockito.any())).thenReturn(accountsDO().get());
            doNothing().when(userTokensRepository).deleteByName(accountsDO().get().getName());
            return MockMvcRequestBuilders.post(getMappingUrl(UserController.class, "updatePwd", UserUpdatePwdRequest.class))
                    .content(JsonUtils.toJSONString(userUpdatePwdRequest));
        });

    }

    private Optional<AccountsDO> accountsDO() {
        return Optional.of(AccountsDO.builder().name("admin").passwordHash("03f961ad4bbfe252460b9f20de1e860322f72d6657266ed15e7e690a8fb3a2a3").build());
    }

}
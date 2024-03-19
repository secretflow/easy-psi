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

package org.secretflow.easypsi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.secretflow.easypsi.common.constant.PlatformType;
import org.secretflow.easypsi.common.constant.UserOwnerType;
import org.secretflow.easypsi.common.dto.UserContextDTO;
import org.secretflow.easypsi.common.util.UserContext;
import org.secretflow.easypsi.service.auth.impl.DefaultInterfaceResourceAuth;
import org.secretflow.easypsi.service.impl.EnvServiceImpl;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

/**
 * Interface Resource Auth Test
 *
 * @author lihaixin
 * @date 2024/03/11
 */
@SpringBootTest(classes = {DefaultInterfaceResourceAuth.class, EnvServiceImpl.class})
public class DefaultInterfaceResourceAuthTest {


    @MockBean
    private EnvService envService;

    @MockBean
    private DefaultInterfaceResourceAuth defaultInterfaceResourceAuth;


    @BeforeEach
    void setUp() {
        UserContextDTO userContextDTO = new UserContextDTO();
        userContextDTO.setName("admin");
        userContextDTO.setOwnerId("kuscia-system");
        userContextDTO.setOwnerType(UserOwnerType.CENTER);
        userContextDTO.setToken("token");
        userContextDTO.setPlatformType(PlatformType.P2P);
        UserContext.setBaseUser(userContextDTO);
    }

    @Test
    void testCheckForPlatformManager() {
        Mockito.when(envService.getPlatformNodeId()).thenReturn("platformManagerId");
        defaultInterfaceResourceAuth.check("anyResourceCode");
    }


}
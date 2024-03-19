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

package org.secretflow.easypsi.web.controller;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import org.mockito.Mockito;
import org.secretflow.easypsi.common.errorcode.NodeErrorCode;
import org.secretflow.easypsi.common.errorcode.SystemErrorCode;
import org.secretflow.easypsi.common.exception.EasyPsiException;
import org.secretflow.easypsi.common.util.JsonUtils;
import org.secretflow.easypsi.common.util.Sha256Utils;
import org.secretflow.easypsi.manager.integration.fabric.FabricManager;
import org.secretflow.easypsi.persistence.entity.FabricLogDO;
import org.secretflow.easypsi.persistence.entity.RsaEncryptionKeyDO;
import org.secretflow.easypsi.persistence.repository.FabricLogRepository;
import org.secretflow.easypsi.service.model.auth.UserUpdatePwdRequest;
import org.secretflow.easypsi.service.model.fabric.FabricLogRequest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Optional;

/**
 * FabricLogControllerTest
 *
 * @author lihaixin
 * @date 2024/02/23
 */
public class FabricLogControllerTest extends ControllerTest {


    @MockBean
    private FabricLogRepository fabricLogRepository;

    @MockBean
    private FabricManager fabricManager;


    @Test
    void queryFabricLog() throws Exception {
        assertResponse(() -> {
            FabricLogRequest fabricLogRequest = new FabricLogRequest();
            fabricLogRequest.setLogHash(Sha256Utils.hash("testHash"));
            fabricLogRequest.setLogPath("/logs/test.log");
            Mockito.when(org.apache.commons.lang3.StringUtils.isBlank(fabricLogRequest.getLogHash())
                            && org.apache.commons.lang3.StringUtils.isBlank(fabricLogRequest.getLogPath())).
                    thenThrow(EasyPsiException.of(SystemErrorCode.VALIDATION_ERROR));
            Mockito.when(fabricLogRepository.findByLogPath(fabricLogRequest.getLogPath())).thenReturn(buildReturn().get());
            Mockito.when(fabricManager.evaluateTransactionByAssetId(fabricLogRequest.getLogHash())).thenReturn("success");
            return MockMvcRequestBuilders.post(getMappingUrl(FabricLogController.class, "queryFabricLog", FabricLogRequest.class))
                    .content(JsonUtils.toJSONString(fabricLogRequest));
        });
    }

    private Optional<FabricLogDO> buildReturn() {
        return Optional.of(FabricLogDO.builder().logHash("testHash").logPath("/logs/test.log").chainCodeName("alice")
                .build());
    }
}
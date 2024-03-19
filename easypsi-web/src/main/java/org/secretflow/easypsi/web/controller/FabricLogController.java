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

import org.apache.commons.lang3.StringUtils;
import org.secretflow.easypsi.common.annotation.resource.InterfaceResource;
import org.secretflow.easypsi.common.constant.resource.InterfaceResourceCode;
import org.secretflow.easypsi.common.errorcode.SystemErrorCode;
import org.secretflow.easypsi.common.exception.EasyPsiException;
import org.secretflow.easypsi.manager.integration.fabric.FabricManager;
import org.secretflow.easypsi.persistence.entity.FabricLogDO;
import org.secretflow.easypsi.persistence.repository.FabricLogRepository;
import org.secretflow.easypsi.service.model.common.EasyPsiResponse;
import org.secretflow.easypsi.service.model.fabric.FabricLogRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * Fabric log controller
 *
 * @author lihaixin
 * @date 2024/01/15
 */
@RestController
@RequestMapping(value = "/api/v1alpha1/fabricLog")
public class FabricLogController {

    @Autowired
    private FabricLogRepository fabricLogRepository;

    @Autowired
    private FabricManager fabricManager;

    /**
     * Query fabric log by log path or log hash
     *
     * @param fabricLogRequest
     * @return {@link EasyPsiResponse }<{@link Object }>
     */

    @ResponseBody
    @PostMapping(value = "/query", consumes = "application/json")
    @InterfaceResource(interfaceCode = InterfaceResourceCode.AUTH_LOGIN)
    public EasyPsiResponse<Object> queryFabricLog(@RequestBody FabricLogRequest fabricLogRequest) {
        if (StringUtils.isBlank(fabricLogRequest.getLogHash()) && StringUtils.isBlank(fabricLogRequest.getLogPath())) {
            throw EasyPsiException.of(SystemErrorCode.VALIDATION_ERROR, "params is null");
        }
        if (StringUtils.isBlank(fabricLogRequest.getLogHash())) {
            FabricLogDO fabricLogDO = fabricLogRepository.findByLogPath(fabricLogRequest.getLogPath());
            if (Objects.isNull(fabricLogDO)) {
                throw EasyPsiException.of(SystemErrorCode.HTTP_5XX_ERROR, "data is null");
            }
            fabricLogRequest.setLogHash(fabricLogDO.getLogHash());
        }
        try {
            return EasyPsiResponse.success(fabricManager.evaluateTransactionByAssetId(fabricLogRequest.getLogHash()));

        } catch (Exception exception) {
            throw EasyPsiException.of(SystemErrorCode.HTTP_5XX_ERROR, "data is null");
        }
    }

}

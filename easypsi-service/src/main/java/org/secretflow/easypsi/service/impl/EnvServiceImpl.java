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

import org.secretflow.easypsi.common.constant.PlatformType;
import org.secretflow.easypsi.common.dto.EnvDTO;
import org.secretflow.easypsi.service.EnvService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @author beiwei
 * @date 2023/9/13
 */
@Service
public class EnvServiceImpl implements EnvService {
    @Value("${easypsi.platform-type}")
    private String platformType;

    @Value("${easypsi.node-id}")
    private String nodeId;
    @Override
    public PlatformType getPlatformType() {
        return PlatformType.valueOf(platformType);
    }

    @Override
    public String getPlatformNodeId() {
        return nodeId;
    }

    @Override
    public EnvDTO getEnv() {
        EnvDTO envDTO = new EnvDTO();
        envDTO.setPlatformNodeId(nodeId);
        envDTO.setPlatformType(PlatformType.valueOf(platformType));
        return envDTO;
    }
}

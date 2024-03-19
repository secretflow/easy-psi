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

package org.secretflow.easypsi.service.model.fabric;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Fabric log request
 *
 * @author lihaixin
 * @date 2024/01/15
 */
@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FabricLogRequest {

    /**
     * Log path
     */
    @Schema(description = "log path")
    private String logPath;

    /**
     * Log hash
     */
    @Schema(description = "log hash")
    private String logHash;
}

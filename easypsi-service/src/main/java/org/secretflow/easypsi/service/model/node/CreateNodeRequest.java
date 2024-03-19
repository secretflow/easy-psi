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

package org.secretflow.easypsi.service.model.node;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.secretflow.easypsi.service.constant.Constants;

/**
 * Create node request
 *
 * @author : xiaonan.fhn
 * @date 2023/5/15
 */
@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateNodeRequest {

    /**
     * certTxt
     */
    @NotBlank
    @Schema(description = "certText")
    private String certText;

    /**
     * dstNetAddress
     */
    @NotBlank
    @Schema(description = "dstNetAddress")
    @Pattern(regexp = Constants.IP_PORT_PATTERN, message = "address not support rule")
    private String dstNetAddress;

    /**
     * nodeRemark
     */
    @Schema(description = "nodeRemark")
    private String nodeRemark;

    /**
     * trust
     */
    @Schema(description = "trust")
    private Boolean trust = false;
}

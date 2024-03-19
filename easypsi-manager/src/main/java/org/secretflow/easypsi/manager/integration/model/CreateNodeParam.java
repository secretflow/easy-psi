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

package org.secretflow.easypsi.manager.integration.model;

import lombok.Builder;
import lombok.Data;

/**
 * Parameters for creating a node
 *
 * @author : xiaonan.fhn
 * @date 2023/05/16
 */
@Data
@Builder
public class CreateNodeParam {

    /**
     * Node name, it can be reproducible and can not be empty
     */
    private String name;

    /**
     * Node id
     */
    private String nodeId;

    /**
     * Certificate related authorization
     */
    private String auth;

    /**
     * Node description
     */
    private String description;

    /**
     * node feature indicates by bit, bit0 - mpc | bit1 - tee | bit2 mpc&tee
     */
    private Integer mode;

    /**
     * node netAddress
     */
    private String netAddress;

    /**
     * node certText
     */
    private String certText;

    /**
     * node Remark
     */
    private String nodeRemark;

    /**
     * trust
     */
    private Boolean trust;
}

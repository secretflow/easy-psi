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

package org.secretflow.secretpad.service.model.node;

import org.secretflow.secretpad.manager.integration.model.NodeDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Node view object
 *
 * @author jiezi
 * @date 2023/5/31
 */
@Builder
@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class NodeVO {
    /**
     * id
     */
    @Schema(description = "nodeId")
    private String nodeId;
    /**
     * nodeName
     */
    @Schema(description = "nodeName")
    private String nodeName;
    /**
     * controlNodeId
     */
    @Schema(description = "controlNodeId")
    private String controlNodeId;
    /**
     * description
     */
    @Schema(description = "description")
    private String description;
    /**
     * netAddress
     */
    @Schema(description = "netAddress")
    private String netAddress;
    /**
     * nodeStatus Pending,  Ready,  NotReady,  Unknown
     */
    @Schema(description = "节点状态")
    private String nodeStatus;
    /**
     * gmtCreate
     */
    @Schema(description = "gmtCreate")
    private String gmtCreate;
    /**
     * gmtModified
     */
    @Schema(description = "gmtModified")
    private String gmtModified;
    /**
     * nodeCertText
     */
    @Schema(description = "certText")
    private String certText;
    /**
     * nodeRemark
     */
    @Schema(description = "nodeRemark")
    private String nodeRemark;

    public static NodeVO from(NodeDTO nodeDTO) {
        return NodeVO.builder().nodeId(nodeDTO.getNodeId()).nodeName(nodeDTO.getNodeName())
                .controlNodeId(nodeDTO.getControlNodeId()).description(nodeDTO.getDescription())
                .netAddress(nodeDTO.getNetAddress()).nodeStatus(nodeDTO.getNodeStatus())
                .gmtCreate(nodeDTO.getGmtCreate()).gmtModified(nodeDTO.getGmtModified())
                .nodeRemark(nodeDTO.getNodeRemark())
                .build();
    }

}

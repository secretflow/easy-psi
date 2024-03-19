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

package org.secretflow.easypsi.service.model.noderoute;

import org.secretflow.easypsi.common.constant.DomainRouterConstants;
import org.secretflow.easypsi.common.util.DateTimes;
import org.secretflow.easypsi.persistence.entity.NodeRouteDO;
import org.secretflow.easypsi.service.model.node.NodeVO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * @author yutu
 * @date 2023/08/04
 */
@Builder
@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class NodeRouterVO {
    /**
     * id
     */
    @Schema(description = "id")
    private String routeId;

    /**
     * srcNodeId
     */
    @Schema(description = "srcNodeId")
    private String srcNodeId;

    /**
     * dstNodeId
     */
    @Schema(description = "dstNodeId")
    private String dstNodeId;

    /**
     * srcNode
     */
    @Schema(description = "srcNode")
    private NodeVO srcNode;

    /**
     * dstNode
     */
    @Schema(description = "dstNode")
    private NodeVO dstNode;

    /**
     * srcNetAddress
     */
    @Schema(description = "srcNetAddress")
    private String srcNetAddress;

    /**
     * dstNetAddress
     */
    @Schema(description = "dstNetAddress")
    private String dstNetAddress;

    /**
     * status Pending,  Succeeded,  Failed,  Unknown
     */
    @Schema(description = "status")
    private String status;

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

    public static NodeRouterVO fromDo(NodeRouteDO nodeRouteDO) {
        NodeRouterVO nodeRouterVO = new NodeRouterVO();
        nodeRouterVO.setRouteId(String.valueOf(nodeRouteDO.getId()));
        nodeRouterVO.setSrcNodeId(nodeRouteDO.getSrcNodeId());
        nodeRouterVO.setDstNodeId(nodeRouteDO.getDstNodeId());
        nodeRouterVO.setSrcNetAddress(nodeRouteDO.getSrcNetAddress());
        nodeRouterVO.setDstNetAddress(nodeRouteDO.getDstNetAddress());
        nodeRouterVO.setGmtCreate(DateTimes.toRfc3339(nodeRouteDO.getGmtCreate()));
        nodeRouterVO.setGmtModified(DateTimes.toRfc3339(nodeRouteDO.getGmtModified()));
        nodeRouterVO.setStatus(DomainRouterConstants.DomainRouterStatusEnum.Unknown.name());
        return nodeRouterVO;
    }
}
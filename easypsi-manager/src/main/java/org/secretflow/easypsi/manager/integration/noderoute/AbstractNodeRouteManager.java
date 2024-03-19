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

package org.secretflow.easypsi.manager.integration.noderoute;

import org.secretflow.easypsi.manager.integration.model.CreateNodeRouteParam;
import org.secretflow.easypsi.manager.integration.model.UpdateNodeRouteParam;
import org.secretflow.easypsi.persistence.entity.NodeDO;
import org.secretflow.v1alpha1.kusciaapi.DomainRoute;

/**
 * @author yutu
 * @date 2023/08/07
 */
public abstract class AbstractNodeRouteManager {

    public abstract Long createNodeRoute(CreateNodeRouteParam param, boolean check);

    public abstract Long createNodeRoute(CreateNodeRouteParam param, NodeDO srcNode, NodeDO dstNode);

    public abstract void deleteNodeRoute(Long nodeRouteId);

    public abstract void updateNodeRoute(UpdateNodeRouteParam param);

    public abstract DomainRoute.RouteStatus getRouteStatus(String srcNodeId, String dstNodeId);

    public abstract boolean testAddress(String address);

    public abstract void checkRouteNotExist(String srcNodeId, String dstNodeId);
}
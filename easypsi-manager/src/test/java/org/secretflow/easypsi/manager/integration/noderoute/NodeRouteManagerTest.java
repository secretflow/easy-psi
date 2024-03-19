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

package org.secretflow.easypsi.manager.integration.noderoute;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.secretflow.easypsi.common.exception.EasyPsiException;
import org.secretflow.easypsi.persistence.entity.NodeRouteDO;
import org.secretflow.easypsi.persistence.repository.NodeRepository;
import org.secretflow.easypsi.persistence.repository.NodeRouteRepository;
import org.secretflow.v1alpha1.common.Common;
import org.secretflow.v1alpha1.kusciaapi.DomainRoute;
import org.secretflow.v1alpha1.kusciaapi.DomainRouteServiceGrpc;

import java.util.Optional;

/**
 * @author chenmingliang
 * @date 2024/03/12
 */
@ExtendWith(MockitoExtension.class)
public class NodeRouteManagerTest {

    @Mock
    private NodeRouteRepository nodeRouteRepository;
    @Mock
    private NodeRepository nodeRepository;
    @Mock
    private DomainRouteServiceGrpc.DomainRouteServiceBlockingStub routeServiceBlockingStub;

    @Test
    public void deleteNodeRoute() {
        NodeRouteManager nodeRouteManager = new NodeRouteManager(nodeRouteRepository,nodeRepository,routeServiceBlockingStub);
        DomainRoute.DeleteDomainRouteResponse deleteDomainRouteResponse = DomainRoute.DeleteDomainRouteResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(400).build()).build();
        Mockito.when(routeServiceBlockingStub.deleteDomainRoute(Mockito.any())).thenReturn(deleteDomainRouteResponse);
        NodeRouteDO nodeRouteDO = new NodeRouteDO();
        nodeRouteDO.setSrcNodeId("aaa");
        nodeRouteDO.setDstNodeId("aaa");
        Assertions.assertThrows(EasyPsiException.class,()->nodeRouteManager.deleteNodeRoute(nodeRouteDO));
        Mockito.when(nodeRouteRepository.findByRouteId(1L)).thenReturn(null);
        Assertions.assertThrows(EasyPsiException.class,()->nodeRouteManager.deleteNodeRoute(1L));

    }

    @Test
    public void testGetRouteStatus() {
        Mockito.when(routeServiceBlockingStub.queryDomainRoute(Mockito.any())).thenReturn(null);
        NodeRouteManager nodeRouteManager = new NodeRouteManager(nodeRouteRepository,nodeRepository,routeServiceBlockingStub);
        Assertions.assertThrows(EasyPsiException.class,()->nodeRouteManager.getRouteStatus("alice","bob"));

    }

    @Test
    public void testCheckNode() {
        NodeRouteManager nodeRouteManager = new NodeRouteManager(nodeRouteRepository,nodeRepository,routeServiceBlockingStub);
        Assertions.assertThrows(EasyPsiException.class,()->nodeRouteManager.checkNode(null));
    }

    @Test
    public void testCheckRouteExist() {
        NodeRouteManager nodeRouteManager = new NodeRouteManager(nodeRouteRepository,nodeRepository,routeServiceBlockingStub);
        Mockito.when(nodeRouteRepository.findBySrcNodeIdAndDstNodeId("alice","bob")).thenReturn(Optional.ofNullable(null));
        Assertions.assertThrows(EasyPsiException.class,()->nodeRouteManager.checkRouteNotExist("alice","bob"));
    }

}

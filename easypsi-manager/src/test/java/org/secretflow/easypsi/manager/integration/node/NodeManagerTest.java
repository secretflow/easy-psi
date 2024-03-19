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

package org.secretflow.easypsi.manager.integration.node;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.secretflow.easypsi.common.exception.EasyPsiException;
import org.secretflow.easypsi.manager.integration.model.UpdateNodeParam;
import org.secretflow.easypsi.persistence.entity.NodeDO;
import org.secretflow.easypsi.persistence.repository.NodeRepository;
import org.secretflow.v1alpha1.kusciaapi.DomainServiceGrpc;

/**
 * @author beiwei
 * @date 2023/9/11
 */
@ExtendWith(MockitoExtension.class)
class NodeManagerTest {

    @Mock
    NodeRepository nodeRepository;

    @Mock
    DomainServiceGrpc.DomainServiceBlockingStub domainServiceBlockingStub;

    @Test
    void genDomainId() {
        NodeManager nodeManager = new NodeManager(null, null, null);
        String s = nodeManager.genDomainId();
        Assertions.assertThat(s).hasSize(8);
    }

    @Test
    public void testCheckNodeCert() {
        NodeManager nodeManager = new NodeManager(null, null, null);
        org.junit.jupiter.api.Assertions.assertThrows(EasyPsiException.class,()->nodeManager.checkNodeCert(null));
    }

    @Test
    public void testUpdateNode() {
        NodeManager nodeManager = new NodeManager(nodeRepository, null, null);
        NodeDO nodeDO = new NodeDO();
        nodeDO.setTrust(true);
        Mockito.when(nodeRepository.findByNodeId("alice")).thenReturn(nodeDO);
        nodeManager.updateNode(UpdateNodeParam.builder().nodeId("alice").build());
    }

    @Test
    public void testGetNodeCertificate() {
        NodeManager nodeManager = new NodeManager(nodeRepository, domainServiceBlockingStub, null);
        Mockito.when(nodeRepository.findByNodeId("alice")).thenReturn(null);
        org.junit.jupiter.api.Assertions.assertThrows(EasyPsiException.class,()->nodeManager.getNodeCertificate("alice"));
    }

    @Test
    public void testQueryNode() {
        NodeManager nodeManager = new NodeManager(nodeRepository, domainServiceBlockingStub, null);
        Mockito.when(domainServiceBlockingStub.queryDomain(Mockito.any())).thenReturn(null);
        org.junit.jupiter.api.Assertions.assertThrows(EasyPsiException.class,()->nodeManager.queryNode("alice"));

    }
}
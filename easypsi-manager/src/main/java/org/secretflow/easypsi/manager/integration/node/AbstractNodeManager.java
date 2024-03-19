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

import org.secretflow.easypsi.manager.integration.model.CreateNodeParam;
import org.secretflow.easypsi.manager.integration.model.NodeCertificateDTO;
import org.secretflow.easypsi.manager.integration.model.NodeDTO;
import org.secretflow.easypsi.manager.integration.model.UpdateNodeParam;
import org.secretflow.easypsi.persistence.entity.NodeDO;
import org.secretflow.v1alpha1.kusciaapi.Domain;

/**
 * @author xiaonan
 * @date 2023/05/23
 */
public abstract class AbstractNodeManager {

    /**
     * Create node
     *
     * @param param create parma
     * @return nodeId
     */
    public abstract String createNode(CreateNodeParam param);

    /**
     * Update node
     *
     * @param param update parma
     * @return nodeId
     */
    public abstract NodeDO updateNode(UpdateNodeParam param);

    /**
     * Delete node
     *
     * @param nodeId nodeId
     */
    public abstract void deleteNode(String nodeId);

    /**
     * Get node information
     *
     * @param nodeId nodeId
     * @return NodeDTO
     */
    public abstract NodeDTO getNode(String nodeId);

    /**
     * Get node cert
     *
     * @param nodeId target nodeId
     * @return node cert
     */
    public abstract String getCert(String nodeId);

    /**
     * Check if node exists
     *
     * @param nodeId nodeId
     * @return boolean true exist false no exist
     */
    public abstract boolean checkNodeExists(String nodeId);

    /**
     * Get node certificate
     *
     * @param nodeId
     * @return
     */
    public abstract NodeCertificateDTO getNodeCertificate(String nodeId);

    /**
     * initial node
     */
    public abstract void initialNode(String nodeId);
    /**
     * check node cert
     */
    public abstract void checkNodeCert(String cert);

    /**
     * check node id
     */
    public abstract void checkNodeId(String srcNodeId,String dstNodeId);

    /**
     * check node status
     * @param nodeId
     * @return
     */
    public abstract boolean checkNodeStatus(String nodeId);

    public abstract Domain.QueryDomainResponse getNodeNotCheck(String nodeId);

}

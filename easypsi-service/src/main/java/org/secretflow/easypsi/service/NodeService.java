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

package org.secretflow.easypsi.service;

import org.secretflow.easypsi.service.model.node.*;
import org.secretflow.v1alpha1.kusciaapi.Domain;
import org.springframework.web.multipart.MultipartFile;

/**
 * Node service interface
 *
 * @author xiaonan
 * @date 2023/5/4
 */
public interface NodeService {

    /**
     * Create a node
     *
     * @param request create node request
     * @return nodeId
     */
    String createNode(CreateNodeRequest request);

    /**
     * update node
     *
     * @param request update node request
     * @return nodeId
     */
    NodeVO updateNode(UpdateNodeRequest request);

    /**
     * Delete a node
     *
     * @param routerId delete node request
     */
    void deleteNode(String routerId);

    /**
     * 查询节点
     *
     * @return 节点视图
     */
    NodeVO getNode();

    /**
     * Convert certificate
     *
     * @param file
     * @return
     */
    UploadNodeResultVO convertCertificate(MultipartFile file);

    /**
     * Dowmload certificate
     *
     * @param request
     * @return
     */
    CertificateDownloadInfo download(DownloadNodeCertificateRequest request);

    /**
     * initial node
     */
    void initialNode();

    Domain.QueryDomainResponse getNodeNotCheck();

}

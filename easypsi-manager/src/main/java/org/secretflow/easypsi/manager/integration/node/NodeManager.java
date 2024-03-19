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

import org.secretflow.easypsi.common.constant.DomainConstants;
import org.secretflow.easypsi.common.dto.UserContextDTO;
import org.secretflow.easypsi.common.errorcode.NodeErrorCode;
import org.secretflow.easypsi.common.exception.EasyPsiException;
import org.secretflow.easypsi.common.util.UUIDUtils;
import org.secretflow.easypsi.common.util.UserContext;
import org.secretflow.easypsi.manager.integration.model.CreateNodeParam;
import org.secretflow.easypsi.manager.integration.model.NodeCertificateDTO;
import org.secretflow.easypsi.manager.integration.model.NodeDTO;
import org.secretflow.easypsi.manager.integration.model.UpdateNodeParam;
import org.secretflow.easypsi.manager.kuscia.grpc.KusciaDomainRpc;
import org.secretflow.easypsi.persistence.entity.NodeDO;
import org.secretflow.easypsi.persistence.model.GraphJobStatus;
import org.secretflow.easypsi.persistence.repository.NodeRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.secretflow.v1alpha1.kusciaapi.Domain;
import org.secretflow.v1alpha1.kusciaapi.DomainServiceGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * Manager node operation
 *
 * @author xiaonan
 * @date 2023/05/23
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class NodeManager extends AbstractNodeManager {

    private final static Logger LOGGER = LoggerFactory.getLogger(NodeManager.class);
    private final NodeRepository nodeRepository;
    private final DomainServiceGrpc.DomainServiceBlockingStub domainServiceBlockingStub;
    private final KusciaDomainRpc kusciaDomainRpc;

    @Override
    public boolean checkNodeStatus(String nodeId) {
        List<String> statusByNodeId = nodeRepository.findStatusByNodeId(nodeId);
        for (String s : statusByNodeId) {
            if (!GraphJobStatus.checkJobFinalStatus(s)) {
                log.info("node exits running tasks, prohibit delete");
                return true;
            }
        }
        return false;
    }

    @Override
    public void checkNodeCert(String certText) {
        if (ObjectUtils.isEmpty(certText)) {
            throw EasyPsiException.of(NodeErrorCode.NODE_CERT_CONFIG_ERROR, "");
        }
    }

    @Override
    public void checkNodeId(String srcNodeId, String dstNodeId) {
        boolean exists = nodeRepository.existsById(dstNodeId);
        if (ObjectUtils.isEmpty(dstNodeId) || exists || getCert(srcNodeId).equals(dstNodeId) || srcNodeId.equals(dstNodeId)) {
            throw EasyPsiException.of(NodeErrorCode.NODE_CERT_CONFIG_ERROR, "");
        }
    }

    /**
     * Create node
     *
     * @param param create parma
     * @return nodeId
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createNode(CreateNodeParam param) {
        String nodeId = param.getNodeId();
        log.info("create nodeId: " + param.getNodeId());
        NodeDO nodeDO = NodeDO.builder().controlNodeId(nodeId).nodeId(nodeId)
                .netAddress(param.getNetAddress())
                .certText(param.getCertText())
                .nodeRemark(param.getNodeRemark())
                .name(param.getName())
                .trust(param.getTrust()).build();
        nodeRepository.save(nodeDO);

        Domain.CreateDomainRequest request = Domain.CreateDomainRequest.newBuilder().setDomainId(nodeId)
                .setAuthCenter(
                        Domain.AuthCenter.newBuilder().setAuthenticationType("Token")
                                .setTokenGenMethod("RSA-GEN")
                                .build())
                .setRole("partner")
                .setCert(param.getCertText())
                .build();
        Domain.CreateDomainResponse domain = Domain.CreateDomainResponse.newBuilder().build();
        try {
            domain = kusciaDomainRpc.createDomain(request);
        } catch (Exception e) {
            LOGGER.error(nodeId + " node create fail in kuscia :" + domain.getStatus().getMessage());
            throw EasyPsiException.of(NodeErrorCode.NODE_CREATE_ERROR, nodeId + " node create fail in kuscia :" + domain.getStatus().getMessage());
        }
        return nodeId;
    }

    @Override
    public NodeDO updateNode(UpdateNodeParam param) {
        NodeDO byNodeId = nodeRepository.findByNodeId(param.getNodeId());
        byNodeId.setTrust(param.getTrust());
        return nodeRepository.save(byNodeId);
    }

    @Override
    public void deleteNode(String nodeId) {
        log.info("check node exits running tasks");
        if (!checkNodeExists(nodeId)) {
            nodeRepository.deleteById(nodeId);
            nodeRepository.flush();
            LOGGER.error("node {} is not exist! but delete anyway", nodeId);
            return;
        }
        // call the api interface to delete node
        Domain.DeleteDomainRequest request = Domain.DeleteDomainRequest.newBuilder().setDomainId(nodeId).build();
        nodeRepository.deleteById(nodeId);
        Domain.DeleteDomainResponse deleteDomainResponse = Domain.DeleteDomainResponse.newBuilder().build();
        try {
            deleteDomainResponse = kusciaDomainRpc.deleteDomain(request);
        } catch (Exception e) {
            throw EasyPsiException.of(NodeErrorCode.NODE_DELETE_ERROR, nodeId + " kuscia delete node fail :" + deleteDomainResponse.getStatus().getMessage());
        }
    }

    @Override
    public NodeDTO getNode(String nodeId) {
        NodeDO nodeDO = nodeRepository.findByNodeId(nodeId);
        if (ObjectUtils.isEmpty(nodeDO)) {
            LOGGER.error("Cannot find node by nodeId {}.", nodeId);
            throw EasyPsiException.of(NodeErrorCode.NODE_NOT_EXIST_ERROR);
        }
        Domain.QueryDomainRequest request = Domain.QueryDomainRequest.newBuilder().setDomainId(nodeId).build();
        Domain.QueryDomainResponse response = null;
        try {
            response = kusciaDomainRpc.queryDomainNoCheck(request);
        } catch (Exception e) {
            log.error("kuscia connect error: {}", e.getMessage());
        }
        NodeDTO nodeDTO = NodeDTO.fromDo(nodeDO);
        nodeDTO.setNodeStatus(getNodeStatus(response) ? DomainConstants.DomainStatusEnum.NotReady.name() : DomainConstants.DomainStatusEnum.Ready.name());
        return nodeDTO;
    }

    @Override
    public Domain.QueryDomainResponse getNodeNotCheck(String nodeId) {
        Domain.QueryDomainRequest request = Domain.QueryDomainRequest.newBuilder().setDomainId(nodeId).build();
        Domain.QueryDomainResponse response = kusciaDomainRpc.queryDomainNoCheck(request);
        return response;
    }

    @Override
    public String getCert(String nodeId) {
        Domain.QueryDomainRequest queryDomainRequest =
                Domain.QueryDomainRequest.newBuilder().setDomainId(nodeId).build();
        Domain.QueryDomainResponse response = kusciaDomainRpc.queryDomainNoCheck(queryDomainRequest);
        if (response.getStatus().getCode() != 0) {
            return "";
        }
        if (ObjectUtils.isEmpty(response.getData())) {
            return "";
        }
        return response.getData().getCert();
    }

    /**
     * Check whether node exists in domain service stub
     *
     * @param nodeId nodeId
     * @return whether node exists in domain service stub
     */
    @Override
    public boolean checkNodeExists(String nodeId) {
        Domain.QueryDomainRequest request = Domain.QueryDomainRequest.newBuilder()
                .setDomainId(nodeId)
                .build();
        Domain.QueryDomainResponse response = domainServiceBlockingStub.queryDomain(request);
        return response.getStatus().getCode() == 0;
    }

    @Override
    public NodeCertificateDTO getNodeCertificate(String nodeId) {
        NodeDO nodeDO = nodeRepository.findByNodeId(nodeId);
        if (ObjectUtils.isEmpty(nodeDO)) {
            LOGGER.error("Cannot find node by nodeId {}.", nodeId);
            throw EasyPsiException.of(NodeErrorCode.NODE_NOT_EXIST_ERROR);
        }
        NodeCertificateDTO nodeCertificateDTO = new NodeCertificateDTO();
        nodeCertificateDTO.setNodeId(nodeDO.getNodeId());
        nodeCertificateDTO.setCertText(nodeDO.getCertText());
        return nodeCertificateDTO;
    }

    @Override
    public void initialNode(String nodeId) {
        Domain.QueryDomainResponseData data = queryNode(nodeId).getData();
        NodeDO build = NodeDO.builder().nodeId(nodeId)
                .description(nodeId)
                .name(nodeId).netAddress("127.0.0.1:28080").controlNodeId(nodeId)
                .certText(data.getCert()).build();
        UserContextDTO userContextDTO = new UserContextDTO();
        userContextDTO.setName("admin");
        UserContext.setBaseUser(userContextDTO);
        nodeRepository.saveAndFlush(build);
        log.info("initial node:" + nodeId);
    }


    public Domain.QueryDomainResponse queryNode(String nodeId) {
        Domain.QueryDomainRequest request = Domain.QueryDomainRequest.newBuilder()
                .setDomainId(nodeId)
                .build();
        Domain.QueryDomainResponse response =
                domainServiceBlockingStub.queryDomain(request);
        if (ObjectUtils.isEmpty(response)) {
            throw EasyPsiException.of(NodeErrorCode.NODE_NOT_EXIST_ERROR, nodeId + ":query node error,node not exits");
        }
        return response;
    }

    protected String genDomainId() {
        return UUIDUtils.random(8);
    }

    private boolean getNodeStatus(Domain.QueryDomainResponse response) {
        return (ObjectUtils.isEmpty(response) || response.getStatus().getCode() != 0);
    }

}

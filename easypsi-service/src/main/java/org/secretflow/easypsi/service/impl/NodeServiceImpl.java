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

package org.secretflow.easypsi.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.secretflow.easypsi.common.constant.PermissionTargetType;
import org.secretflow.easypsi.common.constant.PermissionUserType;
import org.secretflow.easypsi.common.constant.role.RoleCodeConstants;
import org.secretflow.easypsi.common.errorcode.NodeErrorCode;
import org.secretflow.easypsi.common.exception.EasyPsiException;
import org.secretflow.easypsi.common.util.FileUtils;
import org.secretflow.easypsi.manager.integration.job.AbstractJobManager;
import org.secretflow.easypsi.manager.integration.model.CreateNodeParam;
import org.secretflow.easypsi.manager.integration.model.NodeCertificateDTO;
import org.secretflow.easypsi.manager.integration.model.NodeDTO;
import org.secretflow.easypsi.manager.integration.model.UpdateNodeParam;
import org.secretflow.easypsi.manager.integration.node.AbstractNodeManager;
import org.secretflow.easypsi.manager.integration.noderoute.AbstractNodeRouteManager;
import org.secretflow.easypsi.persistence.entity.NodeRouteDO;
import org.secretflow.easypsi.persistence.entity.SysUserPermissionRelDO;
import org.secretflow.easypsi.persistence.repository.NodeRouteRepository;
import org.secretflow.easypsi.persistence.repository.SysUserPermissionRelRepository;
import org.secretflow.easypsi.service.EnvService;
import org.secretflow.easypsi.service.NodeRouterService;
import org.secretflow.easypsi.service.NodeService;
import org.secretflow.easypsi.service.model.node.*;
import org.secretflow.easypsi.service.model.noderoute.CreateNodeRouterRequest;
import org.secretflow.v1alpha1.kusciaapi.Domain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

/**
 * Node service implementation class
 *
 * @author : xiaonan.fhn
 * @date 2023/5/23
 */
@Slf4j
@Service
public class NodeServiceImpl implements NodeService {

    @Autowired
    private AbstractNodeManager nodeManager;

    @Autowired
    private AbstractNodeRouteManager nodeRouteManager;

    @Autowired
    private NodeRouteRepository nodeRouteRepository;

    @Autowired
    private NodeRouterService nodeRouterService;

    @Autowired
    private EnvService envService;

    @Autowired
    private AbstractJobManager jobManager;

    @Autowired
    private SysUserPermissionRelRepository permissionRelRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createNode(CreateNodeRequest request) {
        nodeManager.checkNodeCert(request.getCertText());
        String nodeId = getNodeIdFromX509Cert(parseCertificate(request.getCertText()));
        log.info("create node id:{}", nodeId);
        nodeManager.checkNodeId(envService.getPlatformNodeId(), nodeId);
        CreateNodeParam param = CreateNodeParam.builder()
                .nodeId(nodeId)
                .name(nodeId)
                .netAddress(request.getDstNetAddress())
                .certText(request.getCertText())
                .nodeRemark(request.getNodeRemark())
                .trust(request.getTrust())
                .build();
        nodeManager.createNode(param);
        nodeRouterService.createNodeRouter(CreateNodeRouterRequest.builder()
                .srcNodeId(envService.getPlatformNodeId())
                .dstNodeId(nodeId)
                .dstNetAddress(request.getDstNetAddress())
                .build());
        addNodePermissions(nodeId);
        return nodeId;
    }

    @Override
    public NodeVO updateNode(UpdateNodeRequest request) {
        NodeDTO it = NodeDTO.fromDo(nodeManager.updateNode(UpdateNodeParam.builder()
                .nodeId(request.getNodeId())
                .trust(request.getTrust()).build()));
        return NodeVO.from(it);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteNode(String routerId) {
        Long id = Long.parseLong(routerId);
        NodeRouteDO byRouteId = nodeRouteRepository.findByRouteId(id);
        if (ObjectUtils.isEmpty(byRouteId)) {
            throw EasyPsiException.of(NodeErrorCode.NODE_NOT_EXIST_ERROR,
                    "node not exist " + routerId);
        }
        if (nodeManager.checkNodeStatus(byRouteId.getDstNodeId())) {
            throw EasyPsiException.of(NodeErrorCode.NODE_DELETE_UNFINISHED_ERROR);
        }
        nodeManager.deleteNode(byRouteId.getDstNodeId());
        nodeRouteManager.deleteNodeRoute(id);
        permissionRelRepository.deleteByUserKey(byRouteId.getDstNodeId());
    }

    @Override
    public NodeVO getNode() {
        NodeDTO it = nodeManager.getNode(envService.getPlatformNodeId());
        return NodeVO.from(it);
    }

    @Override
    public Domain.QueryDomainResponse getNodeNotCheck() {
        Domain.QueryDomainResponse response = nodeManager.getNodeNotCheck(envService.getPlatformNodeId());
        return response;
    }

    @Override
    public UploadNodeResultVO convertCertificate(MultipartFile multipartFile) {
        String certificate;

        log.info("upload node cert check file name: {}", multipartFile.getOriginalFilename());
        FileUtils.fileNameCheck(multipartFile.getOriginalFilename());

        try {
            certificate = new Base64().encodeToString(multipartFile.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return UploadNodeResultVO.builder()
                .certificate(certificate)
                .build();

    }


    @Override
    public CertificateDownloadInfo download(DownloadNodeCertificateRequest request) {
        NodeCertificateDTO nodeCertificate = nodeManager.getNodeCertificate(request.getNodeId());
        return CertificateDownloadInfo.builder()
                .fileName(nodeCertificate.getNodeId())
                .certText(nodeCertificate.getCertText())
                .build();
    }

    @Override
    public void initialNode() {
        nodeManager.initialNode(envService.getPlatformNodeId());
    }

    private void addNodePermissions(String nodeId) {
        SysUserPermissionRelDO sysUserPermission = new SysUserPermissionRelDO();
        sysUserPermission.setUserType(PermissionUserType.NODE);
        sysUserPermission.setTargetType(PermissionTargetType.ROLE);
        SysUserPermissionRelDO.UPK upk = new SysUserPermissionRelDO.UPK();
        upk.setUserKey(nodeId);
        upk.setTargetCode(RoleCodeConstants.P2P_NODE);
        sysUserPermission.setUpk(upk);
        permissionRelRepository.save(sysUserPermission);
    }

    private X509Certificate parseCertificate(String certText) {
        CertificateFactory cf;
        X509Certificate cert;

        byte[] decode = new Base64().decode(certText);

        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(decode)) {
            cf = CertificateFactory.getInstance("X.509");
            cert = (X509Certificate) cf.generateCertificate(byteArrayInputStream);

        } catch (IOException | CertificateException e) {
            throw new RuntimeException(e);
        }
        return cert;
    }

    private String getNodeIdFromX509Cert(X509Certificate cert) {
        String subjectDN = cert.getSubjectX500Principal().getName();
        if (StringUtils.isNotBlank(subjectDN)) {
            return subjectDN.substring(subjectDN.lastIndexOf('=') + 1);
        }
        return "";
    }

}

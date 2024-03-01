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

package org.secretflow.secretpad.service.impl;

import org.secretflow.secretpad.common.constant.PermissionTargetType;
import org.secretflow.secretpad.common.constant.PermissionUserType;
import org.secretflow.secretpad.common.constant.role.RoleCodeConstants;
import org.secretflow.secretpad.common.errorcode.NodeErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.common.util.FileUtils;
import org.secretflow.secretpad.manager.integration.job.AbstractJobManager;
import org.secretflow.secretpad.manager.integration.model.CreateNodeParam;
import org.secretflow.secretpad.manager.integration.model.NodeCertificateDTO;
import org.secretflow.secretpad.manager.integration.model.NodeDTO;
import org.secretflow.secretpad.manager.integration.node.AbstractNodeManager;
import org.secretflow.secretpad.manager.integration.noderoute.AbstractNodeRouteManager;
import org.secretflow.secretpad.persistence.entity.NodeRouteDO;
import org.secretflow.secretpad.persistence.entity.SysUserPermissionRelDO;
import org.secretflow.secretpad.persistence.repository.NodeRouteRepository;
import org.secretflow.secretpad.persistence.repository.SysUserPermissionRelRepository;
import org.secretflow.secretpad.service.EnvService;
import org.secretflow.secretpad.service.NodeRouterService;
import org.secretflow.secretpad.service.NodeService;
import org.secretflow.secretpad.service.model.node.*;
import org.secretflow.secretpad.service.model.noderoute.CreateNodeRouterRequest;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.secretflow.v1alpha1.kusciaapi.Domain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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
        CreateNodeParam param = CreateNodeParam.builder()
                .nodeId(request.getDstNodeId())
                .name(request.getDstNodeId())
                .netAddress(request.getDstNetAddress())
                .certText(request.getCertText())
                .nodeRemark(request.getNodeRemark())
                .build();
        nodeManager.checkNodeCert(envService.getPlatformNodeId(), param);
        String nodeId = nodeManager.createNode(param);
        nodeRouterService.createNodeRouter(CreateNodeRouterRequest.builder()
                .srcNodeId(envService.getPlatformNodeId())
                .dstNodeId(request.getDstNodeId())
                .dstNetAddress(request.getDstNetAddress())
                .build());

        SysUserPermissionRelDO sysUserPermission = new SysUserPermissionRelDO();
        sysUserPermission.setUserType(PermissionUserType.NODE);
        sysUserPermission.setTargetType(PermissionTargetType.ROLE);
        SysUserPermissionRelDO.UPK upk = new SysUserPermissionRelDO.UPK();
        upk.setUserKey(nodeId);
        upk.setTargetCode(RoleCodeConstants.P2P_NODE);
        sysUserPermission.setUpk(upk);
        permissionRelRepository.save(sysUserPermission);
        return nodeId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteNode(String routerId) {
        Long id = Long.parseLong(routerId);
        NodeRouteDO byRouteId = nodeRouteRepository.findByRouteId(id);
        if (ObjectUtils.isEmpty(byRouteId)) {
            throw SecretpadException.of(NodeErrorCode.NODE_NOT_EXIST_ERROR,
                    "node not exist " + routerId);
        }
        if (nodeManager.checkNodeStatus(byRouteId.getDstNodeId())) {
            throw SecretpadException.of(NodeErrorCode.NODE_DELETE_UNFINISHED_ERROR);
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
        CertificateFactory cf;
        X509Certificate cert;
        String nodeId = "";
        String certificate;

        log.info("upload node cert check file name: {}",multipartFile.getOriginalFilename());
        FileUtils.fileNameCheck(multipartFile.getOriginalFilename());

        try (InputStream inputStream  = multipartFile.getInputStream() ){
            cf = CertificateFactory.getInstance("X.509");
            cert = (X509Certificate) cf.generateCertificate(multipartFile.getInputStream());
            String subjectDN = cert.getSubjectDN().getName();
            if (StringUtils.isNotBlank(subjectDN)) {
                nodeId = subjectDN.substring(subjectDN.lastIndexOf('=') + 1);
            }

            byte[] buffer = new byte[multipartFile.getBytes().length];
            inputStream.read(buffer);
            certificate = new Base64().encodeToString(buffer);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (CertificateException e) {
            throw new RuntimeException(e);
        }

        return UploadNodeResultVO.builder()
                .certificate(certificate)
                .nodeId(nodeId)
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
}

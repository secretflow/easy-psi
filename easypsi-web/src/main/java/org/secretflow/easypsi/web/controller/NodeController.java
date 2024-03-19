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

package org.secretflow.easypsi.web.controller;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Base64;
import org.secretflow.easypsi.common.annotation.resource.DataResource;
import org.secretflow.easypsi.common.annotation.resource.InterfaceResource;
import org.secretflow.easypsi.common.constant.resource.DataResourceType;
import org.secretflow.easypsi.common.constant.resource.InterfaceResourceCode;
import org.secretflow.easypsi.common.errorcode.SystemErrorCode;
import org.secretflow.easypsi.common.exception.EasyPsiException;
import org.secretflow.easypsi.service.NodeService;
import org.secretflow.easypsi.service.model.common.EasyPsiResponse;
import org.secretflow.easypsi.service.model.node.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author xiaonan
 */
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/v1alpha1/node")
public class NodeController {

    private final static Logger LOGGER = LoggerFactory.getLogger(NodeController.class);

    private final NodeService nodeService;

    /**
     * Create a new node api
     *
     * @param request create node request
     * @return successful EasyPsiResponse with nodeId
     */
    @PostMapping(value = "/create", consumes = "application/json")
    @InterfaceResource(interfaceCode = InterfaceResourceCode.NODE_CREATE)
    public EasyPsiResponse<String> createNode(@Valid @RequestBody CreateNodeRequest request) {
        String node = nodeService.createNode(request);
        return EasyPsiResponse.success(node);
    }

    /**
     * Update node api
     *
     * @param request create node request
     * @return successful EasyPsiResponse with nodeId
     */
    @PostMapping(value = "/update", consumes = "application/json")
    @InterfaceResource(interfaceCode = InterfaceResourceCode.NODE_CREATE)
    public EasyPsiResponse<NodeVO> updateNode(@Valid @RequestBody UpdateNodeRequest request) {
        return EasyPsiResponse.success(nodeService.updateNode(request));
    }
    @PostMapping(value = "/get", consumes = "application/json")
    @DataResource(field = "nodeId", resourceType = DataResourceType.NODE_ID)
    @InterfaceResource(interfaceCode = InterfaceResourceCode.NODE_GET)
    public EasyPsiResponse<NodeVO> get() {
        return EasyPsiResponse.success(nodeService.getNode());
    }


    @PostMapping(value = "/delete", consumes = "application/json")
    @DataResource(field = "nodeId", resourceType = DataResourceType.NODE_ID)
    @InterfaceResource(interfaceCode = InterfaceResourceCode.NODE_DELETE)
    public EasyPsiResponse<Void> deleteNode(@Valid @RequestBody DeleteNodeIdRequest request) {
        nodeService.deleteNode(request.getRouterId());
        return EasyPsiResponse.success();
    }

    /**
     * Upload node certificate api
     *
     * @param file multipart file
     * @return successful EasyPsiResponse with upload certificate result view object
     */
    @ResponseBody
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @InterfaceResource(interfaceCode = InterfaceResourceCode.NODE_CERTIFICATE_UPLOAD)
    public EasyPsiResponse<UploadNodeResultVO> upload(
            @RequestParam("file") MultipartFile file
    ) {
        return EasyPsiResponse.success(nodeService.convertCertificate(file));
    }

    /**
     * Download node certificate api
     *
     * @param response http servlet response
     * @return successful EasyPsiResponse with null data
     */
    @ResponseBody
    @PostMapping(value = "/download")
    @DataResource(field = "nodeId", resourceType = DataResourceType.NODE_ID)
    @InterfaceResource(interfaceCode = InterfaceResourceCode.NODE_CERTIFICATE_DOWNLOAD)
    public void download(HttpServletResponse response, @Valid @RequestBody DownloadNodeCertificateRequest request) {
        CertificateDownloadInfo downloadInfo = nodeService.download(request);
        response.setContentType(MediaType.TEXT_PLAIN_VALUE);
        response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + downloadInfo.getFileName());
        byte[] bytes = Base64.decodeBase64(downloadInfo.getCertText());
        response.setContentLength(bytes.length);
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            outputStream.write(bytes);
            outputStream.close();
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw EasyPsiException.of(SystemErrorCode.UNKNOWN_ERROR, e);
        }
    }

}

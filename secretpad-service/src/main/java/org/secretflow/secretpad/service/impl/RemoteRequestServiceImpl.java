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

import org.secretflow.secretpad.common.constant.DomainRouterConstants;
import org.secretflow.secretpad.common.errorcode.JobErrorCode;
import org.secretflow.secretpad.common.errorcode.KusciaGrpcErrorCode;
import org.secretflow.secretpad.common.errorcode.NodeRouteErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.common.util.RestTemplateUtil;
import org.secretflow.secretpad.service.NodeRouterService;
import org.secretflow.secretpad.service.NodeService;
import org.secretflow.secretpad.service.RemoteRequestService;
import org.secretflow.secretpad.service.model.common.SecretPadResponse;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.Future;

/**
 * @author liujunhao
 * @date 2023/11/20
 */
@Service
public class RemoteRequestServiceImpl implements RemoteRequestService {

    private final static Logger LOGGER = LoggerFactory.getLogger(RemoteRequestService.class);

    @Autowired
    private NodeRouterService nodeRouterService;

    @Autowired
    private NodeService nodeService;


    @Override
    public boolean checkBothSidesNodeRouteIsReady(String srcNodeId, String dstNodeId) {
        LOGGER.info("check node nodeRoute status");
        String nodeRouteStatus;
        try{
            nodeService.getNodeNotCheck();
        }catch (Exception e){
            throw SecretpadException.of(KusciaGrpcErrorCode.KUSCIA_CPMMECT_ERROR);
        }
        try {
            nodeRouteStatus = nodeRouterService.getNodeRouteStatus(srcNodeId, dstNodeId);
        }catch (Exception e){
            throw SecretpadException.of(NodeRouteErrorCode.NODE_ROUTE_NOT_READY, dstNodeId);
        }
        if (!StringUtils.equals(nodeRouteStatus, DomainRouterConstants.DomainRouterStatusEnum.Succeeded.name())) {
            throw SecretpadException.of(NodeRouteErrorCode.NODE_ROUTE_NOT_READY,  dstNodeId);
        }
        return true;
    }

    @Override
    public SecretPadResponse sendPostJson(Object request, String partnerNodeId, String url) {
        String svc = "secretpad." + partnerNodeId + ".svc";
        ImmutableMap<String, String> immutableMap = ImmutableMap.of("Host", svc);
        SecretPadResponse secretPadResponse = RestTemplateUtil.sendPostJson(url, request, immutableMap, SecretPadResponse.class);
        LOGGER.info("secretPadResponse={}", JsonUtils.toJSONString(secretPadResponse));
        if (secretPadResponse.getStatus().getCode() != 0) {
            LOGGER.error("secretPadResponse error msg={}", secretPadResponse.getStatus().getMsg());
            throw SecretpadException.of(JobErrorCode.PROJECT_JOB_RPC_ERROR,secretPadResponse.getStatus().getMsg());
        }
        return secretPadResponse;
    }

    @Async
    @Override
    public Future<SecretPadResponse> asyncSendPostJson(Object request, String partnerNodeId, String url) {
        return new AsyncResult<>(sendPostJson(request,partnerNodeId,url));
    }
}

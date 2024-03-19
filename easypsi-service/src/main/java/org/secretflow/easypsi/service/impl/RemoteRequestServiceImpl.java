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

import com.google.common.collect.ImmutableMap;
import org.secretflow.easypsi.common.errorcode.JobErrorCode;
import org.secretflow.easypsi.common.errorcode.KusciaGrpcErrorCode;
import org.secretflow.easypsi.common.exception.EasyPsiException;
import org.secretflow.easypsi.common.util.JsonUtils;
import org.secretflow.easypsi.common.util.RestTemplateUtil;
import org.secretflow.easypsi.service.NodeRouterService;
import org.secretflow.easypsi.service.NodeService;
import org.secretflow.easypsi.service.RemoteRequestService;
import org.secretflow.easypsi.service.model.common.EasyPsiResponse;
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
        try {
            nodeService.getNodeNotCheck();
        } catch (Exception e) {
            throw EasyPsiException.of(KusciaGrpcErrorCode.KUSCIA_CPMMECT_ERROR);
        }
        return true;
    }

    @Async
    @Override
    public Future<Boolean> asynCheckBothSidesNodeRouteIsReady(String srcNodeId, String dstNodeId) {
        return new AsyncResult<>(checkBothSidesNodeRouteIsReady(srcNodeId, dstNodeId));
    }

    @Override
    public EasyPsiResponse sendPostJson(Object request, String partnerNodeId, String url) {
        String svc = "secretpad." + partnerNodeId + ".svc";
        ImmutableMap<String, String> immutableMap = ImmutableMap.of("Host", svc);
        EasyPsiResponse easyPsiResponse = RestTemplateUtil.sendPostJson(url, request, immutableMap, EasyPsiResponse.class);
        LOGGER.info("EasyPsiResponse={}", JsonUtils.toJSONString(easyPsiResponse));
        if (easyPsiResponse.getStatus().getCode() != 0) {
            LOGGER.error("EasyPsiResponse error msg={}", easyPsiResponse.getStatus().getMsg());
            throw EasyPsiException.of(JobErrorCode.PROJECT_JOB_RPC_ERROR, easyPsiResponse.getStatus().getMsg());
        }
        return easyPsiResponse;
    }

    @Async
    @Override
    public Future<EasyPsiResponse> asyncSendPostJson(Object request, String partnerNodeId, String url) {
        return new AsyncResult<>(sendPostJson(request, partnerNodeId, url));
    }
}

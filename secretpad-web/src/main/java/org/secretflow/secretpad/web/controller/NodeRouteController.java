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

package org.secretflow.secretpad.web.controller;

import org.secretflow.secretpad.common.annotation.resource.InterfaceResource;
import org.secretflow.secretpad.common.constant.resource.InterfaceResourceCode;
import org.secretflow.secretpad.service.NodeRouterService;
import org.secretflow.secretpad.service.model.common.SecretPadResponse;
import org.secretflow.secretpad.service.model.noderoute.NodeRouterVO;
import org.secretflow.secretpad.service.model.noderoute.RouterAddressRequest;
import org.secretflow.secretpad.service.model.noderoute.RouterIdRequest;
import org.secretflow.secretpad.service.model.noderoute.UpdateNodeRouterRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author yutu
 * @date 2023/08/09
 */
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/v1alpha1/nodeRoute")
public class NodeRouteController {
    private final NodeRouterService nodeRouterService;

    @PostMapping(value = "/update", consumes = "application/json")
    @InterfaceResource(interfaceCode = InterfaceResourceCode.NODE_ROUTE_UPDATE)
    public SecretPadResponse<String> update(@Valid @RequestBody UpdateNodeRouterRequest request) {
        nodeRouterService.updateNodeRouter(request);
        return SecretPadResponse.success(request.getRouterId());
    }

    @PostMapping(value = "/refresh", consumes = "application/json")
    @InterfaceResource(interfaceCode = InterfaceResourceCode.NODE_ROUTE_REFRESH)
    public SecretPadResponse<NodeRouterVO> refresh(@Valid @RequestBody RouterIdRequest request) {
        return SecretPadResponse.success(nodeRouterService.refreshRouter(Long.parseLong(request.getRouterId())));
    }

    @PostMapping(value = "/test", consumes = "application/json")
    @InterfaceResource(interfaceCode = InterfaceResourceCode.NODE_ROUTE_TEST)
    public SecretPadResponse<Boolean> test(@Valid @RequestBody RouterAddressRequest request){
        return SecretPadResponse.success(nodeRouterService.testAddress(request.getNetAddress()));
    }

    @PostMapping(value = "/collaborationRoute")
    @InterfaceResource(interfaceCode = InterfaceResourceCode.NODE_ROUTE_LIST)
    public SecretPadResponse<List<NodeRouterVO>> queryCollaborationList() {
        return SecretPadResponse.success(nodeRouterService.listNodeRoute());
    }

}
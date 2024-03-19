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

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.secretflow.easypsi.common.annotation.resource.InterfaceResource;
import org.secretflow.easypsi.common.constant.resource.InterfaceResourceCode;
import org.secretflow.easypsi.service.NodeRouterService;
import org.secretflow.easypsi.service.model.common.EasyPsiResponse;
import org.secretflow.easypsi.service.model.noderoute.NodeRouterVO;
import org.secretflow.easypsi.service.model.noderoute.RouterAddressRequest;
import org.secretflow.easypsi.service.model.noderoute.RouterIdRequest;
import org.secretflow.easypsi.service.model.noderoute.UpdateNodeRouterRequest;
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
    public EasyPsiResponse<String> update(@Valid @RequestBody UpdateNodeRouterRequest request) {
        nodeRouterService.updateNodeRouter(request);
        return EasyPsiResponse.success(request.getRouterId());
    }

    @PostMapping(value = "/refresh", consumes = "application/json")
    @InterfaceResource(interfaceCode = InterfaceResourceCode.NODE_ROUTE_REFRESH)
    public EasyPsiResponse<NodeRouterVO> refresh(@Valid @RequestBody RouterIdRequest request) {
        return EasyPsiResponse.success(nodeRouterService.refreshRouter(Long.parseLong(request.getRouterId())));
    }

    @PostMapping(value = "/test", consumes = "application/json")
    @InterfaceResource(interfaceCode = InterfaceResourceCode.NODE_ROUTE_TEST)
    public EasyPsiResponse<Boolean> test(@Valid @RequestBody RouterAddressRequest request){
        return EasyPsiResponse.success(nodeRouterService.testAddress(request.getNetAddress()));
    }

    @PostMapping(value = "/collaborationRoute")
    @InterfaceResource(interfaceCode = InterfaceResourceCode.NODE_ROUTE_LIST)
    public EasyPsiResponse<List<NodeRouterVO>> queryCollaborationList() {
        return EasyPsiResponse.success(nodeRouterService.listNodeRoute());
    }

}
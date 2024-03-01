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
import org.secretflow.secretpad.common.errorcode.NodeRouteErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.manager.integration.model.CreateNodeRouteParam;
import org.secretflow.secretpad.manager.integration.model.UpdateNodeRouteParam;
import org.secretflow.secretpad.manager.integration.node.NodeManager;
import org.secretflow.secretpad.manager.integration.noderoute.AbstractNodeRouteManager;
import org.secretflow.secretpad.manager.integration.noderoute.NodeRouteManager;
import org.secretflow.secretpad.persistence.entity.NodeRouteDO;
import org.secretflow.secretpad.persistence.repository.NodeRouteRepository;
import org.secretflow.secretpad.service.EnvService;
import org.secretflow.secretpad.service.NodeRouterService;
import org.secretflow.secretpad.service.model.node.NodeVO;
import org.secretflow.secretpad.service.model.noderoute.CreateNodeRouterRequest;
import org.secretflow.secretpad.service.model.noderoute.NodeRouterVO;
import org.secretflow.secretpad.service.model.noderoute.UpdateNodeRouterRequest;

import lombok.extern.slf4j.Slf4j;
import org.secretflow.v1alpha1.kusciaapi.DomainRoute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author yutu
 * @date 2023/08/04
 */
@Slf4j
@Service
public class NodeRouterServiceImpl implements NodeRouterService {

    private final static Logger LOGGER = LoggerFactory.getLogger(NodeRouteManager.class);

    @Autowired
    private AbstractNodeRouteManager nodeRouteManager;
    @Autowired
    private NodeRouteRepository nodeRouteRepository;
    @Autowired
    private NodeManager nodeManager;
    @Autowired
    private EnvService envService;

    @Override
    public String createNodeRouter(CreateNodeRouterRequest request) {
        LOGGER.info("create node route:{}", request);
        return String.valueOf(nodeRouteManager.createNodeRoute(CreateNodeRouteParam.builder()
                .srcNodeId(request.getSrcNodeId())
                .dstNodeId(request.getDstNodeId())
                .routeType(request.getRouteType())
                .srcNetAddress(replaceNetAddressProtocol(request.getSrcNetAddress()))
                .dstNetAddress(replaceNetAddressProtocol(request.getDstNetAddress()))
                .build(), false));
    }

    @Override
    public List<NodeRouterVO> listNodeRoute() {
        String platformNodeId = envService.getPlatformNodeId();
        List<NodeRouteDO> nodeRouteDO = nodeRouteRepository.listQuery(platformNodeId);
        List<NodeRouterVO> data = nodeRouteDO.stream().map(NodeRouterVO::fromDo).collect(Collectors.toList());
        data.forEach(d -> {
            d.setSrcNode(NodeVO.from(nodeManager.getNode(d.getSrcNodeId())));
            d.setDstNode(NodeVO.from(nodeManager.getNode(d.getDstNodeId())));
            d.setStatus(getNodeRouteStatus(d.getSrcNodeId(), d.getDstNodeId()));
        });
        return data;
    }

    @Override
    public void updateNodeRouter(UpdateNodeRouterRequest request) {
        LOGGER.info("update router id:{}", request.getRouterId());
        NodeRouteDO byRouteId = nodeRouteRepository.findByRouteId(Long.parseLong(request.getRouterId()));
        if (ObjectUtils.isEmpty(byRouteId)) {
            LOGGER.info("update router address error : route not exist");
            throw SecretpadException.of(NodeRouteErrorCode.NODE_ROUTE_NOT_EXIST_ERROR);
        }
        if (nodeManager.checkNodeStatus(byRouteId.getDstNodeId())) {
            LOGGER.info("update router address error : There are unfinished tasks that cannot be modified");
            throw SecretpadException.of(NodeRouteErrorCode.NODE_ROUTE_UPDATE_UNFINISHED_ERROR);
        }
        nodeRouteManager.updateNodeRoute(UpdateNodeRouteParam.builder()
                .nodeRouteId(Long.parseLong(request.getRouterId()))
                .dstNetAddress(replaceNetAddressProtocol(request.getDstNetAddress()))
                .build());
    }

    @Override
    public NodeRouterVO getNodeRouter(Long routeId) {
        NodeRouteDO byRouteId = nodeRouteRepository.findByRouteId(routeId);
        if (ObjectUtils.isEmpty(byRouteId)) {
            LOGGER.info("get node router error : route not exist");
            throw SecretpadException.of(NodeRouteErrorCode.NODE_ROUTE_NOT_EXIST_ERROR, "route not exist");
        }
        NodeRouterVO nodeRouterVO = NodeRouterVO.fromDo(byRouteId);
        nodeRouterVO.setSrcNode(NodeVO.from(nodeManager.getNode(nodeRouterVO.getSrcNodeId())));
        nodeRouterVO.setDstNode(NodeVO.from(nodeManager.getNode(nodeRouterVO.getDstNodeId())));
        nodeRouterVO.setStatus(getNodeRouteStatus(nodeRouterVO.getSrcNodeId(), nodeRouterVO.getDstNodeId()));
        return nodeRouterVO;
    }
    @Override
    public String getNodeRouteStatus(String srcNodeNodeId, String dstNodeNodeId) {
        LOGGER.info("query node route status srcNodeId: {}, dstNodeId: {}", srcNodeNodeId, dstNodeNodeId);
        if (ObjectUtils.isEmpty(srcNodeNodeId) || ObjectUtils.isEmpty(dstNodeNodeId)) {
            return null;
        }
        nodeRouteManager.checkRouteNotExist(srcNodeNodeId, dstNodeNodeId);
        DomainRoute.RouteStatus routeStatusGo = null ,routeStatusCome = null;
        try {
            routeStatusGo = nodeRouteManager.getRouteStatus(srcNodeNodeId, dstNodeNodeId);
            routeStatusCome = nodeRouteManager.getRouteStatus(dstNodeNodeId, srcNodeNodeId);
        }catch (Exception e){
            log.error("get node route srcNode:{} dstNode:{} error", srcNodeNodeId, dstNodeNodeId);
        }
        if (!ObjectUtils.isEmpty(routeStatusCome) && !ObjectUtils.isEmpty(routeStatusGo)) {
            return routeStatusGo.getStatus().equals(routeStatusCome.getStatus()) ? routeStatusGo.getStatus() : DomainRouterConstants.DomainRouterStatusEnum.Failed.name();
        }
        return DomainRouterConstants.DomainRouterStatusEnum.Failed.name();
    }

    @Override
    public NodeRouterVO refreshRouter(Long routerId) {
        LOGGER.info("refresh router id:{}", routerId);
        return getNodeRouter(routerId);
    }

    @Override
    public boolean testAddress(String netAddress) {
        LOGGER.info("test address:{}", netAddress);
        return nodeRouteManager.testAddress(netAddress);
    }

    private String replaceNetAddressProtocol(String netAddress) {
        try {
            URL url = new URL(netAddress);
            return String.format("%s:%d", url.getHost(), url.getPort());
        } catch (MalformedURLException e) {
            log.warn("replaceNetAddressProtocol str cast URL error", e);
            return netAddress;
        }
    }

}
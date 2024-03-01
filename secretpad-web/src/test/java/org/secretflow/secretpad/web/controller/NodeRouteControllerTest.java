package org.secretflow.secretpad.web.controller;

import org.secretflow.secretpad.common.constant.DomainConstants;
import org.secretflow.secretpad.common.constant.DomainRouterConstants;
import org.secretflow.secretpad.common.errorcode.NodeRouteErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.manager.integration.model.NodeDTO;
import org.secretflow.secretpad.manager.integration.node.NodeManager;
import org.secretflow.secretpad.manager.integration.noderoute.NodeRouteManager;
import org.secretflow.secretpad.persistence.entity.NodeDO;
import org.secretflow.secretpad.persistence.entity.NodeRouteDO;
import org.secretflow.secretpad.persistence.model.GraphJobStatus;
import org.secretflow.secretpad.persistence.repository.NodeRepository;
import org.secretflow.secretpad.persistence.repository.NodeRouteRepository;
import org.secretflow.secretpad.service.model.noderoute.RouterAddressRequest;
import org.secretflow.secretpad.service.model.noderoute.RouterIdRequest;
import org.secretflow.secretpad.service.model.noderoute.UpdateNodeRouterRequest;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.secretflow.v1alpha1.common.Common;
import org.secretflow.v1alpha1.kusciaapi.Domain;
import org.secretflow.v1alpha1.kusciaapi.DomainRoute;
import org.secretflow.v1alpha1.kusciaapi.DomainRouteServiceGrpc;
import org.secretflow.v1alpha1.kusciaapi.DomainServiceGrpc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.doNothing;

/**
 * NodeRoute controller test
 *
 * @author yutu
 * @date 2023/8/31
 */
class NodeRouteControllerTest extends ControllerTest {

    @MockBean
    private NodeRepository nodeRepository;
    @MockBean
    private NodeRouteRepository nodeRouteRepository;

    @MockBean
    private DomainRouteServiceGrpc.DomainRouteServiceBlockingStub domainRouteServiceBlockingStub;

    @MockBean
    private DomainServiceGrpc.DomainServiceBlockingStub domainServiceStub;

    @MockBean
    private NodeRouteManager nodeRouteManager;

    @MockBean
    private NodeManager nodeManager;

    @Test
    void update() throws Exception {
        assertResponse(() -> {
            UpdateNodeRouterRequest request = buildUpdateNodeRouterRequest();
            Mockito.when(nodeRepository.findByNodeId(Mockito.any())).thenReturn(buildNodeDO());
            Mockito.when(nodeManager.checkNodeStatus(Mockito.anyString())).thenReturn(false);
            Mockito.when(nodeRepository.findStatusByNodeId(Mockito.anyString())).thenReturn(List.of(GraphJobStatus.SUCCEEDED.name()));
            Mockito.when(nodeRouteRepository.findByRouteId(Mockito.anyLong())).thenReturn(buildNodeRouteDO().get());
            Mockito.when(nodeRouteRepository.save(Mockito.any())).thenReturn(buildNodeRouteDO().get());
            Mockito.when(nodeRouteRepository.findBySrcNodeIdAndDstNodeId(Mockito.anyString(), Mockito.anyString())).thenReturn(buildNodeRouteDO());
            Mockito.when(domainRouteServiceBlockingStub.createDomainRoute(Mockito.any())).thenReturn(buildCreateDomainRouteResponse(0));
            Mockito.when(domainRouteServiceBlockingStub.queryDomainRoute(Mockito.any())).thenReturn(buildQueryDomainRouteResponse(0));
            Mockito.when(domainServiceStub.queryDomain(Mockito.any())).thenReturn(buildQueryDomainResponse(0));
            doNothing().when(nodeRouteRepository).updateGmtModified(Mockito.anyString(), Mockito.anyString());
            return MockMvcRequestBuilders.post(getMappingUrl(NodeRouteController.class, "update", UpdateNodeRouterRequest.class)).
                    content(JsonUtils.toJSONString(request));
        });
    }

    @Test
    void updateByUnfinishedTasks() throws Exception {
        assertErrorCode(() -> {
            UpdateNodeRouterRequest request = buildUpdateNodeRouterRequest();
            Mockito.when(nodeRepository.findStatusByNodeId(Mockito.anyString())).thenReturn(List.of(GraphJobStatus.RUNNING.name()));
            Mockito.when(nodeManager.checkNodeStatus(Mockito.anyString())).thenReturn(true);
            Mockito.when(nodeRepository.findByNodeId(Mockito.any())).thenReturn(buildNodeDO());
            Mockito.when(nodeRouteRepository.findByRouteId(Mockito.anyLong())).thenReturn(buildNodeRouteDO().get());
            Mockito.when(nodeRouteRepository.save(Mockito.any())).thenReturn(buildNodeRouteDO().get());
            Mockito.when(nodeRouteRepository.findBySrcNodeIdAndDstNodeId(Mockito.anyString(), Mockito.anyString())).thenReturn(buildNodeRouteDO());
            Mockito.when(domainRouteServiceBlockingStub.createDomainRoute(Mockito.any())).thenReturn(buildCreateDomainRouteResponse(0));
            Mockito.when(domainRouteServiceBlockingStub.queryDomainRoute(Mockito.any())).thenReturn(buildQueryDomainRouteResponse(0));
            Mockito.when(domainServiceStub.queryDomain(Mockito.any())).thenReturn(buildQueryDomainResponse(0));
            doNothing().when(nodeRouteRepository).updateGmtModified(Mockito.anyString(), Mockito.anyString());
            return MockMvcRequestBuilders.post(getMappingUrl(NodeRouteController.class, "update", UpdateNodeRouterRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, NodeRouteErrorCode.NODE_ROUTE_UPDATE_UNFINISHED_ERROR);
    }

    @Test
    void updateByRouteNotExist() throws Exception {
        assertErrorCode(() -> {
            UpdateNodeRouterRequest request = buildUpdateNodeRouterRequest();
            Mockito.when(nodeRepository.findByNodeId(Mockito.any())).thenReturn(buildNodeDO());
            Mockito.when(nodeManager.checkNodeStatus(Mockito.anyString())).thenReturn(false);
            Mockito.when(nodeRepository.findStatusByNodeId(Mockito.anyString())).thenReturn(List.of(GraphJobStatus.SUCCEEDED.name()));
            Mockito.when(nodeRouteRepository.findByRouteId(Mockito.anyLong())).thenReturn(null);
            Mockito.when(nodeRouteRepository.save(Mockito.any())).thenReturn(buildNodeRouteDO().get());
            Mockito.when(nodeRouteRepository.findBySrcNodeIdAndDstNodeId(Mockito.anyString(), Mockito.anyString())).thenReturn(buildNodeRouteDO());
            Mockito.when(domainRouteServiceBlockingStub.createDomainRoute(Mockito.any())).thenReturn(buildCreateDomainRouteResponse(0));
            Mockito.when(domainRouteServiceBlockingStub.queryDomainRoute(Mockito.any())).thenReturn(buildQueryDomainRouteResponse(0));
            Mockito.when(domainServiceStub.queryDomain(Mockito.any())).thenReturn(buildQueryDomainResponse(0));
            Mockito.doThrow(SecretpadException.of(NodeRouteErrorCode.NODE_ROUTE_NOT_EXIST_ERROR,
                    "route not exist ")).when(nodeRouteManager).updateNodeRoute(Mockito.any());
            doNothing().when(nodeRouteRepository).updateGmtModified(Mockito.anyString(), Mockito.anyString());
            return MockMvcRequestBuilders.post(getMappingUrl(NodeRouteController.class, "update", UpdateNodeRouterRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, NodeRouteErrorCode.NODE_ROUTE_NOT_EXIST_ERROR);
    }

    @Test
    void updateByNodeNotExist() throws Exception {
        assertErrorCode(() -> {
            UpdateNodeRouterRequest request = buildUpdateNodeRouterRequest();
            Mockito.when(nodeRepository.findByNodeId(Mockito.any())).thenReturn(null);
            Mockito.when(nodeManager.checkNodeStatus(Mockito.anyString())).thenReturn(false);
            Mockito.when(nodeRepository.findStatusByNodeId(Mockito.anyString())).thenReturn(List.of(GraphJobStatus.SUCCEEDED.name()));
            Mockito.when(nodeRouteRepository.findByRouteId(Mockito.anyLong())).thenReturn(buildNodeRouteDO().get());
            Mockito.when(nodeRouteRepository.save(Mockito.any())).thenReturn(buildNodeRouteDO().get());
            Mockito.when(nodeRouteRepository.findBySrcNodeIdAndDstNodeId(Mockito.anyString(), Mockito.anyString())).thenReturn(buildNodeRouteDO());
            Mockito.when(domainRouteServiceBlockingStub.createDomainRoute(Mockito.any())).thenReturn(buildCreateDomainRouteResponse(0));
            Mockito.when(domainRouteServiceBlockingStub.queryDomainRoute(Mockito.any())).thenReturn(buildQueryDomainRouteResponse(0));
            Mockito.when(domainServiceStub.queryDomain(Mockito.any())).thenReturn(buildQueryDomainResponse(0));
            Mockito.doThrow(SecretpadException.of(NodeRouteErrorCode.NODE_ROUTE_CREATE_ERROR,
                    "node do not exit")).when(nodeRouteManager).updateNodeRoute(Mockito.any());
            doNothing().when(nodeRouteRepository).updateGmtModified(Mockito.anyString(), Mockito.anyString());
            return MockMvcRequestBuilders.post(getMappingUrl(NodeRouteController.class, "update", UpdateNodeRouterRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, NodeRouteErrorCode.NODE_ROUTE_CREATE_ERROR);
    }

    @Test
    void test() throws Exception {
        assertResponse(() -> {
            RouterAddressRequest routerAddressRequest = new RouterAddressRequest();
            routerAddressRequest.setNetAddress("127.0.0.1:8080");
            Mockito.when(nodeRouteManager.testAddress(routerAddressRequest.getNetAddress())).thenReturn(true);
            return MockMvcRequestBuilders.post(getMappingUrl(NodeRouteController.class, "test", RouterAddressRequest.class))
                    .content(JsonUtils.toJSONString(routerAddressRequest));
        });
    }

    @Test
    void queryCollaborationList() throws Exception {
        assertResponse(() -> {
            Mockito.when(nodeRouteRepository.listQuery(Mockito.anyString())).thenReturn(buildCreateListNodeRouteDO());
            Mockito.when(nodeManager.getNode(Mockito.anyString())).thenReturn(NodeDTO.builder().build());
            Mockito.when(domainRouteServiceBlockingStub.queryDomainRoute(Mockito.any()))
                    .thenReturn(buildQueryDomainRouteResponse(0));
            Mockito.when(nodeRouteManager.getRouteStatus(Mockito.anyString(), Mockito.anyString()))
                    .thenReturn(buildQueryDomainRouteResponse(0).getData().getStatus());
            return MockMvcRequestBuilders.post(getMappingUrl(NodeRouteController.class, "queryCollaborationList", null))
                    .content(JsonUtils.toJSONString(buildCreateListNodeRouteDO()));
        });
    }

    @Test
    void refresh() throws Exception {
        assertResponse(() -> {
            RouterIdRequest routerIdRequest = buildRouterIdRequest();
            Mockito.when(nodeRouteRepository.findByRouteId(Mockito.any())).thenReturn(buildNodeRouteDO().get());
            Mockito.when(nodeManager.getNode(Mockito.anyString())).thenReturn(buildNodeDTO());
            Mockito.when(nodeRouteManager.getRouteStatus(Mockito.anyString(), Mockito.anyString())).thenReturn(buildRouteStatus());
            return MockMvcRequestBuilders.post(getMappingUrl(NodeRouteController.class, "refresh", RouterIdRequest.class))
                    .content(JsonUtils.toJSONString(routerIdRequest));
        });
    }

    private RouterIdRequest buildRouterIdRequest(){
        return RouterIdRequest.builder()
                .routerId("1").build();
    }

    private DomainRoute.RouteStatus buildRouteStatus() {
        return buildQueryDomainRouteResponse(0).getData().getStatus();
    }

    private NodeDTO buildNodeDTO() {
        return NodeDTO.builder()
                .nodeId("alice")
                .controlNodeId("alice")
                .nodeStatus(DomainRouterConstants.DomainRouterStatusEnum.Succeeded.name())
                .build();
    }

    private List<NodeRouteDO> buildCreateListNodeRouteDO() {
        ArrayList<NodeRouteDO> nodeRouteDOS = new ArrayList<>();
        nodeRouteDOS.add(NodeRouteDO.builder()
                .srcNodeId("alice")
                .dstNodeId("bob").build());
        return nodeRouteDOS;
    }

    private UpdateNodeRouterRequest buildUpdateNodeRouterRequest() {
        return UpdateNodeRouterRequest.builder()
                .routerId("1")
                .dstNetAddress("http://127.0.0.1:8080")
                .build();
    }


    private NodeDO buildNodeDO() {
        return NodeDO.builder().nodeId("alice").build();
    }

    private Optional<NodeRouteDO> buildNodeRouteDO() {
        return Optional.ofNullable(NodeRouteDO.builder().srcNodeId("alice").dstNodeId("bob").srcNetAddress("127.0.0.1:8080")
                .dstNetAddress("127.0.0.1:8080").id(1L).build());
    }

    private DomainRoute.CreateDomainRouteResponse buildCreateDomainRouteResponse(int code) {
        return DomainRoute.CreateDomainRouteResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(code).build()).build();
    }

    private DomainRoute.QueryDomainRouteResponse buildQueryDomainRouteResponse(int code) {
        return DomainRoute.QueryDomainRouteResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(code).build()).build();
    }

    private Domain.QueryDomainResponse buildQueryDomainResponse(Integer code) {
        return Domain.QueryDomainResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(code).build())
                .setData(Domain.QueryDomainResponseData.newBuilder()
                        .addNodeStatuses(Domain.NodeStatus.newBuilder().setStatus(DomainConstants.DomainStatusEnum.Ready.name()).build()).build())
                .build();
    }

}
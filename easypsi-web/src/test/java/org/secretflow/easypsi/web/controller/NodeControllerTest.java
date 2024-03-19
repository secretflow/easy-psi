package org.secretflow.easypsi.web.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.codec.binary.Base64;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.secretflow.easypsi.common.errorcode.NodeErrorCode;
import org.secretflow.easypsi.common.exception.EasyPsiException;
import org.secretflow.easypsi.common.util.JsonUtils;
import org.secretflow.easypsi.persistence.entity.NodeDO;
import org.secretflow.easypsi.persistence.entity.NodeRouteDO;
import org.secretflow.easypsi.persistence.repository.NodeRepository;
import org.secretflow.easypsi.persistence.repository.NodeRouteRepository;
import org.secretflow.easypsi.service.model.node.CreateNodeRequest;
import org.secretflow.easypsi.service.model.node.DeleteNodeIdRequest;
import org.secretflow.easypsi.service.model.node.DownloadNodeCertificateRequest;
import org.secretflow.easypsi.web.utils.FakerUtils;
import org.secretflow.v1alpha1.common.Common;
import org.secretflow.v1alpha1.kusciaapi.Domain;
import org.secretflow.v1alpha1.kusciaapi.DomainRoute;
import org.secretflow.v1alpha1.kusciaapi.DomainRouteServiceGrpc;
import org.secretflow.v1alpha1.kusciaapi.DomainServiceGrpc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.Optional;

/**
 * Node controller test
 *
 * @author xjn
 * @date 2023/8/2
 */
class NodeControllerTest extends ControllerTest {

    @MockBean
    private NodeRepository nodeRepository;

    @MockBean
    private DomainServiceGrpc.DomainServiceBlockingStub domainServiceStub;

    @MockBean
    private NodeRouteRepository nodeRouteRepository;

    @MockBean
    private DomainRouteServiceGrpc.DomainRouteServiceBlockingStub domainRouteServiceBlockingStub;

    private Domain.QueryDomainResponse buildQueryDomainResponse(Integer code) {
        return Domain.QueryDomainResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(code).build()).build();
    }

    private Domain.CreateDomainResponse buildCreateDomainResponse(Integer code) {
        return Domain.CreateDomainResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(code).build()).build();
    }

    private Domain.DeleteDomainResponse buildDeleteDomainResponse(Integer code) {
        return Domain.DeleteDomainResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(code).build()).build();
    }

    private NodeDO buildNodeDO() {
        return NodeDO.builder().nodeId("alice").build();
    }

    private Optional<NodeRouteDO> buildNodeRouteDO() {
        return Optional.ofNullable(NodeRouteDO.builder().srcNodeId("alice").dstNodeId("bob").srcNetAddress("127.0.0.1:8080")
                .dstNetAddress("127.0.0.1:8080").id(3L).build());
    }

    private DomainRoute.QueryDomainRouteResponse buildQueryDomainRouteResponse(int code) {
        return DomainRoute.QueryDomainRouteResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(code).build()).build();
    }

    private Optional<NodeRouteDO> buildEmptyNodeRouteDO() {
        return Optional.empty();
    }

    private DomainRoute.CreateDomainRouteResponse buildCreateDomainRouteResponse(int code) {
        return DomainRoute.CreateDomainRouteResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(code).build()).build();
    }

    private DomainRoute.DeleteDomainRouteResponse buildDeleteDomainRouteResponse(int code) {
        return DomainRoute.DeleteDomainRouteResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(code).build()).build();
    }

    private CreateNodeRequest buildCreateNodeRequest() {
        return CreateNodeRequest.builder().certText("certText")
                .dstNetAddress("127.0.0.1:8090").nodeRemark("remark")
                .certText("LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUNxRENDQVpBQ0ZIZUxQT09TOG5qUHc5dHA2ajNlTGR1bTdlZ2hNQTBHQ1NxR1NJYjNEUUVCQ3dVQU1CRXgKRHpBTkJnTlZCQU1NQmt0MWMyTnBZVEFnRncweU16RXdNVE13TmpBNU5UbGFHQTh5TURVeE1ESXlPREEyTURrMQpPVm93RGpFTU1Bb0dBMVVFQXd3RFltOWlNSUlCSWpBTkJna3Foa2lHOXcwQkFRRUZBQU9DQVE4QU1JSUJDZ0tDCkFRRUFzQ1ZXSkpOWmZHSmd3Tm5nSTMxOG1wYmdpK0FqTSs0aTBvY2ZZY2hUbG1xaGpQRVJvWWl4R2xFREdmQisKSHduNmdLVEhNV2FHZjg0UTczbit3c0hhRjl6WUlvREU4VFVLZVh0ZmlmeGJCVEYxN0VuR3B3OHNUTitYSndJaAo1SVZxZjNRNVNiWldYSWJIYlBZbVdOR28vbiszTUVQRzVrZEpJdVQvQmNHNVJQVlZOMGRkSkFNK05XSnd5ZzZJCktYSHJUMjB5K3lWeTExem1yclk0MlpvVE8wZXlzVE52TU53Wm1WSVdPMmYxOUlmNDhrWTkxVG1paDlXa3J0bUEKSWk1RVRqY09NQ2pOZEFENnpNbXptbC92T3lIRnR5UDVSNnBQTkVTTUZLNm9OSHIvVTBkdVRDaUE5TE44RE9IYwpnN2lReW1GbFk4aGVlNU1ualBBbzhPdFc3UUlEQVFBQk1BMEdDU3FHU0liM0RRRUJDd1VBQTRJQkFRQU1qd21rCktyUWw0TThKQ2cyeWNlR1dIRE5IRXBMVzhKbUQwckJVWUVqem8xSkdOMS9tOEhSSUFFd0hTaEJQSzM0bkRaVUcKY1laMDEwM1BveWNoOFk2bHRYS1h2MWRhWGdHTUNGaW01OTFQVDBEbVZIU1A4QzVvTGZFSHVaMWNySkFVNDF3bgpHOEk2Nzgwa3JmT2hZY3dLaVZoODR5ZEluczFCVGF4RWw0TlkxTVUyVmc1V0x0ZlNkWmR4akZ1eHp3WWprbk0xCktKNXBpY2tNak8ySEtkRFdHTk5KN2dNNW50RlJOUllaTkZaZEkwbGtjb1ZQNEZyekFzQkFtbDZOTEdJd2ozeHAKc2toRWlQTlRHOXA1MHU5eWZyYkhFTGIvd2ZsY25zM3NUWTlnVGVocXc1cmN2bDZYemttRWJRZkFycjczUTA0WApIQ1JSRXN5VVhGdmxVK3BHCi0tLS0tRU5EIENFUlRJRklDQVRFLS0tLS0K")
                .trust(false).build();
    }

    private DeleteNodeIdRequest buildeDeleteNodeIdRequest(){
        return DeleteNodeIdRequest.builder().routerId("3").build();
    }

    @Test
    void createNode() throws Exception {
        assertResponse(() -> {
            CreateNodeRequest request = buildCreateNodeRequest();
            Domain.QueryDomainResponse queryDomainResponse = buildQueryDomainResponse(1);
            Mockito.when(domainServiceStub.queryDomain(Mockito.any())).thenReturn(queryDomainResponse);
            Domain.CreateDomainResponse createDomainResponse = buildCreateDomainResponse(0);
            Mockito.when(domainServiceStub.createDomain(Mockito.any())).thenReturn(createDomainResponse);
            Mockito.when(nodeRepository.findByNodeId(Mockito.any())).thenReturn(buildNodeDO());
            Mockito.when(nodeRepository.existsById(Mockito.anyString())).thenReturn(false);
            Mockito.when(nodeRouteRepository.save(Mockito.any())).thenReturn(buildNodeRouteDO().get());
            Mockito.when(nodeRouteRepository.findBySrcNodeIdAndDstNodeId(Mockito.anyString(), Mockito.anyString())).thenReturn(buildEmptyNodeRouteDO());
            Mockito.when(domainRouteServiceBlockingStub.createDomainRoute(Mockito.any())).thenReturn(buildCreateDomainRouteResponse(0));
            Mockito.when(domainRouteServiceBlockingStub.queryDomainRoute(Mockito.any())).thenReturn(buildQueryDomainRouteResponse(0));
            Mockito.when(domainServiceStub.queryDomain(Mockito.any())).thenReturn(buildQueryDomainResponse(0));
            return MockMvcRequestBuilders.post(getMappingUrl(NodeController.class, "createNode", CreateNodeRequest.class)).
                    content(JsonUtils.toJSONString(request));
        });
    }

    @Test
    void createNodeByNodeAlreadyExistsException() throws Exception {
        assertErrorCode(() -> {
            CreateNodeRequest request = buildCreateNodeRequest();
            Domain.QueryDomainResponse queryDomainResponse = buildQueryDomainResponse(1);
            Mockito.when(domainServiceStub.queryDomain(Mockito.any())).thenReturn(queryDomainResponse);
            Domain.CreateDomainResponse createDomainResponse = buildCreateDomainResponse(0);
            Mockito.when(domainServiceStub.createDomain(Mockito.any())).thenReturn(createDomainResponse);
            Mockito.when(nodeRepository.findByNodeId(Mockito.any())).thenReturn(buildNodeDO());
            Mockito.when(nodeRepository.existsById(Mockito.anyString())).thenReturn(true);
            Mockito.when(nodeRouteRepository.save(Mockito.any())).thenReturn(buildNodeRouteDO().get());
            Mockito.when(nodeRouteRepository.findBySrcNodeIdAndDstNodeId(Mockito.anyString(), Mockito.anyString())).thenReturn(buildEmptyNodeRouteDO());
            Mockito.when(domainRouteServiceBlockingStub.createDomainRoute(Mockito.any())).thenReturn(buildCreateDomainRouteResponse(0));
            Mockito.when(domainRouteServiceBlockingStub.queryDomainRoute(Mockito.any())).thenReturn(buildQueryDomainRouteResponse(0));
            Mockito.when(domainServiceStub.queryDomain(Mockito.any())).thenReturn(buildQueryDomainResponse(1));
            return MockMvcRequestBuilders.post(getMappingUrl(NodeController.class, "createNode", CreateNodeRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, NodeErrorCode.NODE_CERT_CONFIG_ERROR);
    }

    @Test
    void createNodeByNodeCreateFailedException() throws Exception {
        assertErrorCode(() -> {
            CreateNodeRequest request = buildCreateNodeRequest();
            Domain.QueryDomainResponse queryDomainResponse = buildQueryDomainResponse(1);
            Mockito.when(domainServiceStub.queryDomain(Mockito.any())).thenReturn(queryDomainResponse);
            Domain.CreateDomainResponse createDomainResponse = buildCreateDomainResponse(0);
            Mockito.when(domainServiceStub.createDomain(Mockito.any())).thenThrow(EasyPsiException.of(NodeErrorCode.NODE_CREATE_ERROR));
            Mockito.when(nodeRepository.findByNodeId(Mockito.any())).thenReturn(buildNodeDO());
            Mockito.when(nodeRepository.existsById(Mockito.anyString())).thenReturn(false);
            Mockito.when(nodeRouteRepository.save(Mockito.any())).thenReturn(buildNodeRouteDO().get());
            Mockito.when(nodeRouteRepository.findBySrcNodeIdAndDstNodeId(Mockito.anyString(), Mockito.anyString())).thenReturn(buildEmptyNodeRouteDO());
            Mockito.when(domainRouteServiceBlockingStub.createDomainRoute(Mockito.any())).thenReturn(buildCreateDomainRouteResponse(0));
            Mockito.when(domainRouteServiceBlockingStub.queryDomainRoute(Mockito.any())).thenReturn(buildQueryDomainRouteResponse(0));
            Mockito.when(domainServiceStub.queryDomain(Mockito.any())).thenReturn(buildQueryDomainResponse(0));
            return MockMvcRequestBuilders.post(getMappingUrl(NodeController.class, "createNode", CreateNodeRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, NodeErrorCode.NODE_CREATE_ERROR);
    }

    @Test
    void deleteNode() throws Exception {
        assertResponseWithEmptyData(() -> {
            DeleteNodeIdRequest request = buildeDeleteNodeIdRequest();
            Mockito.when(nodeRouteRepository.findByRouteId(Long.parseLong(request.getRouterId()))).thenReturn(buildNodeRouteDO().get());
            Domain.DeleteDomainResponse deleteDomainResponse = buildDeleteDomainResponse(0);
            Mockito.when(domainServiceStub.deleteDomain(Mockito.any())).thenReturn(deleteDomainResponse);
            DomainRoute.DeleteDomainRouteResponse response = buildDeleteDomainRouteResponse(0);
            Mockito.when(domainRouteServiceBlockingStub.deleteDomainRoute(Mockito.any())).thenReturn(response);
            Mockito.when(nodeRouteRepository.findBySrcNodeIdAndDstNodeId(Mockito.anyString(),Mockito.anyString())).thenReturn(buildNodeRouteDO());
            Domain.QueryDomainResponse queryDomainResponse = buildQueryDomainResponse(0);
            Mockito.when(domainServiceStub.queryDomain(Mockito.any())).thenReturn(queryDomainResponse);
            return MockMvcRequestBuilders.post(getMappingUrl(NodeController.class, "deleteNode", DeleteNodeIdRequest.class)).
                    content(JsonUtils.toJSONString(request));
        });
    }

    @Test
    void deleteNodeByNodeNotExistsException() throws Exception {
        assertResponseWithEmptyData(() -> {
            DeleteNodeIdRequest request = buildeDeleteNodeIdRequest();
            Mockito.when(nodeRouteRepository.findByRouteId(Long.parseLong(request.getRouterId()))).thenReturn(buildNodeRouteDO().get());
            Domain.DeleteDomainResponse deleteDomainResponse = buildDeleteDomainResponse(0);
            Mockito.when(domainServiceStub.deleteDomain(Mockito.any())).thenReturn(deleteDomainResponse);
            DomainRoute.DeleteDomainRouteResponse response = buildDeleteDomainRouteResponse(0);
            Mockito.when(domainRouteServiceBlockingStub.deleteDomainRoute(Mockito.any())).thenReturn(response);
            Mockito.when(nodeRouteRepository.findBySrcNodeIdAndDstNodeId(Mockito.anyString(),Mockito.anyString())).thenReturn(buildNodeRouteDO());
            Domain.QueryDomainResponse queryDomainResponse = buildQueryDomainResponse(1);
            Mockito.when(domainServiceStub.queryDomain(Mockito.any())).thenReturn(queryDomainResponse);
            return MockMvcRequestBuilders.post(getMappingUrl(NodeController.class, "deleteNode", DeleteNodeIdRequest.class)).
                    content(JsonUtils.toJSONString(request));
        });
    }

    @Test
    void deleteNodeByNodeDeleteFailedException() throws Exception {
        assertErrorCode(() -> {
            DeleteNodeIdRequest request = buildeDeleteNodeIdRequest();
            Mockito.when(nodeRouteRepository.findByRouteId(Long.parseLong(request.getRouterId()))).thenReturn(buildNodeRouteDO().get());
            Domain.DeleteDomainResponse deleteDomainResponse = buildDeleteDomainResponse(1);
            Mockito.when(domainServiceStub.deleteDomain(Mockito.any())).thenReturn(deleteDomainResponse);
            DomainRoute.DeleteDomainRouteResponse response = buildDeleteDomainRouteResponse(0);
            Mockito.when(domainRouteServiceBlockingStub.deleteDomainRoute(Mockito.any())).thenReturn(response);
            Mockito.when(nodeRouteRepository.findBySrcNodeIdAndDstNodeId(Mockito.anyString(),Mockito.anyString())).thenReturn(buildNodeRouteDO());
            Domain.QueryDomainResponse queryDomainResponse = buildQueryDomainResponse(0);
            Mockito.when(domainServiceStub.queryDomain(Mockito.any())).thenReturn(queryDomainResponse);
            return MockMvcRequestBuilders.post(getMappingUrl(NodeController.class, "deleteNode", DeleteNodeIdRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, NodeErrorCode.NODE_DELETE_ERROR);
    }


    @Test
    void getNode() throws Exception {
        assertResponse(() -> {
            NodeDO alice = NodeDO.builder().nodeId("alice").build();
            Mockito.when(nodeRepository.findByNodeId(Mockito.any())).thenReturn(alice);
            Domain.QueryDomainResponse queryDomainResponse = buildQueryDomainResponse(0);
            Mockito.when(domainServiceStub.queryDomain(Mockito.any())).thenReturn(queryDomainResponse);
            return MockMvcRequestBuilders.post(getMappingUrl(NodeController.class, "get")).
                    content(JsonUtils.toJSONString(alice));
        });
    }

    @Test
    void getNodeByNodeNotExists() throws Exception {
        assertErrorCode(() -> {
            Mockito.when(nodeRepository.findByNodeId(Mockito.any())).thenReturn(null);
            Domain.QueryDomainResponse queryDomainResponse = buildQueryDomainResponse(0);
            Mockito.when(domainServiceStub.queryDomain(Mockito.any())).thenReturn(queryDomainResponse);
            return MockMvcRequestBuilders.post(getMappingUrl(NodeController.class, "get")).
                    content(JsonUtils.toJSONString(null));
        }, NodeErrorCode.NODE_NOT_EXIST_ERROR);
    }

    @Test
    void getNodeByKusciaNode() throws Exception {
        assertResponse(() -> {
            NodeDO alice = NodeDO.builder().nodeId("alice").build();
            Mockito.when(nodeRepository.findByNodeId(Mockito.any())).thenReturn(alice);
            Domain.QueryDomainResponse queryDomainResponse = buildQueryDomainResponse(-1);
            Mockito.when(domainServiceStub.queryDomain(Mockito.any())).thenReturn(queryDomainResponse);
            return MockMvcRequestBuilders.post(getMappingUrl(NodeController.class, "get")).
                    content(JsonUtils.toJSONString(alice));
        });
    }


    @Test
    void upload() throws Exception {
        assertMultipartResponse(() -> {
            File file = new File("mockCertText");
            byte[] bytes = Base64.decodeBase64("LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUNxRENDQVpBQ0ZIZUxQT09TOG5qUHc5dHA2ajNlTGR1bTdlZ2hNQTBHQ1NxR1NJYjNEUUVCQ3dVQU1CRXgKRHpBTkJnTlZCQU1NQmt0MWMyTnBZVEFnRncweU16RXdNVE13TmpBNU5UbGFHQTh5TURVeE1ESXlPREEyTURrMQpPVm93RGpFTU1Bb0dBMVVFQXd3RFltOWlNSUlCSWpBTkJna3Foa2lHOXcwQkFRRUZBQU9DQVE4QU1JSUJDZ0tDCkFRRUFzQ1ZXSkpOWmZHSmd3Tm5nSTMxOG1wYmdpK0FqTSs0aTBvY2ZZY2hUbG1xaGpQRVJvWWl4R2xFREdmQisKSHduNmdLVEhNV2FHZjg0UTczbit3c0hhRjl6WUlvREU4VFVLZVh0ZmlmeGJCVEYxN0VuR3B3OHNUTitYSndJaAo1SVZxZjNRNVNiWldYSWJIYlBZbVdOR28vbiszTUVQRzVrZEpJdVQvQmNHNVJQVlZOMGRkSkFNK05XSnd5ZzZJCktYSHJUMjB5K3lWeTExem1yclk0MlpvVE8wZXlzVE52TU53Wm1WSVdPMmYxOUlmNDhrWTkxVG1paDlXa3J0bUEKSWk1RVRqY09NQ2pOZEFENnpNbXptbC92T3lIRnR5UDVSNnBQTkVTTUZLNm9OSHIvVTBkdVRDaUE5TE44RE9IYwpnN2lReW1GbFk4aGVlNU1ualBBbzhPdFc3UUlEQVFBQk1BMEdDU3FHU0liM0RRRUJDd1VBQTRJQkFRQU1qd21rCktyUWw0TThKQ2cyeWNlR1dIRE5IRXBMVzhKbUQwckJVWUVqem8xSkdOMS9tOEhSSUFFd0hTaEJQSzM0bkRaVUcKY1laMDEwM1BveWNoOFk2bHRYS1h2MWRhWGdHTUNGaW01OTFQVDBEbVZIU1A4QzVvTGZFSHVaMWNySkFVNDF3bgpHOEk2Nzgwa3JmT2hZY3dLaVZoODR5ZEluczFCVGF4RWw0TlkxTVUyVmc1V0x0ZlNkWmR4akZ1eHp3WWprbk0xCktKNXBpY2tNak8ySEtkRFdHTk5KN2dNNW50RlJOUllaTkZaZEkwbGtjb1ZQNEZyekFzQkFtbDZOTEdJd2ozeHAKc2toRWlQTlRHOXA1MHU5eWZyYkhFTGIvd2ZsY25zM3NUWTlnVGVocXc1cmN2bDZYemttRWJRZkFycjczUTA0WApIQ1JSRXN5VVhGdmxVK3BHCi0tLS0tRU5EIENFUlRJRklDQVRFLS0tLS0K");
            FileOutputStream out;
            try {
                out = new FileOutputStream(file);
                out.write(bytes);
                out.close();
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            MockMultipartFile multipartFile = new MockMultipartFile("file", "bob.crt", MediaType.APPLICATION_JSON_VALUE, new FileInputStream(file));
            return MockMvcRequestBuilders.multipart(getMappingUrl(NodeController.class, "upload", MultipartFile.class))
                    .file(multipartFile).contentType(MediaType.MULTIPART_FORM_DATA_VALUE);
        });
    }


    @Test
    void download() throws Exception {
        assertResponseWithContent(() -> {
            String userAgent = FakerUtils.fake(String.class);
            DownloadNodeCertificateRequest request = FakerUtils.fake(DownloadNodeCertificateRequest.class);
            request.setNodeId("mockMvcNodeId");

            NodeDO alice = NodeDO.builder().nodeId("mockMvcNodeId").certText("mockCertText").build();
            Mockito.when(nodeRepository.findByNodeId(Mockito.any())).thenReturn(alice);

            return MockMvcRequestBuilders.post(getMappingUrl(NodeController.class, "download", HttpServletResponse.class, DownloadNodeCertificateRequest.class))
                    .header("User-Agent", userAgent).content(JsonUtils.toJSONString(request));
        });
    }
}
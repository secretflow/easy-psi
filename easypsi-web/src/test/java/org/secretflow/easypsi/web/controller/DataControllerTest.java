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

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.secretflow.easypsi.common.constant.DomainRouterConstants;
import org.secretflow.easypsi.common.dto.DownloadInfo;
import org.secretflow.easypsi.common.errorcode.DataErrorCode;
import org.secretflow.easypsi.common.errorcode.KusciaGrpcErrorCode;
import org.secretflow.easypsi.common.errorcode.NodeRouteErrorCode;
import org.secretflow.easypsi.common.exception.EasyPsiException;
import org.secretflow.easypsi.common.util.JsonUtils;
import org.secretflow.easypsi.manager.integration.data.DataManager;
import org.secretflow.easypsi.manager.integration.node.NodeManager;
import org.secretflow.easypsi.manager.integration.noderoute.NodeRouteManager;
import org.secretflow.easypsi.service.model.data.GetDataTableInformatinoRequest;
import org.secretflow.easypsi.web.utils.FakerUtils;
import org.secretflow.v1alpha1.common.Common;
import org.secretflow.v1alpha1.kusciaapi.Domain;
import org.secretflow.v1alpha1.kusciaapi.DomainRoute;
import org.slf4j.Logger;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.Mockito.*;

/**
 * DataController test
 *
 * @author guyu
 * @date 2023/8/1
 */
class DataControllerTest extends ControllerTest {

    @MockBean
    private NodeManager nodeManager;

    @MockBean
    private NodeRouteManager nodeRouteManager;

    @MockBean
    private DataManager dataManager;


    @Test
    void queryHostPath() throws Exception {
        assertResponse(() -> MockMvcRequestBuilders.post(getMappingUrl(DataController.class, "queryHostPath")));
    }

    @Test
    void queryDataVersion() throws Exception {
        assertResponse(() -> MockMvcRequestBuilders.post(getMappingUrl(DataController.class, "queryDataVersion")));
    }

    private Domain.QueryDomainResponse buildQueryDomainResponse(Integer code) {
        return Domain.QueryDomainResponse.newBuilder()
                .setStatus(Common.Status.newBuilder().setCode(code).build())
                .setData(Domain.QueryDomainResponseData.newBuilder().build())
                .build();
    }

    private DomainRoute.RouteStatus buildRouteStatus(Integer code) {
        return DomainRoute.RouteStatus.newBuilder()
                .setStatus(DomainRouterConstants.DomainRouterStatusEnum.Succeeded.name())
                .build();
    }

    @Test
    void queryDataTableInformationKusciaError() throws Exception {
        assertErrorCode(() -> {
            GetDataTableInformatinoRequest request = FakerUtils.fake(GetDataTableInformatinoRequest.class);
            Mockito.when(nodeManager.getNodeNotCheck(Mockito.anyString())).thenThrow(EasyPsiException.of(KusciaGrpcErrorCode.KUSCIA_CPMMECT_ERROR));
            doNothing().when(nodeRouteManager).checkRouteNotExist(Mockito.anyString(), Mockito.anyString());
            Mockito.when(nodeRouteManager.getRouteStatus(Mockito.anyString(), Mockito.anyString())).thenReturn(buildRouteStatus(0));

            return MockMvcRequestBuilders.post(getMappingUrl(DataController.class, "queryDataTableInformation", GetDataTableInformatinoRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, DataErrorCode.FILE_NOT_EXISTS_ERROR);
    }

    @Test
    void queryDataTableInformationNodeRouteError() throws Exception {
        assertErrorCode(() -> {
            GetDataTableInformatinoRequest request = FakerUtils.fake(GetDataTableInformatinoRequest.class);
            Mockito.when(nodeManager.getNodeNotCheck(Mockito.anyString())).thenReturn(buildQueryDomainResponse(0));
            doNothing().when(nodeRouteManager).checkRouteNotExist(Mockito.anyString(), Mockito.anyString());
            Mockito.when(nodeRouteManager.getRouteStatus(Mockito.anyString(), Mockito.anyString())).thenThrow(EasyPsiException.of(NodeRouteErrorCode.NODE_ROUTE_NOT_READY));

            return MockMvcRequestBuilders.post(getMappingUrl(DataController.class, "queryDataTableInformation", GetDataTableInformatinoRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, DataErrorCode.FILE_NOT_EXISTS_ERROR);
    }


    @Test
    public void testDownloadFileByStream() throws Exception {
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        mockResponse.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        mockResponse.setHeader(eq("Content-Disposition"), anyString());
        mockResponse.setContentLength(anyInt());
        Logger mockLogger = mock(Logger.class);
        DownloadInfo sampleDownloadInfo = DownloadInfo.builder().fileName("testFileName").filePath("/path/to/test/file").build();
        Assertions.assertThrows(EasyPsiException.class, () -> DataController.downloadFileByStream(mockResponse, sampleDownloadInfo, mockLogger));
    }
}
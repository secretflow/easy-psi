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
import org.secretflow.secretpad.common.dto.DownloadInfo;
import org.secretflow.secretpad.common.errorcode.SystemErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.common.util.UserContext;
import org.secretflow.secretpad.service.DataService;
import org.secretflow.secretpad.service.model.common.SecretPadResponse;
import org.secretflow.secretpad.service.model.data.DataSourceVO;
import org.secretflow.secretpad.service.model.data.DataTableInformationVo;
import org.secretflow.secretpad.service.model.data.DataVersionVO;
import org.secretflow.secretpad.service.model.data.GetDataTableInformatinoRequest;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Data controller
 *
 * @author xiaonan
 * @date 2023/05/25
 */
@RestController
@RequestMapping(value = "/api/v1alpha1/data")
public class DataController {
    private final static Logger LOGGER = LoggerFactory.getLogger(DataController.class);


    public final DataService dataService;

    @Autowired
    public DataController(DataService dataService) {
        this.dataService = dataService;
    }


    public static void downloadFileByStream(HttpServletResponse response, DownloadInfo downloadInfo, Logger logger) {
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + downloadInfo.getFileName());
        response.setContentLength((int) new File(downloadInfo.getFilePath()).length());
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            InputStream inputStream = new FileInputStream(downloadInfo.getFilePath());
            byte[] buf = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, len);
            }
            inputStream.close();
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw SecretpadException.of(SystemErrorCode.UNKNOWN_ERROR, e);
        }
    }

    @PostMapping(value = "/host/path")
    @InterfaceResource(interfaceCode = InterfaceResourceCode.DATA_HOST_PATH)
    public SecretPadResponse<DataSourceVO> queryHostPath() {
        return SecretPadResponse.success(dataService.queryDataPath());
    }

    @PostMapping(value = "/version")
    @InterfaceResource(interfaceCode = InterfaceResourceCode.DATA_VERSION)
    public SecretPadResponse<DataVersionVO> queryDataVersion() {
        return SecretPadResponse.success(dataService.queryDataVersion());
    }

    @PostMapping(value = "/count")
    /**
     * query data table info. for web ui
     */
    @InterfaceResource(interfaceCode = InterfaceResourceCode.DATA_COUNT)
    public SecretPadResponse<DataTableInformationVo> queryDataTableInformation(@Valid @RequestBody GetDataTableInformatinoRequest request) {
        return SecretPadResponse.success(dataService.queryDataTableInformation(request));
    }

    @PostMapping(value = "/count/kuscia")
    @InterfaceResource(interfaceCode = InterfaceResourceCode.DATA_COUNT_KUSCIA)
    /**
     * query data table info. for partner node
     */
    public SecretPadResponse<String> queryKusciaDataTableInformation(@Valid @RequestBody GetDataTableInformatinoRequest request) {
        LOGGER.info("The opposite party queries our data table information, opposite node id: {},query data table information : {}", UserContext.getUser().getOwnerId(), request.getDstDataTableName());
        DataTableInformationVo.DataTableInformation dataTabelInformation = dataService.getDataTabelInformation(request);
        return SecretPadResponse.success(JsonUtils.toJSONString(dataTabelInformation));
    }


}

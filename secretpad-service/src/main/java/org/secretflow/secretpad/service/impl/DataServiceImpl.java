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


import org.secretflow.secretpad.common.errorcode.DataErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.common.util.FileUtils;
import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.manager.integration.job.AbstractJobManager;
import org.secretflow.secretpad.manager.integration.job.JobManager;
import org.secretflow.secretpad.service.DataService;
import org.secretflow.secretpad.service.EnvService;
import org.secretflow.secretpad.service.RemoteRequestService;
import org.secretflow.secretpad.service.model.common.SecretPadResponse;
import org.secretflow.secretpad.service.model.data.DataSourceVO;
import org.secretflow.secretpad.service.model.data.DataTableInformationVo;
import org.secretflow.secretpad.service.model.data.DataVersionVO;
import org.secretflow.secretpad.service.model.data.GetDataTableInformatinoRequest;

import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Data service implementation class
 *
 * @author : xiaonan.fhn
 * @date 2023/5/25
 */
@Service
public class DataServiceImpl implements DataService {

    private final static Logger LOGGER = LoggerFactory.getLogger(DataService.class);

    private final String VERSION_DEFAULT = "--";

    @Value("${secretpad.data.host-path:/home/kuscia/p2p/}")
    private String dataHostPath;

    @Value("${secretpad.version.secretpad-image:--}")
    private String secretpadImage;

    @Value("${secretpad.version.kuscia-image:--}")
    private String kusciaImage;

    @Value("${secretpad.version.secretflow-image:--}")
    private String secretflowImage;

    @Value("${secretpad.gateway}")
    private String gateway;

    private static final String QUERY_DATA_TABLE_INFORMATION = "/api/v1alpha1/data/count/kuscia";

    public static HashMap<String, String> dataTableCountCache = new HashMap<>();

    @Autowired
    private RemoteRequestService remoteRequestService;

    @Autowired
    private EnvService envService;

    @Autowired
    private AbstractJobManager jobManager;

    @Override
    public DataSourceVO queryDataPath() {
        return DataSourceVO.builder().path(dataHostPath).build();
    }

    @Override
    public DataVersionVO queryDataVersion() {
        return DataVersionVO.builder()
                .secretpadTag(getLastPart(secretpadImage, ":"))
                .kusciaTag(getLastPart(kusciaImage, ":"))
                .secretflowTag(getLastPart(secretflowImage, ":"))
                .build();
    }

    @Override
    public DataTableInformationVo queryDataTableInformation(GetDataTableInformatinoRequest request) {
        remoteRequestService.checkBothSidesNodeRouteIsReady(envService.getPlatformNodeId(), request.getDstNodeId());
        checkDataTableName(request);
        LOGGER.info("Asynchronously request the data table of the other party, request:{}", request);
        Future<SecretPadResponse> dataTableInformationFuture = remoteRequestService.asyncSendPostJson(request, request.getDstNodeId(), JobManager.HTTP_HEADER + gateway + QUERY_DATA_TABLE_INFORMATION);

        DataTableInformationVo.DataTableInformation srcDataTableInformation = getDataTabelInformation(request);
        LOGGER.debug("Query our data table information, data table information: {}", srcDataTableInformation);
        DataTableInformationVo.DataTableInformation dstDataTableInformation;
        try {
            SecretPadResponse secretPadResponse = dataTableInformationFuture.get();
            LOGGER.debug("Query data information response: {},msg: {}", secretPadResponse.getData(), secretPadResponse.getStatus().getMsg());
            dstDataTableInformation = JsonUtils.toJavaObject(secretPadResponse.getData().toString(), DataTableInformationVo.DataTableInformation.class);
            LOGGER.debug("Query opposite data table information, data table information: {}", dstDataTableInformation);
            dataTableCountCache.put(spliceNodeTable(dstDataTableInformation.getNodeId(), request.getDstDataTableName()), dstDataTableInformation.getDataTableCount());
        } catch (ExecutionException | InterruptedException e) {
            LOGGER.error("Querying opposite data table information is abnormal，opposite node id: {} error: {}", request.getDstDataTableName(), e.getMessage());
            throw SecretpadException.of(DataErrorCode.QUERY_DATA_ERROR, request.getDstDataTableName());
        }
        return DataTableInformationVo.builder()
                .srcDataTableInformation(srcDataTableInformation)
                .dstDataTableInformation(dstDataTableInformation).build();
    }


    @Override
    public DataTableInformationVo.DataTableInformation getDataTabelInformation(GetDataTableInformatinoRequest request) {
        checkDataTableName(request);
        String dataTableName;
        String platformNodeId = envService.getPlatformNodeId();
        if (!ObjectUtils.isEmpty(platformNodeId) && platformNodeId.equals(request.getDstNodeId())) {
            dataTableName = request.getDstDataTableName();
        } else {
            dataTableName = request.getSrcDataTableName();
        }
        String csvFilePath = JobManager.CSV_DATA_PATH + File.separator + dataTableName;
        Future<String> dataLineFuture = countLinesByCommand(csvFilePath, dataTableName);
        String dataLine;
        try {
            dataLine = dataLineFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("data table read error, data table: {}", dataTableName);
            throw SecretpadException.of(DataErrorCode.QUERY_DATA_ERROR, dataTableName);
        }
        long lineCount = Long.parseLong(Objects.requireNonNull(dataLine));
        String dataInterval = getDataInterval(lineCount);
        LOGGER.info("data table name: {}, data interval: {}", dataTableName, dataInterval);
        dataTableCountCache.put(spliceNodeTable(platformNodeId, dataTableName), dataInterval);
        return DataTableInformationVo.DataTableInformation.builder()
                .nodeId(platformNodeId)
                .dataTableName(dataTableName)
                .dataTableCount(dataInterval)
                .build();
    }


    private String getLastPart(String image, String separator) {
        if (ObjectUtils.isEmpty(separator) || VERSION_DEFAULT.equals(image)) {
            return VERSION_DEFAULT;
        }
        String[] split = image.split(separator);
        if (split.length < 1) {
            return VERSION_DEFAULT;
        }

        return processString(split[split.length - 1]);
    }

    private String processString(String str) {
        if (str.startsWith("V")) {
            return "v" + str.substring(1);
        } else if (!str.startsWith("v")) {
            return "v" + str;
        } else {
            return str;
        }
    }

    @Async
    private Future<String> countLinesByCommand(String filePath, String fileName) {
        BufferedReader reader = null;
        try {
            Process process = new ProcessBuilder("awk", "NR == 100000001 { print \"100000001\"; exit } END { if (NR < 100000001) print NR } ", filePath).start();

            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            return new AsyncResult<>(reader.readLine());

        } catch (IOException e) {
            LOGGER.error("data table read error, data table: {}", fileName);
            throw SecretpadException.of(DataErrorCode.QUERY_DATA_ERROR, fileName);
        } finally {
            if (!ObjectUtils.isEmpty(reader)) {
                try {
                    reader.close();
                } catch (IOException e) {
                    LOGGER.error("Close stream error", e);
                }
            }
        }
    }

    private String getDataInterval(Long dataCount) {
        if (dataCount < 0) {
            return "L0";
        } else if (dataCount < 10000) {
            return "L1";
        } else if (dataCount < 100000) {
            return "L2";
        } else if (dataCount < 1000000) {
            return "L3";
        } else if (dataCount < 10000000) {
            return "L4";
        } else if (dataCount < 100000000) {
            return "L5";
        } else {
            return "L6";
        }
    }

    public static String spliceNodeTable(String node, String tableName) {
        return node + "_" + tableName;
    }

    private void checkDataTableName(GetDataTableInformatinoRequest request) {
        LOGGER.info("verify the data table exists check table name:{}", request.getSrcDataTableName());
        FileUtils.fileNameCheck(request.getSrcDataTableName());
        LOGGER.info("verify the data table exists check table name:{}", request.getDstDataTableName());
        FileUtils.fileNameCheck(request.getDstDataTableName());
        List<String> tableNames = FileUtils.traverseDirectories(new File(JobManager.CSV_DATA_PATH), ".csv", FileUtils.FILE_NAME);
        if (tableNames == null) {
            LOGGER.error("file to be verified not found:{}", request.getSrcDataTableName());
            throw SecretpadException.of(DataErrorCode.FILE_NOT_EXISTS_ERROR, request.getSrcDataTableName());
        }
        if (envService.getPlatformNodeId().equals(request.getDstNodeId())) {
            if (!tableNames.contains(request.getDstDataTableName())) {
                LOGGER.error("file to be verified not found:{}", request.getDstDataTableName());
                throw SecretpadException.of(DataErrorCode.FILE_NOT_EXISTS_ERROR, request.getDstDataTableName());
            }
        } else {
            if (!tableNames.contains(request.getSrcDataTableName())) {
                LOGGER.error("file to be verified not found:{}", request.getSrcDataTableName());
                throw SecretpadException.of(DataErrorCode.FILE_NOT_EXISTS_ERROR, request.getSrcDataTableName());
            }
        }
    }
}

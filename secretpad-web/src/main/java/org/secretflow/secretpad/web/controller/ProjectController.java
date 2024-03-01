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

import org.secretflow.secretpad.common.annotation.resource.DataResource;
import org.secretflow.secretpad.common.annotation.resource.InterfaceResource;
import org.secretflow.secretpad.common.constant.resource.DataResourceType;
import org.secretflow.secretpad.common.constant.resource.InterfaceResourceCode;
import org.secretflow.secretpad.common.dto.DownloadInfo;
import org.secretflow.secretpad.common.util.UserContext;
import org.secretflow.secretpad.service.ProjectService;
import org.secretflow.secretpad.service.model.common.SecretPadPageResponse;
import org.secretflow.secretpad.service.model.common.SecretPadResponse;
import org.secretflow.secretpad.service.model.graph.GrapDataHeaderVO;
import org.secretflow.secretpad.service.model.graph.GrapDataTableVO;
import org.secretflow.secretpad.service.model.graph.GraphNodeJobLogsVO;
import org.secretflow.secretpad.service.model.project.*;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Project controller
 *
 * @author xiaonan
 * @date 2023/6/15
 */
@RestController
@RequestMapping(value = "/api/v1alpha1/project")
public class ProjectController {

    private final static Logger LOGGER = LoggerFactory.getLogger(ProjectController.class);

    @Autowired
    private ProjectService projectService;

    /**
     * Paging list project job list api
     *
     * @param request list project job request
     * @return successful SecretPadResponse with paging project job view object
     */
    @ResponseBody
    @PostMapping(value = "/job/list")
    @Operation(summary = "project job list", description = "project job list")
    @DataResource(field = "projectId", resourceType = DataResourceType.PROJECT_ID)
    @InterfaceResource(interfaceCode = InterfaceResourceCode.PRJ_JOB_LIST)
    public SecretPadResponse<SecretPadPageResponse<ProjectJobListVO>> listJob(@Valid @RequestBody ListProjectJobRequest request) {
        return SecretPadResponse.success(projectService.listProjectJob(request));
    }

    /**
     * Query project job detail api
     *
     * @param request get project job request
     * @return successful SecretPadResponse with project job view object
     */
    @ResponseBody
    @PostMapping(value = "/job/get")
    @Operation(summary = "project job detail", description = "project job detail")
    @DataResource(field = "projectId", resourceType = DataResourceType.PROJECT_ID)
    @InterfaceResource(interfaceCode = InterfaceResourceCode.PRJ_JOB_GET)
    public SecretPadResponse<ProjectJobVO> getJob(@Valid @RequestBody GetProjectJobRequest request) {
        return SecretPadResponse.success(projectService.getProjectJob(request));
    }

    /**
     * Stop project job api
     *
     * @param request stop project job task request
     * @return successful SecretPadResponse with null data
     */
    @ResponseBody
    @PostMapping(value = "/job/stop")
    @DataResource(field = "projectId", resourceType = DataResourceType.PROJECT_ID)
    @InterfaceResource(interfaceCode = InterfaceResourceCode.PRJ_JOB_STOP)
    @Operation(summary = "stop project job", description = "stop project job")
    public SecretPadResponse<Void> stopJob(@Valid @RequestBody StopProjectJobTaskRequest request) {
        projectService.stopProjectJob(request);
        return SecretPadResponse.success();
    }

    /**
     * Query project job logs
     *
     * @return successful SecretPadResponse with project job logs list view object
     */
    @PostMapping(value = "/job/logs")
    @InterfaceResource(interfaceCode = InterfaceResourceCode.PRJ_JOB_LOGS)
    @Operation(summary = "project job logs", description = "project job logs")
    public SecretPadResponse<GraphNodeJobLogsVO> getProjectLogs(@Valid @RequestBody GetProjectJobLogRequest request) {
        return SecretPadResponse.success(projectService.getProjectJobInFeilLogs(request));
    }

    /**
     * Query csv data table
     *
     * @return successful SecretPadResponse with csv data table list view object
     */
    @PostMapping(value = "/data/table")
    @InterfaceResource(interfaceCode = InterfaceResourceCode.PRJ_DATA_HEADER)
    @Operation(summary = "query data table", description = "query data table")
    public SecretPadResponse<GrapDataTableVO> getDataTable(@Valid @RequestBody GetProjectJobTableRequest request) {
        return SecretPadResponse.success(projectService.getDataTable(request, request.isCheckTableExist()));
    }

    /**
     * Query csv data header
     *
     * @return successful SecretPadResponse with csv data header list view object
     */
    @PostMapping(value = "/data/header")
    @InterfaceResource(interfaceCode = InterfaceResourceCode.PRJ_DATA_HEADER)
    @Operation(summary = "query data header", description = "query data header")
    public SecretPadResponse<GrapDataHeaderVO> getDataHeader(@Valid @RequestBody GetProjectJobDataHeaderRequest request) {
        return SecretPadResponse.success(projectService.getDataHeader(request, request.isCheckDataHeaderExist()));
    }

    /**
     * Delete project job api
     *
     * @param request delete project job task request
     * @return successful SecretPadResponse with null data
     */
    @ResponseBody
    @PostMapping(value = "/job/delete")
    @DataResource(field = "projectId", resourceType = DataResourceType.PROJECT_ID)
    @InterfaceResource(interfaceCode = InterfaceResourceCode.PRJ_JOB_DELETE)
    @Operation(summary = "delete project job", description = "delete project job")
    public SecretPadResponse<Void> deleteJob(@Valid @RequestBody DeleteProjectJobTaskRequest request) {
        projectService.deleteProjectJob(request);
        return SecretPadResponse.success();
    }

    /**
     * Create project job api
     *
     * @param request create project job task request
     * @return successful SecretPadResponse with CreateProjectJobVO
     */
    @ResponseBody
    @PostMapping(value = "/job/create")
    @DataResource(field = "projectId", resourceType = DataResourceType.PROJECT_ID)
    @InterfaceResource(interfaceCode = InterfaceResourceCode.PRJ_JOB_CREATE)
    @Operation(summary = "create project job", description = "create project job")
    public SecretPadResponse<CreateProjectJobVO> createJob(@Valid @RequestBody CreateProjectJobRequest request) {
        return SecretPadResponse.success(projectService.createJob(request));
    }

    /**
     * Agree project job api
     *
     * @param request agree project job task request
     * @return successful SecretPadResponse with null data
     */
    @ResponseBody
    @PostMapping(value = "/job/agree")
    @DataResource(field = "projectId", resourceType = DataResourceType.PROJECT_ID)
    @InterfaceResource(interfaceCode = InterfaceResourceCode.PRJ_JOB_AGREE)
    @Operation(summary = "agree project job", description = "agree project job")
    public SecretPadResponse<Void> agreeJob(@Valid @RequestBody AgreeProjectJobTaskRequest request) {
        projectService.agreeJob(request);
        return SecretPadResponse.success();
    }

    /**
     * Pause project job api
     *
     * @param request pause project job task request
     * @return successful SecretPadResponse with null data
     */
    @ResponseBody
    @PostMapping(value = "/job/pause")
    @DataResource(field = "projectId", resourceType = DataResourceType.PROJECT_ID)
    @InterfaceResource(interfaceCode = InterfaceResourceCode.PRJ_JOB_PAUSE)
    @Operation(summary = "pause project job", description = "pause project job")
    public SecretPadResponse<Void> pauseJob(@Valid @RequestBody StopProjectJobTaskRequest request) {
        projectService.pauseJob(request);
        return SecretPadResponse.success();
    }

    /**
     * Continue project job api
     *
     * @param request Continue project job task request
     * @return successful SecretPadResponse with null data
     */
    @ResponseBody
    @PostMapping(value = "/job/continue")
    @DataResource(field = "projectId", resourceType = DataResourceType.PROJECT_ID)
    @InterfaceResource(interfaceCode = InterfaceResourceCode.PRJ_JOB_CONTINUE)
    @Operation(summary = "continue project job", description = "continue project job")
    public SecretPadResponse<Void> continueJob(@Valid @RequestBody StopProjectJobTaskRequest request) {
        projectService.continueJob(request);
        return SecretPadResponse.success();
    }

    /**
     * Create project job api
     *
     * @param request create project job task request
     * @return successful SecretPadResponse with null data
     */
    @ResponseBody
    @PostMapping(value = "/job/create/kuscia")
    @DataResource(field = "projectId", resourceType = DataResourceType.PROJECT_ID)
    @InterfaceResource(interfaceCode = InterfaceResourceCode.PRJ_JOB_CREATE_KUSCIA)
    @Operation(summary = "create project job", description = "create project job")
    public SecretPadResponse<Void> createKusciaJob(@Valid @RequestBody CreateProjectJobTaskRequest request) {
        projectService.createKusciaJob(request);
        return SecretPadResponse.success();
    }

    /**
     * Stop project job api
     *
     * @param request stop project job task request
     * @return successful SecretPadResponse with null data
     */
    @ResponseBody
    @PostMapping(value = "/job/stop/kuscia")
    @DataResource(field = "projectId", resourceType = DataResourceType.PROJECT_ID)
    @InterfaceResource(interfaceCode = InterfaceResourceCode.PRJ_JOB_STOP_KUSCIA)
    @Operation(summary = "stop project job", description = "stop project job")
    public SecretPadResponse<Void> stopKusciaJob(@Valid @RequestBody StopProjectJobTaskRequest request) {
        projectService.stopKusciaJob(request);
        return SecretPadResponse.success();
    }

    /**
     * Continue project job api
     *
     * @param request Continue project job task request
     * @return successful SecretPadResponse with null data
     */
    @ResponseBody
    @PostMapping(value = "/job/continue/kuscia")
    @DataResource(field = "projectId", resourceType = DataResourceType.PROJECT_ID)
    @InterfaceResource(interfaceCode = InterfaceResourceCode.PRJ_JOB_CONTINUE_KUSCIA)
    @Operation(summary = "continue project job", description = "continue project job")
    public SecretPadResponse<Void> continueKusciaJob(@Valid @RequestBody StopProjectJobTaskRequest request) {
        projectService.continueKusciaJob(request);
        return SecretPadResponse.success();
    }

    /**
     * Pause project job api
     *
     * @param request pause project job task request
     * @return successful SecretPadResponse with null data
     */
    @ResponseBody
    @PostMapping(value = "/job/pause/kuscia")
    @DataResource(field = "projectId", resourceType = DataResourceType.PROJECT_ID)
    @InterfaceResource(interfaceCode = InterfaceResourceCode.PRJ_JOB_PAUSE_KUSCIA)
    @Operation(summary = "pause project job", description = "pause project job")
    public SecretPadResponse<Void> pauseKusciaJob(@Valid @RequestBody StopProjectJobTaskRequest request) {
        projectService.pauseKusciaJob(request);
        return SecretPadResponse.success();
    }

    /**
     * Reject project job api
     *
     * @param request Reject project job task request
     * @return successful SecretPadResponse with null data
     */
    @ResponseBody
    @PostMapping(value = "/job/reject")
    @DataResource(field = "projectId", resourceType = DataResourceType.PROJECT_ID)
    @InterfaceResource(interfaceCode = InterfaceResourceCode.PRJ_JOB_REJECT)
    @Operation(summary = "reject project job", description = "reject project job")
    public SecretPadResponse<Void> rejectJob(@Valid @RequestBody RejectProjectJobTaskRequest request) {
        projectService.rejectJob(request);
        return SecretPadResponse.success();
    }

    /**
     * Download project result api
     *
     * @param request  download data request
     * @return successful SecretPadResponse with hash string
     */
    @ResponseBody
    @PostMapping(value = "/job/result/download")
    @InterfaceResource(interfaceCode = InterfaceResourceCode.PRJ_JOB_RESULT_DOWNLOAD)
    public SecretPadResponse<String> downloadProjectResult(@Valid @RequestBody DownloadProjectResult request) {
        return SecretPadResponse.success(projectService.downloadProjectResult(request.getJobId()));
    }

    /**
     * Download project result api
     *
     * @param response http servlet response
     * @param request  download data request
     * @return successful SecretPadResponse with null data
     */
    @ResponseBody
    @GetMapping(value = "/job/result/download")
    @InterfaceResource(interfaceCode = InterfaceResourceCode.PRJ_JOB_RESULT_DOWNLOAD)
    public void getloadProjectResult(HttpServletResponse response, GetloadProjectResult request) {
        DownloadInfo downloadInfo = projectService.getloadProjectResult(request.getHash());
        DataController.downloadFileByStream(response, downloadInfo, LOGGER);
    }

    /**
     * Query project edge job list
     *
     * @return project job view object list
     */
    @ResponseBody
    @PostMapping(value = "/edge/job/list")
    @InterfaceResource(interfaceCode = InterfaceResourceCode.PRJ_EDGE_JOB_LIST)
    @Operation(summary = "query project edge job list", description = "query project edge job list")
    public SecretPadResponse<List<ProjectJobVO>> queryEdgeProjectJobs() {
        LOGGER.info("requestNodeId = {}", UserContext.getUser().getOwnerId());
        List<ProjectJobVO> projectJobs = projectService.queryEdgeProjectJobs(UserContext.getUser().getOwnerId());
        return SecretPadResponse.success(projectJobs);
    }
}

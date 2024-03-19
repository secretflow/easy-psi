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

import org.secretflow.easypsi.common.constant.DatabaseConstants;
import org.secretflow.easypsi.common.errorcode.JobErrorCode;
import org.secretflow.easypsi.common.util.DateTimes;
import org.secretflow.easypsi.common.util.JsonUtils;
import org.secretflow.easypsi.manager.integration.job.JobManager;
import org.secretflow.easypsi.manager.integration.noderoute.NodeRouteManager;
import org.secretflow.easypsi.persistence.entity.NodeRouteDO;
import org.secretflow.easypsi.persistence.entity.ProjectJobDO;
import org.secretflow.easypsi.persistence.model.GraphJobStatus;
import org.secretflow.easypsi.persistence.repository.NodeRouteRepository;
import org.secretflow.easypsi.persistence.repository.ProjectJobRepository;
import org.secretflow.easypsi.service.RemoteRequestService;

import org.secretflow.easypsi.service.model.common.EasyPsiResponse;
import org.secretflow.easypsi.web.utils.FakerUtils;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.secretflow.easypsi.service.model.project.*;
import org.secretflow.v1alpha1.common.Common;
import org.secretflow.v1alpha1.kusciaapi.DomainRoute;
import org.secretflow.v1alpha1.kusciaapi.Job;
import org.secretflow.v1alpha1.kusciaapi.JobServiceGrpc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;

/**
 * Project controller test
 *
 * @author yansi
 * @date 2023/7/24
 */
class ProjectControllerTest extends ControllerTest {

    private static final String PROJECT_ID = "projectagdasvacaghyhbvscvyjnba";
    private static final String JOB_ID = "op-psiv3-dabgvasfasdasdas";

    @MockBean
    private ProjectJobRepository projectJobRepository;

    @MockBean
    private JobServiceGrpc.JobServiceBlockingStub jobStub;

    @MockBean
    private NodeRouteManager nodeRouteManager;

    @MockBean
    private NodeRouteRepository nodeRouteRepository;

    @MockBean
    private RemoteRequestService remoteRequestService;

    @MockBean
    private JobManager jobManager;


    private ProjectJobDO buildProjectJobDO(boolean isTaskEmpty) {
        ProjectJobDO.UPK upk = new ProjectJobDO.UPK();
        upk.setProjectId(PROJECT_ID);
        upk.setJobId(JOB_ID);
        ProjectJobDO projectJobDO = ProjectJobDO.builder().upk(upk).build();
        if (!isTaskEmpty) {
            projectJobDO.setInitiatorConfig("{\"advancedJoinType\":\"ADVANCED_JOIN_TYPE_INNER_JOIN\",\"leftSide\":\"psi1\",\"datatableCount\":\"L5\",\"disableAlignment\":false,\"inputConfig\":{\"path\":\"/home/kuscia/var/storage/data/testdata_1000w_50_sender.csv\",\"type\":\"IO_TYPE_FILE_CSV\"},\"keys\":[\"id0\"],\"linkConfig\":{\"httpTimeoutMs\":\"30\",\"recvTimeoutMs\":\"30\"},\"nodeId\":\"psi1\",\"outputConfig\":{\"path\":\"/home/kuscia/var/storage/data/result/gaddayeb/ss.csv\",\"type\":\"IO_TYPE_FILE_CSV\"},\"outputDifference\":false,\"protocolConfig\":{\"broadcastResult\":true,\"protocol\":\"PROTOCOL_RR22\",\"role\":\"ROLE_SENDER\",\"rr22Config\":{\"bucketSize\":\"1048576\",\"lowCommMode\":false}},\"recoveryConfig\":{\"enabled\":false,\"folder\":\"/home/kuscia/var/storage/data/tmp/gaddayeb/\"},\"skipDuplicatesCheck\":true}");
        } else {
            projectJobDO.setInitiatorConfig("");
        }
        projectJobDO.setGmtCreate(DateTimes.utcFromRfc3339("2023-08-02T08:30:15.235+08:00"));
        projectJobDO.setGmtModified(DateTimes.utcFromRfc3339("2023-08-02T16:30:15.235+08:00"));
        projectJobDO.setInitiatorNodeId("alice");
        projectJobDO.setPartnerNodeId("bob");
        projectJobDO.setPartnerConfig("{\"advancedJoinType\":\"ADVANCED_JOIN_TYPE_INNER_JOIN\",\"leftSide\":\"psi1\",\"datatableCount\":\"L5\",\"disableAlignment\":false,\"inputConfig\":{\"path\":\"/home/kuscia/var/storage/data/testdata_1000w_50_sender.csv\",\"type\":\"IO_TYPE_FILE_CSV\"},\"keys\":[\"id0\"],\"linkConfig\":{\"httpTimeoutMs\":\"30\",\"recvTimeoutMs\":\"30\"},\"nodeId\":\"psi1\",\"outputConfig\":{\"path\":\"/home/kuscia/var/storage/data/result/gaddayeb/ss.csv\",\"type\":\"IO_TYPE_FILE_CSV\"},\"outputDifference\":false,\"protocolConfig\":{\"broadcastResult\":true,\"protocol\":\"PROTOCOL_RR22\",\"role\":\"ROLE_SENDER\",\"rr22Config\":{\"bucketSize\":\"1048576\",\"lowCommMode\":false}},\"recoveryConfig\":{\"enabled\":false,\"folder\":\"/home/kuscia/var/storage/data/tmp/gaddayeb/\"},\"skipDuplicatesCheck\":true}");
        return projectJobDO;
    }

    private Future<ProjectJobDO> buildFutureProjectJobDO() {
        ProjectJobDO jobOpt = buildProjectJobDO(true);
        return new AsyncResult<>(jobOpt);
    }

    private NodeRouteDO buildNodeRouteDO() {
        return NodeRouteDO.builder().build();
    }

    private DomainRoute.RouteStatus buildRouteStatus() {
        return DomainRoute.RouteStatus.newBuilder().setStatus("Succeeded").build();
    }

    private EasyPsiResponse buildEasyPsiResponse() {
        return EasyPsiResponse.success("Succeeded");
    }

    private ProjectJobDO buildProjectJobDO() {
        return ProjectJobDO.builder().build();
    }

    private GetProjectJobTableRequest buildGetProjectJobTableRequest(boolean check) {
        GetProjectJobTableRequest getProjectJobTableRequest = new GetProjectJobTableRequest();
        getProjectJobTableRequest.setCheckTableExist(check);
        return getProjectJobTableRequest;
    }

    private GetProjectJobDataHeaderRequest buildGetProjectJobDataHeaderRequest(String tableName, List<String> tableHeader) {
        GetProjectJobDataHeaderRequest request = new GetProjectJobDataHeaderRequest();
        request.setCheckDataHeaderExist(true);
        request.setTableName(tableName);
        request.setCheckTableHeader(tableHeader);
        return request;
    }

    private GetProjectJobDataHeaderRequest buildGetProjectJobDataHeaderRequest(String tableName) {
        GetProjectJobDataHeaderRequest request = new GetProjectJobDataHeaderRequest();
        request.setCheckDataHeaderExist(false);
        request.setTableName(tableName);
        return request;
    }

    @Test
    void listJob() throws Exception {
        assertResponse(() -> {
            ListProjectJobRequest request = FakerUtils.fake(ListProjectJobRequest.class);
            request.setPageNum(1);
            request.setPageSize(10);
            request.setSortKey(DatabaseConstants.GMT_CREATE);
            Pageable pageable = PageRequest.of(request.getPageNum() - 1, request.getPageSize(), Sort.Direction.DESC, request.getSortKey());

            Page<ProjectJobDO> page = new PageImpl<>(Arrays.asList(buildProjectJobDO(false)));
            Mockito.when(projectJobRepository.findAll(Specification.anyOf(), pageable)).thenReturn(page);

            return MockMvcRequestBuilders.post(getMappingUrl(ProjectController.class, "listJob", ListProjectJobRequest.class)).
                    content(JsonUtils.toJSONString(request));
        });
    }

    @Test
    void getJob() throws Exception {
        assertResponse(() -> {
            GetProjectJobRequest request = FakerUtils.fake(GetProjectJobRequest.class);

            Mockito.when(projectJobRepository.findByJobId(Mockito.any())).thenReturn(Optional.of(buildProjectJobDO(false)));

            return MockMvcRequestBuilders.post(getMappingUrl(ProjectController.class, "getJob", GetProjectJobRequest.class)).
                    content(JsonUtils.toJSONString(request));
        });
    }

    @Test
    void getJobByProjectNotExistsException() throws Exception {
        assertErrorCode(() -> {
            GetProjectJobRequest request = FakerUtils.fake(GetProjectJobRequest.class);

            return MockMvcRequestBuilders.post(getMappingUrl(ProjectController.class, "getJob", GetProjectJobRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, JobErrorCode.PROJECT_JOB_NOT_EXISTS);
    }

    @Test
    void getJobByProjectJobNotExistsException() throws Exception {
        assertErrorCode(() -> {
            GetProjectJobRequest request = FakerUtils.fake(GetProjectJobRequest.class);

            return MockMvcRequestBuilders.post(getMappingUrl(ProjectController.class, "getJob", GetProjectJobRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, JobErrorCode.PROJECT_JOB_NOT_EXISTS);
    }

    @Test
    void stopJob() throws Exception {
        assertResponseWithEmptyData(() -> {
            StopProjectJobTaskRequest request = FakerUtils.fake(StopProjectJobTaskRequest.class);

            ProjectJobDO projectJobDO = buildProjectJobDO(false);
            projectJobDO.setHostNodeId("kuscia-system");
            Mockito.when(projectJobRepository.findByJobId(Mockito.any())).thenReturn(Optional.of(projectJobDO));
            Mockito.when(nodeRouteManager.getRouteStatus(Mockito.anyString(), Mockito.anyString())).thenReturn(buildRouteStatus());
            Mockito.when(nodeRouteRepository.findBySrcNodeIdAndDstNodeId(Mockito.anyString(), Mockito.anyString())).thenReturn(Optional.of(buildNodeRouteDO()));
            Mockito.when(remoteRequestService.checkBothSidesNodeRouteIsReady(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
            Mockito.when(remoteRequestService.sendPostJson(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(buildEasyPsiResponse());

            Mockito.when(jobStub.deleteJob(Mockito.any())).thenReturn(org.secretflow.v1alpha1.kusciaapi.Job.DeleteJobResponse.newBuilder().build());
            return MockMvcRequestBuilders.post(getMappingUrl(ProjectController.class, "stopJob", StopProjectJobTaskRequest.class)).
                    content(JsonUtils.toJSONString(request));
        });
    }

    @Test
    void stopJobByProjectJobNotExistsException() throws Exception {
        assertErrorCode(() -> {
            StopProjectJobTaskRequest request = FakerUtils.fake(StopProjectJobTaskRequest.class);


            return MockMvcRequestBuilders.post(getMappingUrl(ProjectController.class, "stopJob", StopProjectJobTaskRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, JobErrorCode.PROJECT_JOB_NOT_EXISTS);
    }

    @Test
    void getProjectLogs() throws Exception {
        assertResponse(() -> {
            GetProjectJobLogRequest request = FakerUtils.fake(GetProjectJobLogRequest.class);

            ProjectJobDO projectJobDO = buildProjectJobDO(false);
            projectJobDO.setStatus(GraphJobStatus.RUNNING);
            Mockito.when(projectJobRepository.findByJobId(Mockito.any())).thenReturn(Optional.of(projectJobDO));
            Mockito.when(nodeRouteManager.getRouteStatus(Mockito.anyString(), Mockito.anyString())).thenReturn(buildRouteStatus());
            Mockito.when(nodeRouteRepository.findBySrcNodeIdAndDstNodeId(Mockito.anyString(), Mockito.anyString())).thenReturn(Optional.of(buildNodeRouteDO()));
            Mockito.when(remoteRequestService.checkBothSidesNodeRouteIsReady(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
            Mockito.when(remoteRequestService.sendPostJson(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(buildEasyPsiResponse());

            return MockMvcRequestBuilders.post(getMappingUrl(ProjectController.class, "getProjectLogs", GetProjectJobLogRequest.class)).
                    content(JsonUtils.toJSONString(request));
        });
    }


    @Test
    void getDataTableErrorException() throws Exception {
        assertErrorCode(() -> {
            GetProjectJobTableRequest request = FakerUtils.fake(GetProjectJobTableRequest.class);
            request.setTableName(null);

            return MockMvcRequestBuilders.post(getMappingUrl(ProjectController.class, "getDataTable", GetProjectJobTableRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, JobErrorCode.PROJECT_DATA_NOT_EXISTS_ERROR);
    }

    @Test
    void deleteJob() throws Exception {
        assertResponseWithEmptyData(() -> {
            DeleteProjectJobTaskRequest request = FakerUtils.fake(DeleteProjectJobTaskRequest.class);

            ProjectJobDO projectJobDO = buildProjectJobDO(false);
            projectJobDO.setStatus(GraphJobStatus.SUCCEEDED);
            Mockito.when(projectJobRepository.findByJobId(Mockito.any())).thenReturn(Optional.of(projectJobDO));

            return MockMvcRequestBuilders.post(getMappingUrl(ProjectController.class, "deleteJob", DeleteProjectJobTaskRequest.class)).
                    content(JsonUtils.toJSONString(request));
        });
    }

    @Test
    void agreeJobErrorException() throws Exception {
        assertErrorCode(() -> {
            AgreeProjectJobTaskRequest request = FakerUtils.fake(AgreeProjectJobTaskRequest.class);

            Mockito.when(projectJobRepository.findByJobId(Mockito.any())).thenReturn(Optional.of(buildProjectJobDO(false)));
            Mockito.when(jobStub.createJob(Mockito.any())).thenReturn(org.secretflow.v1alpha1.kusciaapi.Job.CreateJobResponse.newBuilder().build());
            Mockito.when(nodeRouteManager.getRouteStatus(Mockito.anyString(), Mockito.anyString())).thenReturn(buildRouteStatus());
            Mockito.when(nodeRouteRepository.findBySrcNodeIdAndDstNodeId(Mockito.anyString(), Mockito.anyString())).thenReturn(Optional.of(buildNodeRouteDO()));
            Mockito.when(remoteRequestService.checkBothSidesNodeRouteIsReady(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
            Mockito.when(remoteRequestService.sendPostJson(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(buildEasyPsiResponse());

            return MockMvcRequestBuilders.post(getMappingUrl(ProjectController.class, "agreeJob", AgreeProjectJobTaskRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, JobErrorCode.PROJECT_JOB_ACTION_NOT_ALLOWED);
    }

    @Test
    void pauseJob() throws Exception {
        assertResponseWithEmptyData(() -> {
            StopProjectJobTaskRequest request = FakerUtils.fake(StopProjectJobTaskRequest.class);

            ProjectJobDO projectJobDO = buildProjectJobDO(false);
            projectJobDO.setHostNodeId("kuscia-system");
            Mockito.when(projectJobRepository.findByJobId(Mockito.any())).thenReturn(Optional.of(projectJobDO));
            Mockito.when(nodeRouteManager.getRouteStatus(Mockito.anyString(), Mockito.anyString())).thenReturn(buildRouteStatus());
            Mockito.when(nodeRouteRepository.findBySrcNodeIdAndDstNodeId(Mockito.anyString(), Mockito.anyString())).thenReturn(Optional.of(buildNodeRouteDO()));
            Mockito.when(remoteRequestService.checkBothSidesNodeRouteIsReady(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
            Mockito.when(remoteRequestService.sendPostJson(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(buildEasyPsiResponse());

            Mockito.when(jobStub.deleteJob(Mockito.any())).thenReturn(org.secretflow.v1alpha1.kusciaapi.Job.DeleteJobResponse.newBuilder().build());
            return MockMvcRequestBuilders.post(getMappingUrl(ProjectController.class, "pauseJob", StopProjectJobTaskRequest.class)).
                    content(JsonUtils.toJSONString(request));
        });
    }

    @Test
    void pauseJobErrorException() throws Exception {
        assertErrorCode(() -> {
            StopProjectJobTaskRequest request = FakerUtils.fake(StopProjectJobTaskRequest.class);

            ProjectJobDO projectJobDO = buildProjectJobDO(false);
            projectJobDO.setStatus(GraphJobStatus.SUCCEEDED);
            Mockito.when(projectJobRepository.findByJobId(Mockito.any())).thenReturn(Optional.of(projectJobDO));
            Mockito.when(nodeRouteManager.getRouteStatus(Mockito.anyString(), Mockito.anyString())).thenReturn(buildRouteStatus());
            Mockito.when(nodeRouteRepository.findBySrcNodeIdAndDstNodeId(Mockito.anyString(), Mockito.anyString())).thenReturn(Optional.of(buildNodeRouteDO()));
            Mockito.when(remoteRequestService.checkBothSidesNodeRouteIsReady(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
            Mockito.when(remoteRequestService.sendPostJson(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(buildEasyPsiResponse());

            Mockito.when(jobStub.deleteJob(Mockito.any())).thenReturn(org.secretflow.v1alpha1.kusciaapi.Job.DeleteJobResponse.newBuilder().build());
            return MockMvcRequestBuilders.post(getMappingUrl(ProjectController.class, "pauseJob", StopProjectJobTaskRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, JobErrorCode.PROJECT_JOB_ACTION_NOT_ALLOWED);
    }

    @Test
    void continueJob() throws Exception {
        assertResponseWithEmptyData(() -> {
            StopProjectJobTaskRequest request = FakerUtils.fake(StopProjectJobTaskRequest.class);

            ProjectJobDO projectJobDO = buildProjectJobDO(false);
            projectJobDO.setHostNodeId("kuscia-system");
            projectJobDO.setStatus(GraphJobStatus.PAUSED);
            Mockito.when(projectJobRepository.findByJobId(Mockito.any())).thenReturn(Optional.of(projectJobDO));
            Mockito.when(nodeRouteManager.getRouteStatus(Mockito.anyString(), Mockito.anyString())).thenReturn(buildRouteStatus());
            Mockito.when(nodeRouteRepository.findBySrcNodeIdAndDstNodeId(Mockito.anyString(), Mockito.anyString())).thenReturn(Optional.of(buildNodeRouteDO()));
            Mockito.when(remoteRequestService.checkBothSidesNodeRouteIsReady(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
            Mockito.when(remoteRequestService.sendPostJson(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(buildEasyPsiResponse());
            Mockito.when(jobStub.deleteJob(Mockito.any())).thenReturn(org.secretflow.v1alpha1.kusciaapi.Job.DeleteJobResponse.newBuilder().build());
            Job.CreateJobResponse response = Job.CreateJobResponse.newBuilder()
                    .setStatus(Common.Status.newBuilder().setCode(0).build())
                    .build();
            Mockito.when(jobStub.createJob(Mockito.any())).thenReturn(response);
            return MockMvcRequestBuilders.post(getMappingUrl(ProjectController.class, "continueJob", StopProjectJobTaskRequest.class)).
                    content(JsonUtils.toJSONString(request));
        });
    }

    @Test
    void continueJobErrorException() throws Exception {
        assertErrorCode(() -> {
            StopProjectJobTaskRequest request = FakerUtils.fake(StopProjectJobTaskRequest.class);

            ProjectJobDO projectJobDO = buildProjectJobDO(false);
            projectJobDO.setStatus(GraphJobStatus.SUCCEEDED);
            Mockito.when(projectJobRepository.findByJobId(Mockito.any())).thenReturn(Optional.of(projectJobDO));
            Mockito.when(nodeRouteManager.getRouteStatus(Mockito.anyString(), Mockito.anyString())).thenReturn(buildRouteStatus());
            Mockito.when(nodeRouteRepository.findBySrcNodeIdAndDstNodeId(Mockito.anyString(), Mockito.anyString())).thenReturn(Optional.of(buildNodeRouteDO()));
            Mockito.when(remoteRequestService.checkBothSidesNodeRouteIsReady(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
            Mockito.when(remoteRequestService.sendPostJson(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(buildEasyPsiResponse());

            return MockMvcRequestBuilders.post(getMappingUrl(ProjectController.class, "continueJob", StopProjectJobTaskRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, JobErrorCode.PROJECT_JOB_ACTION_NOT_ALLOWED);
    }

    @Test
    void rejectJob() throws Exception {
        assertResponseWithEmptyData(() -> {
            RejectProjectJobTaskRequest request = FakerUtils.fake(RejectProjectJobTaskRequest.class);

            ProjectJobDO projectJobDO = buildProjectJobDO(false);
            projectJobDO.setStatus(GraphJobStatus.PENDING_REVIEW);
            Mockito.when(projectJobRepository.findByJobId(Mockito.any())).thenReturn(Optional.of(projectJobDO));
            Mockito.when(nodeRouteManager.getRouteStatus(Mockito.anyString(), Mockito.anyString())).thenReturn(buildRouteStatus());
            Mockito.when(nodeRouteRepository.findBySrcNodeIdAndDstNodeId(Mockito.anyString(), Mockito.anyString())).thenReturn(Optional.of(buildNodeRouteDO()));
            Mockito.when(remoteRequestService.checkBothSidesNodeRouteIsReady(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
            Mockito.when(remoteRequestService.sendPostJson(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(buildEasyPsiResponse());

            return MockMvcRequestBuilders.post(getMappingUrl(ProjectController.class, "rejectJob", RejectProjectJobTaskRequest.class)).
                    content(JsonUtils.toJSONString(request));
        });
    }

    @Test
    void rejectJobErrorException() throws Exception {
        assertErrorCode(() -> {
            RejectProjectJobTaskRequest request = FakerUtils.fake(RejectProjectJobTaskRequest.class);

            ProjectJobDO projectJobDO = buildProjectJobDO(false);
            projectJobDO.setStatus(GraphJobStatus.SUCCEEDED);
            Mockito.when(projectJobRepository.findByJobId(Mockito.any())).thenReturn(Optional.of(projectJobDO));
            Mockito.when(nodeRouteManager.getRouteStatus(Mockito.anyString(), Mockito.anyString())).thenReturn(buildRouteStatus());
            Mockito.when(nodeRouteRepository.findBySrcNodeIdAndDstNodeId(Mockito.anyString(), Mockito.anyString())).thenReturn(Optional.of(buildNodeRouteDO()));
            Mockito.when(remoteRequestService.checkBothSidesNodeRouteIsReady(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
            Mockito.when(remoteRequestService.sendPostJson(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(buildEasyPsiResponse());
            return MockMvcRequestBuilders.post(getMappingUrl(ProjectController.class, "rejectJob", RejectProjectJobTaskRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, JobErrorCode.PROJECT_JOB_ACTION_NOT_ALLOWED);
    }

    @Test
    void downloadNoPsiConfigErrorException() throws Exception {
        assertErrorCode(() -> {
            GetloadProjectResult request = FakerUtils.fake(GetloadProjectResult.class);
            Mockito.when(jobManager.openProjectJob(Mockito.anyString())).thenReturn(buildFutureProjectJobDO());

            return MockMvcRequestBuilders.post(getMappingUrl(ProjectController.class, "downloadProjectResult", DownloadProjectResult.class)).
                    content(JsonUtils.toJSONString(request));
        }, JobErrorCode.PROJECT_JOB_NOT_EXISTS);
    }


    @Test
    void getloadProjectResultNoPsiConfigErrorException() throws Exception {
        assertErrorCode(() -> {
            GetloadProjectResult request = FakerUtils.fake(GetloadProjectResult.class);
            Mockito.when(jobManager.openProjectJob(Mockito.anyString())).thenReturn(buildFutureProjectJobDO());

            return MockMvcRequestBuilders.get(getMappingUrl(ProjectController.class, "getloadProjectResult", HttpServletResponse.class, GetloadProjectResult.class)).
                    content(JsonUtils.toJSONString(request));
        }, JobErrorCode.PROJECT_JOB_RESULT_HASH_EXPIRED_ERROR);
    }

    @Test
    void getloadProjectResultReceiverErrorException() throws Exception {
        assertErrorCode(() -> {
            GetloadProjectResult request = FakerUtils.fake(GetloadProjectResult.class);

            ProjectJobDO projectJobDO = buildProjectJobDO(false);
            projectJobDO.setStatus(GraphJobStatus.SUCCEEDED);
            Mockito.when(jobManager.openProjectJob(Mockito.anyString())).thenReturn(buildFutureProjectJobDO());

            return MockMvcRequestBuilders.get(getMappingUrl(ProjectController.class, "getloadProjectResult", HttpServletResponse.class, GetloadProjectResult.class)).
                    content(JsonUtils.toJSONString(request));
        }, JobErrorCode.PROJECT_JOB_RESULT_HASH_EXPIRED_ERROR);
    }


    @Test
    void getProjectLogsNotJobExistsErrorException() throws Exception {
        assertErrorCode(() -> {
            GetProjectJobLogRequest request = FakerUtils.fake(GetProjectJobLogRequest.class);
            ProjectJobDO projectJobDO = buildProjectJobDO();
            projectJobDO.setStatus(GraphJobStatus.PENDING_CERT);
            Mockito.when(projectJobRepository.findByJobId(Mockito.anyString())).thenReturn(Optional.ofNullable(null));
            return MockMvcRequestBuilders.post(getMappingUrl(ProjectController.class, "getProjectLogs", GetProjectJobLogRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, JobErrorCode.PROJECT_JOB_NOT_EXISTS);
    }

    @Test
    void getProjectLogsNotAllowedErrorException() throws Exception {
        assertErrorCode(() -> {
            GetProjectJobLogRequest request = FakerUtils.fake(GetProjectJobLogRequest.class);
            ProjectJobDO projectJobDO = buildProjectJobDO();
            projectJobDO.setStatus(GraphJobStatus.PENDING_CERT);
            Mockito.when(projectJobRepository.findByJobId(Mockito.anyString())).thenReturn(Optional.of(projectJobDO));
            return MockMvcRequestBuilders.post(getMappingUrl(ProjectController.class, "getProjectLogs", GetProjectJobLogRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, JobErrorCode.PROJECT_JOB_ACTION_NOT_ALLOWED);
    }

    @Test
    void getDataTableDataPathNotExistsErrorException() throws Exception {
        assertErrorCode(() -> {
            GetProjectJobTableRequest getProjectJobTableRequest = buildGetProjectJobTableRequest(false);
            return MockMvcRequestBuilders.post(getMappingUrl(ProjectController.class, "getDataTable", GetProjectJobTableRequest.class)).
                    content(JsonUtils.toJSONString(getProjectJobTableRequest));
        }, JobErrorCode.PROJECT_DATA_PATH_NOT_EXISTS_ERROR);
    }

    @Test
    void checkDataTableDataPathNotExistsErrorException() throws Exception {
        assertErrorCode(() -> {
            GetProjectJobTableRequest getProjectJobTableRequest = buildGetProjectJobTableRequest(true);
            getProjectJobTableRequest.setTableName("test");
            return MockMvcRequestBuilders.post(getMappingUrl(ProjectController.class, "getDataTable", GetProjectJobTableRequest.class)).
                    content(JsonUtils.toJSONString(getProjectJobTableRequest));
        }, JobErrorCode.PROJECT_DATA_PATH_NOT_EXISTS_ERROR);
    }

    @Test
    void checkDataTableDataNotExistsErrorException() throws Exception {
        assertErrorCode(() -> {
            GetProjectJobTableRequest getProjectJobTableRequest = buildGetProjectJobTableRequest(true);
            return MockMvcRequestBuilders.post(getMappingUrl(ProjectController.class, "getDataTable", GetProjectJobTableRequest.class)).
                    content(JsonUtils.toJSONString(getProjectJobTableRequest));
        }, JobErrorCode.PROJECT_DATA_NOT_EXISTS_ERROR);
    }

    @Test
    void getDataHeaderDataPathNotExistsErrorException() throws Exception {
        assertErrorCode(() -> {
            GetProjectJobDataHeaderRequest request = buildGetProjectJobDataHeaderRequest("test");
            return MockMvcRequestBuilders.post(getMappingUrl(ProjectController.class, "getDataHeader", GetProjectJobDataHeaderRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, JobErrorCode.PROJECT_DATA_NOT_EXISTS_ERROR);
    }

    @Test
    void checkDataHeaderDataHeaderNullErrorException() throws Exception {
        assertErrorCode(() -> {
            GetProjectJobDataHeaderRequest request = buildGetProjectJobDataHeaderRequest("test", null);
            return MockMvcRequestBuilders.post(getMappingUrl(ProjectController.class, "getDataHeader", GetProjectJobDataHeaderRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, JobErrorCode.PROJECT_TABLE_HEADER_NOT_EXISTS_ERROR);
    }

    @Test
    void checkDataHeaderDataHeaderNotExistsErrorException() throws Exception {
        assertErrorCode(() -> {
            GetProjectJobDataHeaderRequest request = buildGetProjectJobDataHeaderRequest("test", List.of("id"));
            return MockMvcRequestBuilders.post(getMappingUrl(ProjectController.class, "getDataHeader", GetProjectJobDataHeaderRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, JobErrorCode.PROJECT_DATA_NOT_EXISTS_ERROR);
    }
}
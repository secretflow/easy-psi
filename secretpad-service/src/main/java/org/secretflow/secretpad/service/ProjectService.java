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

package org.secretflow.secretpad.service;

import org.secretflow.secretpad.common.dto.DownloadInfo;
import org.secretflow.secretpad.service.model.common.SecretPadPageResponse;
import org.secretflow.secretpad.service.model.graph.GrapDataHeaderVO;
import org.secretflow.secretpad.service.model.graph.GrapDataTableVO;
import org.secretflow.secretpad.service.model.graph.GraphNodeJobLogsVO;
import org.secretflow.secretpad.service.model.project.*;

import java.util.List;

/**
 * Project service interface
 *
 * @author yansi
 * @date 2023/5/4
 */
public interface ProjectService {

    /**
     * Paging query project job list by list project job request
     *
     * @param projectId list project job request
     * @return page response of project job view object
     */
    SecretPadPageResponse<ProjectJobListVO> listProjectJob(ListProjectJobRequest projectId);

    /**
     * Query project job by projectId and jobId
     *
     * @param request target jobId
     * @return project job view object
     */
    ProjectJobVO getProjectJob(GetProjectJobRequest request);

    /**
     * Stop the project job by stop project job task request
     *
     * @param request stop project job task request
     */
    void stopKusciaJob(StopProjectJobTaskRequest request);

    /**
     * Get project job logs by file
     *
     * @param request
     * @return project job logs
     */
    GraphNodeJobLogsVO getProjectJobInFeilLogs(GetProjectJobLogRequest request);

    /**
     * Get project job csv header
     *
     * @param request
     * @return csv header
     */
    GrapDataHeaderVO getDataHeader(GetProjectJobDataHeaderRequest request, boolean check);

    /**
     * Get project job csv header
     *
     * @return csv table
     */
    GrapDataTableVO getDataTable(GetProjectJobTableRequest request, boolean check);

    /**
     * Delete the project job by delete project job task request
     *
     * @param request delete project job task request
     */
    void deleteProjectJob(DeleteProjectJobTaskRequest request);

    /**
     * Create the job by create job task request
     *
     * @param request
     */
    CreateProjectJobVO createJob(CreateProjectJobRequest request);

    /**
     * Agree the job by agree job task request
     *
     * @param request
     */
    void agreeJob(AgreeProjectJobTaskRequest request);

    /**
     * Pause the project job by pause project job task request
     *
     * @param request
     */
    void pauseKusciaJob(StopProjectJobTaskRequest request);

    /**
     * Continue the project job by continue project job task request
     *
     * @param request
     */
    void continueKusciaJob(StopProjectJobTaskRequest request);

    /**
     * Create the project job by create project job task request
     *
     * @param request
     */
    void createKusciaJob(CreateProjectJobTaskRequest request);

    /**
     * Reject the project job by reject project job task request
     *
     * @param request
     */
    void rejectJob(RejectProjectJobTaskRequest request);

    /**
     * Synchronize host node project jobs
     *
     * @param jobs project task view object list
     */
    void syncHostNodeProjectJobs(List<ProjectJobVO> jobs);

    /**
     * Download project result
     *
     * @param jobId target jobId
     */
    String downloadProjectResult(String jobId);

    /**
     * Download project result
     *
     * @param hash target jobId
     */
    DownloadInfo getloadProjectResult(String hash);

    /**
     * Query edge project jobs by request node id
     *
     * @param requestNodeId target request node id
     * @return project job data object list
     */
    List<ProjectJobVO> queryEdgeProjectJobs(String requestNodeId);

    /**
     * Stop the project job by stop project job task request
     *
     * @param request stop project job task request
     */
    void stopProjectJob(StopProjectJobTaskRequest request);

    /**
     * Continue the project job by continue project job task request
     *
     * @param request
     */
    void continueJob(StopProjectJobTaskRequest request);

    /**
     * Pause the project job by pause project job task request
     *
     * @param request
     */
    void pauseJob(StopProjectJobTaskRequest request);

}

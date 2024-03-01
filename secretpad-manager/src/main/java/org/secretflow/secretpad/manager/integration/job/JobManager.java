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

package org.secretflow.secretpad.manager.integration.job;

import org.secretflow.secretpad.common.errorcode.JobErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.common.util.DateTimes;
import org.secretflow.secretpad.common.util.FileUtils;
import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.common.util.Sha256Utils;
import org.secretflow.secretpad.manager.integration.fabric.FabricManager;
import org.secretflow.secretpad.persistence.entity.FabricLogDO;
import org.secretflow.secretpad.persistence.entity.ProjectJobDO;
import org.secretflow.secretpad.persistence.model.GraphJobStatus;
import org.secretflow.secretpad.persistence.repository.FabricLogRepository;
import org.secretflow.secretpad.persistence.repository.ProjectJobRepository;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.secretflow.v1alpha1.common.Common;
import org.secretflow.v1alpha1.kusciaapi.Job;
import org.secretflow.v1alpha1.kusciaapi.JobServiceGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static org.secretflow.secretpad.manager.integration.model.Constants.SUCCESS_STATUS_MESSAGE;

/**
 * Manager job operation
 *
 * @author yansi
 * @date 2023/5/23
 */
@Slf4j
public class JobManager extends AbstractJobManager {

    private final static String PARTY_STATUS_FAILED = "Failed";
    private final static Logger LOGGER = LoggerFactory.getLogger(JobManager.class);
    private final ProjectJobRepository projectJobRepository;

    private final JobServiceGrpc.JobServiceBlockingStub jobStub;

    @Autowired
    private FabricManager fabricManager;

    @Autowired(required = false)
    @Qualifier("fabricThreadPool")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Autowired
    private FabricLogRepository fabricLogRepository;

    @Value("${secretpad.node-id}")
    private String nodeId;

    public JobManager(ProjectJobRepository projectJobRepository,
                      JobServiceGrpc.JobServiceBlockingStub jobStub) {
        this.projectJobRepository = projectJobRepository;
        this.jobStub = jobStub;
    }

    @Value("${secretpad.data.dir-path:/app/data/}")
    private String storeDir;

    public static final String CSV_DATA_PATH = File.separator + "app" + File.separator + "data";

    public static final String KUSCIA_DATA_PATH = "/home/kuscia/var/storage/data/";

    public static final String PROJECT_JOB_LOGS = File.separator + "app" + File.separator + "log" + File.separator + "pods";

    public static final String PROJECT_JOB_TASK_RES = "result" + File.separator;

    public static final String PROJECT_JOB_TASK_TMP = "tmp" + File.separator;

    public static final String HTTP_HEADER = "http://";

    public static String CREATE_JOB_API = "/api/v1alpha1/project/job/create/kuscia";

    public static String STOP_JOB_API = "/api/v1alpha1/project/job/stop/kuscia";

    public static String CONTINUE_JOB_API = "/api/v1alpha1/project/job/continue/kuscia";

    public static String PAUSE_JOB_API = "/api/v1alpha1/project/job/pause/kuscia";

    /**
     * Start synchronized job
     * <p>
     * TODO: can be refactor to void watch(type, handler) ?
     */
    @Override
    public void startSync() {
        try {
            Iterator<Job.WatchJobEventResponse> responses = jobStub.watchJob(Job.WatchJobRequest.newBuilder().build());
            LOGGER.info("starter jobEvent ... ");
            responses.forEachRemaining(this::syncJob);
        } catch (Exception e) {
            LOGGER.error("startSync exception: {}, while restart", e.getMessage());
        }
    }

    /**
     * Synchronize project job data via job event response
     *
     * @param it
     */
    public void syncJob(Job.WatchJobEventResponse it) {
        if (it.getType() == Job.EventType.UNRECOGNIZED || it.getType() == Job.EventType.ERROR) {
            // do nothing
            return;
        }
        LOGGER.info("watched jobEvent: jobId={}, jobState={}, task=[{}], endTime={}", it.getObject().getJobId(), it.getObject().getStatus().getState(),
                it.getObject().getStatus().getTasksList().stream().map(t -> String.format("taskId=%s,state=%s", t.getTaskId(), t.getState())).collect(Collectors.joining("|")),
                it.getObject().getStatus().getEndTime());
        // delete tmp file
        if (isFinishedState(it.getObject().getStatus().getState())) {
            FileUtils.deleteAllFile(buildTmpDirPath(it.getObject().getJobId()));
        }
        Optional<ProjectJobDO> projectJobOpt = projectJobRepository.findByJobId(it.getObject().getJobId());
        if (projectJobOpt.isEmpty()) {
            LOGGER.info("watched jobEvent: jobId={}, but project job not exist, skip", it.getObject().getJobId());
            return;
        }
        if (projectJobOpt.get().isFinished()) {
            return;
        }
        LOGGER.info("watched jobEvent: type={}, find project job={}, id={}, will update.", it.getType(),
                JsonUtils.toJSONString(projectJobOpt.get()), projectJobOpt.get().getUpk().getJobId());
        ProjectJobDO job = updateJob(it, projectJobOpt.get());
        LOGGER.info("watched jobEvent: updated project job={}", JsonUtils.toJSONString(projectJobOpt.get()));
        projectJobRepository.save(job);
        //log to chain
        uploadLogToChain(job);
    }


    /**
     * Upload to chain
     *
     * @param job
     */

    private void uploadLogToChain(ProjectJobDO job) {
        threadPoolTaskExecutor.execute(() -> {
            if (!fabricManager.isOpen()) {
                return;
            }
            FabricLogDO fabricLogDO = new FabricLogDO();
            String directoryPath = JobManager.PROJECT_JOB_LOGS;
            String fileName = getFileName(job, directoryPath);
            List<String> filePaths = FileUtils.traverseDirectories(new File(directoryPath + File.separator + fileName + File.separator + "secretflow"), ".log", FileUtils.FILE_PATH);
            if (filePaths == null || filePaths.size() == 0) {
                LOGGER.warn("job log upload error,{} task log path not exit", job.getId());
                return;
            }
            //get File
            try {
                //start truncate log
                fabricLogDO.setLogPath(filePaths.get(0));
                fabricLogDO.setLogHash(Sha256Utils.hash(filePaths.get(0)));
                //upload to chain
                if (Objects.nonNull(fabricLogDO) && StringUtils.isNotBlank(fabricLogDO.getLogHash())) {
                    fabricManager.submitTransaction(fabricLogDO);
                    fabricLogDO.setResult(1);
                    fabricLogDO.setMessage("success");
                }
            } catch (Exception exception) {
                fabricLogDO.setResult(2);
                fabricLogDO.setMessage(exception.getMessage().length() > 500 ? exception.getMessage().substring(0, 500) : exception.getMessage());
                log.error("job log upload error, to fabric  error,{}", exception.getMessage());
            }
            fabricLogRepository.save(fabricLogDO);
        });

    }

    /**
     * Get job file name
     *
     * @param job
     * @param directoryPath
     * @return {@link String }
     */

    @Nullable
    private String getFileName(ProjectJobDO job, String directoryPath) {
        File directory = new File(directoryPath);
        String pattern = nodeId + "_" + job.getId() + "-0";
        FilenameFilter filenameFilter = (dir, name) -> name.startsWith(pattern);
        File[] matchingFiles = directory.listFiles(filenameFilter);
        String fileName = null;
        long lastModified = 0L;
        if (matchingFiles != null) {
            for (File file : matchingFiles) {
                if (file.lastModified() > lastModified) {
                    fileName = file.getName();
                    lastModified = file.lastModified();
                }
            }
        }
        return fileName;
    }

    /**
     * Build tmp directory path
     *
     * @param jobId target jobId
     * @return Tmp directory path
     */
    private String buildTmpDirPath(String jobId) {
        return storeDir + "tmp" + File.separator + jobId;
    }


    /**
     * Update project job data via job event response
     *
     * @param it
     * @param projectJob
     * @return ProjectJobDO
     */
    public ProjectJobDO updateJob(Job.WatchJobEventResponse it, ProjectJobDO projectJob) {
        switch (it.getType()) {
//            case DELETED:
//                // if projectJob status is not PAUSED, stop the job
//                if (!projectJob.getStatus().equals(GraphJobStatus.PAUSED) && !projectJob.getStatus().equals(GraphJobStatus.FAILED)) {
//                    projectJob.stop(it.getObject().getStatus().getErrMsg());
//                }
//                return projectJob;
            case ADDED:
            case MODIFIED:
                Job.JobStatusDetail kusciaJobStatus = it.getObject().getStatus();
                // when the job state is finished but the end time is not set, we don't update, because that some task state may be not terminate state.
                if (!(isFinishedState(it.getObject().getStatus().getState()) && Strings.isNullOrEmpty(it.getObject().getStatus().getEndTime()))) {
                    projectJob.setStatus(GraphJobStatus.formKusciaJobStatus(kusciaJobStatus.getState()));
                    projectJob.setErrMsg(kusciaJobStatus.getErrMsg());
                }
                if (!Strings.isNullOrEmpty(it.getObject().getStatus().getEndTime())) {
                    projectJob.setFinishedTime(DateTimes.utcFromRfc3339(it.getObject().getStatus().getEndTime()));
                }

                return projectJob;
            default:
                return null;
        }
    }

    /**
     * Check response status whether finished
     *
     * @param state status
     * @return status whether finished
     */
    private boolean isFinishedState(String state) {
        return "Succeeded".equals(state);
    }

    /**
     * Catch task failed party reason via task status
     *
     * @param kusciaTaskStatus
     * @return task failed party reasons
     */
    @NotNull
    private List<String> catchTaskFailedPartyReason(@NotNull Job.TaskStatus kusciaTaskStatus) {
        return kusciaTaskStatus.getPartiesList().stream().filter(pt -> PARTY_STATUS_FAILED.equals(pt.getState())).map(
                pt -> String.format("party %s failed msg: %s", pt.getDomainId(), pt.getErrMsg())
        ).collect(Collectors.toList());
    }

    /**
     * Catch task failed party reason via task status
     *
     * @param kusciaTaskStatus
     * @return task failed party reasons
     */
    private List<String> taskFailedReason(@Nonnull Job.TaskStatus kusciaTaskStatus) {
        List<String> reasons = catchTaskFailedPartyReason(kusciaTaskStatus);
        reasons.add(kusciaTaskStatus.getErrMsg());
        return reasons;
    }

    @Override
    public void createJob(Job.CreateJobRequest request) {
        LOGGER.info("create kuscia job request ={}", request);
        Job.CreateJobResponse response = jobStub.createJob(request);
        if (!response.hasStatus()) {
            throw SecretpadException.of(JobErrorCode.PROJECT_JOB_CREATE_ERROR);
        }
        Common.Status status = response.getStatus();
        String message = status.getMessage();
        LOGGER.info("create kuscia job response ={}", response);
        if (status.getCode() != 0 || (!StringUtils.isEmpty(message) && !SUCCESS_STATUS_MESSAGE.equalsIgnoreCase(message))) {
            throw SecretpadException.of(JobErrorCode.PROJECT_JOB_CREATE_ERROR, status.getMessage());
        }
    }

    @Async
    @Override
    public Future<ProjectJobDO> openProjectJob(String jobId) {
        Optional<ProjectJobDO> jobOpt = projectJobRepository.findByJobId(jobId);
        return new AsyncResult<>(jobOpt.get());
    }
}

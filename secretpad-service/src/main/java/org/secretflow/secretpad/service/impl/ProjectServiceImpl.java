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

import org.secretflow.secretpad.common.constant.JobConstants;
import org.secretflow.secretpad.common.dto.DownloadInfo;
import org.secretflow.secretpad.common.errorcode.JobErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.common.util.*;
import org.secretflow.secretpad.manager.integration.job.AbstractJobManager;
import org.secretflow.secretpad.manager.integration.job.JobManager;
import org.secretflow.secretpad.persistence.entity.ProjectJobDO;
import org.secretflow.secretpad.persistence.model.GraphJobOperation;
import org.secretflow.secretpad.persistence.model.GraphJobStatus;
import org.secretflow.secretpad.persistence.model.PsiConfigDO;
import org.secretflow.secretpad.persistence.repository.ProjectJobRepository;
import org.secretflow.secretpad.service.EnvService;
import org.secretflow.secretpad.service.ProjectService;
import org.secretflow.secretpad.service.RemoteRequestService;
import org.secretflow.secretpad.service.graph.JobChain;
import org.secretflow.secretpad.service.model.common.SecretPadPageResponse;
import org.secretflow.secretpad.service.model.data.DataTableInformationVo;
import org.secretflow.secretpad.service.model.graph.GrapDataHeaderVO;
import org.secretflow.secretpad.service.model.graph.GrapDataTableVO;
import org.secretflow.secretpad.service.model.graph.GraphNodeJobLogsVO;
import org.secretflow.secretpad.service.model.graph.ProjectPsiJob;
import org.secretflow.secretpad.service.model.project.*;

import jakarta.persistence.criteria.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.secretflow.v1alpha1.kusciaapi.Job;
import org.secretflow.v1alpha1.kusciaapi.JobServiceGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * Project service implementation class
 *
 * @author yansi
 * @date 2023/5/4
 */
@Service
public class ProjectServiceImpl implements ProjectService {

    private final static Logger LOGGER = LoggerFactory.getLogger(JobManager.class);

    @Autowired
    private ProjectJobRepository projectJobRepository;

    @Autowired
    private JobServiceGrpc.JobServiceBlockingStub jobStub;

    @Autowired
    private JobChain jobChain;

    @Autowired
    private EnvService envService;

    @Autowired
    private RemoteRequestService remoteRequestService;

    @Autowired
    private AbstractJobManager jobManager;

    @Value("${secretpad.data.dir-path:/app/data/}")
    private String storeDir;

    @Value("${secretpad.gateway}")
    private String gateway;

    private static Map<String, ProjectJobResultVO> jobResult = new HashMap<>();

    @Override
    public SecretPadPageResponse<ProjectJobListVO> listProjectJob(ListProjectJobRequest request) {
        Sort.Direction direction = StringUtils.equals(Sort.Direction.ASC.name(), request.getSortType()) ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        Specification<ProjectJobDO> specification = (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<Predicate>();
            if (!CollectionUtils.isEmpty(request.getStatusFilter())) {
                predicates.add(root.get("status").in(request.getStatusFilter()));
            }
            Predicate[] array_and = predicates.toArray(new Predicate[predicates.size()]);
            Predicate pre_and = criteriaBuilder.and(array_and);
            Predicate pre_or = null;
            if (StringUtils.isNotBlank(request.getSearch())) {
                List<Predicate> predicateList = new ArrayList<Predicate>();
                predicateList.add(criteriaBuilder.like(root.get("initiatorNodeId"), "%" + request.getSearch() + "%"));
                predicateList.add(criteriaBuilder.like(root.get("partnerNodeId"), "%" + request.getSearch() + "%"));
                predicateList.add(criteriaBuilder.like(root.get("name"), "%" + request.getSearch() + "%"));
                Predicate[] array_or = predicateList.toArray(new Predicate[predicateList.size()]);
                pre_or = criteriaBuilder.or(array_or);
            }
            if (null == pre_or) {
                criteriaQuery.where(pre_and);
            } else {
                criteriaQuery.where(pre_and, pre_or);
            }
            return criteriaQuery.getRestriction();
        };
        Pageable pageable = PageRequest.of(request.getPageNum() - 1, request.getPageSize(), direction, request.getSortKey());

        Page<ProjectJobDO> page = projectJobRepository.findAll(specification, pageable);
        if (ObjectUtils.isEmpty(page) || page.getTotalElements() == 0) {
            return SecretPadPageResponse.toPage(null, 0);
        }
        List<ProjectJobListVO> data = convert2ListVO(page, UserContext.getUser().getOwnerId());
        return SecretPadPageResponse.toPage(data, page.getTotalElements());
    }

    @Override
    public ProjectJobVO getProjectJob(GetProjectJobRequest request) {
        ProjectJobDO job = openProjectJob(request.getJobId());
        return buildProjectJobVO(job, UserContext.getUser().getOwnerId());
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void stopKusciaJob(StopProjectJobTaskRequest request) {
        LOGGER.info("The other party cancels the task: {}", request.getJobId());
        ProjectJobDO job = openProjectJob(request.getJobId());

        // check node route
        String platformNodeId = envService.getPlatformNodeId();
        remoteRequestService.checkBothSidesNodeRouteIsReady(platformNodeId, StringUtils.equals(platformNodeId, job.getInitiatorNodeId()) ? job.getPartnerNodeId() : job.getInitiatorNodeId());

        Boolean checkOperation = GraphJobStatus.checkOperation(job.getStatus(), GraphJobOperation.CANCEL);
        if (!checkOperation) {
            throw SecretpadException.of(JobErrorCode.PROJECT_JOB_ACTION_NOT_ALLOWED, GraphJobStatus.getName(job.getStatus()), GraphJobOperation.getName(GraphJobOperation.CANCEL));
        }

        String tmpFilepath = storeDir + JobManager.PROJECT_JOB_TASK_TMP + job.getUpk().getJobId();
        File tmpFile = new File(tmpFilepath);

        if (deleteFile(tmpFile)) {
            LOGGER.info("Successfully deleted tmpFile");
        }

        job.stop("操作取消");
        projectJobRepository.save(job);
        // TODO: we don't check the status, because of we can't know error reason. For job not found, should be treat as success now.
        jobStub.deleteJob(Job.DeleteJobRequest.newBuilder().setJobId(job.getUpk().getJobId()).build());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void stopProjectJob(StopProjectJobTaskRequest request) {
        LOGGER.info("We cancel the task: {}", request.getJobId());
        ProjectJobDO job = openProjectJob(request.getJobId());

        // check node route
        String platformNodeId = envService.getPlatformNodeId();
        remoteRequestService.checkBothSidesNodeRouteIsReady(platformNodeId, StringUtils.equals(platformNodeId, job.getInitiatorNodeId()) ? job.getPartnerNodeId() : job.getInitiatorNodeId());

        Boolean checkOperation = GraphJobStatus.checkOperation(job.getStatus(), GraphJobOperation.CANCEL);
        if (!checkOperation || checkPartnerCancelReview(platformNodeId,job)) {
            throw SecretpadException.of(JobErrorCode.PROJECT_JOB_ACTION_NOT_ALLOWED, GraphJobStatus.getName(job.getStatus()), GraphJobOperation.getName(GraphJobOperation.CANCEL));
        }

        if (StringUtils.equals(platformNodeId, job.getHostNodeId())) {
            stopKusciaJob(request);
        } else {
            String url = JobManager.HTTP_HEADER + gateway + JobManager.STOP_JOB_API;
            remoteRequestService.sendPostJson(request, job.getPartnerNodeId(), url);
        }
    }

    private boolean checkPartnerCancelReview(String nodeId,ProjectJobDO job) {
        return (StringUtils.equals(nodeId,job.getPartnerNodeId()) && StringUtils.equals(job.getStatus().name(),GraphJobStatus.PENDING_REVIEW.name()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProjectJob(DeleteProjectJobTaskRequest request) {
        LOGGER.info("We delete the task: {}", request.getJobId());
        ProjectJobDO job = openProjectJob(request.getJobId());

        Boolean checkOperation = GraphJobStatus.checkOperation(job.getStatus(), GraphJobOperation.DELETE);
        if (!checkOperation) {
            throw SecretpadException.of(JobErrorCode.PROJECT_JOB_ACTION_NOT_ALLOWED, GraphJobStatus.getName(job.getStatus()), GraphJobOperation.getName(GraphJobOperation.DELETE));
        }

        String resFilepath = storeDir + JobManager.PROJECT_JOB_TASK_RES + job.getUpk().getJobId() + File.separator;
        File resFile = new File(resFilepath);
        String tmpFilepath = storeDir + JobManager.PROJECT_JOB_TASK_TMP + job.getUpk().getJobId() + File.separator;
        File tmpFile = new File(tmpFilepath);

        if (deleteFile(resFile)) {
            LOGGER.info("Successfully deleted resFile");
        }
        if (deleteFile(tmpFile)) {
            LOGGER.info("Successfully deleted tmpFile");
        }
        projectJobRepository.deleteById(new ProjectJobDO.UPK("ezpsi", request.getJobId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CreateProjectJobVO createJob(CreateProjectJobRequest request) {
        LOGGER.info("Our Create Task: {}", JsonUtils.toJSONString(request));
        createJobFileNameCheck(request);

        // check node route
        remoteRequestService.checkBothSidesNodeRouteIsReady(request.getInitiatorConfig().getNodeId(), request.getPartnerConfig().getNodeId());

        String jobId = UUIDUtils.random(8);
        CreateProjectJobTaskRequest taskRequest = convertRequest(request, jobId);
        ProjectJobDO jobDO = saveJob(taskRequest, jobId);
        createFile(jobDO);
        taskRequest.setJobId(jobId);
        String url = JobManager.HTTP_HEADER + gateway + JobManager.CREATE_JOB_API;
        remoteRequestService.sendPostJson(taskRequest, taskRequest.getPartnerConfig().getNodeId(), url);
        return CreateProjectJobVO.builder()
                .jobId(jobDO.getUpk().getJobId())
                .name(jobDO.getName())
                .build();
    }

    private void createJobFileNameCheck(CreateProjectJobRequest request) {
        LOGGER.info("check initiatorConfig file name: {}", request.getInitiatorConfig().getPath());
        FileUtils.fileNameCheck(request.getInitiatorConfig().getPath());
        LOGGER.info("check outputConfig file name: {}", request.getOutputConfig().getPath());
        FileUtils.fileNameCheck(request.getOutputConfig().getPath());
        LOGGER.info("check partnerConfig file name: {}", request.getPartnerConfig().getPath());
        FileUtils.fileNameCheck(request.getPartnerConfig().getPath());
    }


    @NotNull
    private ProjectJobDO saveJob(CreateProjectJobTaskRequest request, String jobId) {
        ProjectJobDO jobDO = new ProjectJobDO();
        ProjectJobDO.UPK upk = new ProjectJobDO.UPK();
        upk.setJobId(jobId);
        jobDO.setUpk(upk);
        jobDO.setName(request.getName());
        jobDO.setDescription(request.getDescription());

        CreateProjectJobTaskRequest.PsiConfig partnerConfig = request.getPartnerConfig();
        PsiConfigDO partnerPsiConfigDO = convertPsiConfig(partnerConfig, jobDO.getUpk().getJobId());
        jobDO.setPartnerConfig(JsonUtils.toJSONString(partnerPsiConfigDO));

        CreateProjectJobTaskRequest.PsiConfig initiatorConfig = request.getInitiatorConfig();
        PsiConfigDO initatorPsiConfigDO = convertPsiConfig(initiatorConfig, jobDO.getUpk().getJobId());
        jobDO.setInitiatorConfig(JsonUtils.toJSONString(initatorPsiConfigDO));

        jobDO.setInitiatorNodeId(request.getInitiatorConfig().getNodeId());
        jobDO.setPartnerNodeId(request.getPartnerConfig().getNodeId());
        jobDO.setHostNodeId(request.getPartnerConfig().getNodeId());
        jobDO.setStatus(GraphJobStatus.PENDING_REVIEW);
        projectJobRepository.save(jobDO);
        return jobDO;
    }

    private PsiConfigDO convertPsiConfig(CreateProjectJobTaskRequest.PsiConfig psiConfig, String jobId) {
        PsiConfigDO psiConfigDO = new PsiConfigDO();
        BeanUtils.copyProperties(psiConfig, psiConfigDO);
        PsiConfigDO.InputConfig inputConfig = new PsiConfigDO.InputConfig();
        String inputPath = JobManager.KUSCIA_DATA_PATH + psiConfig.getInputConfig().getPath();
        inputConfig.setPath(inputPath);
        inputConfig.setType(psiConfig.getInputConfig().getType());
        psiConfigDO.setInputConfig(inputConfig);
        LOGGER.debug("convert psi config input config:{}", inputConfig);
        psiConfigDO.setDisableAlignment(psiConfig.getDisableAlignment());
        LOGGER.debug("save job data name: {}, data interval: {}", psiConfig.getInputConfig().getPath(), psiConfig.getDatatableCount());
        psiConfigDO.setDatatableCount(psiConfig.getDatatableCount());
        psiConfigDO.setSkipDuplicatesCheck(psiConfig.getSkipDuplicatesCheck());
        psiConfigDO.setAdvancedJoinType(psiConfig.getAdvancedJoinType());

        PsiConfigDO.ContextDescProto contextDescProto = new PsiConfigDO.ContextDescProto();
        contextDescProto.setHttpTimeoutMs(psiConfig.getLinkConfig().getHttpTimeoutMs());
        contextDescProto.setRecvTimeoutMs(psiConfig.getLinkConfig().getRecvTimeoutMs());
        psiConfigDO.setLinkConfig(contextDescProto);
        LOGGER.debug("convert psi config context desc proto:{}", contextDescProto);

        PsiConfigDO.OutputConfig outputConfig = new PsiConfigDO.OutputConfig();
        String partnerOutputPath = JobManager.KUSCIA_DATA_PATH + JobManager.PROJECT_JOB_TASK_RES + jobId + File.separator;
        if (StringUtils.isNotBlank(psiConfig.getOutputConfig().getPath())) {
            partnerOutputPath = partnerOutputPath + psiConfig.getOutputConfig().getPath();
        }
        outputConfig.setPath(partnerOutputPath);
        outputConfig.setType(psiConfig.getOutputConfig().getType());
        psiConfigDO.setOutputConfig(outputConfig);
        LOGGER.debug("convert psi config output config:{}", outputConfig);

        PsiConfigDO.ProtocolConfig protocolConfig = new PsiConfigDO.ProtocolConfig();
        CreateProjectJobTaskRequest.PsiConfig.ProtocolConfig requestProtocolConfig = psiConfig.getProtocolConfig();
        protocolConfig.setProtocol(requestProtocolConfig.getProtocol());
        protocolConfig.setBroadcastResult(requestProtocolConfig.getBroadcastResult());
        protocolConfig.setRole(requestProtocolConfig.getRole());

        if (!ObjectUtils.isEmpty(requestProtocolConfig.getEcdhConfig())) {
            PsiConfigDO.ProtocolConfig.EcdhConfig ecdhConfig = new PsiConfigDO.ProtocolConfig.EcdhConfig();
            ecdhConfig.setCurve(requestProtocolConfig.getEcdhConfig().getCurve());
            protocolConfig.setEcdhConfig(ecdhConfig);
        }
        if (!ObjectUtils.isEmpty(requestProtocolConfig.getKkrtConfig())) {
            PsiConfigDO.ProtocolConfig.KkrtConfig kkrtConfig = new PsiConfigDO.ProtocolConfig.KkrtConfig();
            kkrtConfig.setBucketSize(requestProtocolConfig.getKkrtConfig().getBucketSize());
            protocolConfig.setKkrtConfig(kkrtConfig);
        }
        if (!ObjectUtils.isEmpty(requestProtocolConfig.getRr22Config())) {
            PsiConfigDO.ProtocolConfig.Rr22Config rr22Config = new PsiConfigDO.ProtocolConfig.Rr22Config();
            rr22Config.setBucketSize(requestProtocolConfig.getRr22Config().getBucketSize());
            rr22Config.setLowCommMode(requestProtocolConfig.getRr22Config().getLowCommMode());
            protocolConfig.setRr22Config(rr22Config);
        }

        psiConfigDO.setProtocolConfig(protocolConfig);
        LOGGER.debug("convert psi config protocol config:{}", protocolConfig);
        psiConfigDO.setOutputDifference(psiConfig.getOutputDifference());
        PsiConfigDO.RecoveryConfig config = new PsiConfigDO.RecoveryConfig();
        config.setEnabled(psiConfig.getRecoveryConfig().getEnabled());
        config.setFolder(psiConfig.getRecoveryConfig().getFolder());
        psiConfigDO.setRecoveryConfig(config);
        LOGGER.debug("convert psi config psiConfigDO:{}", psiConfigDO);
        return psiConfigDO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void agreeJob(AgreeProjectJobTaskRequest request) {
        LOGGER.info("We agree the task: {}", request.getJobId());
        ProjectJobDO job = openProjectJob(request.getJobId());

        // check node route
        String platformNodeId = envService.getPlatformNodeId();
        remoteRequestService.checkBothSidesNodeRouteIsReady(platformNodeId, StringUtils.equals(platformNodeId, job.getInitiatorNodeId()) ? job.getPartnerNodeId() : job.getInitiatorNodeId());

        Boolean checkOperation = GraphJobStatus.checkOperation(job.getStatus(), GraphJobOperation.AGREE);
        if (!checkOperation) {
            throw SecretpadException.of(JobErrorCode.PROJECT_JOB_ACTION_NOT_ALLOWED, GraphJobStatus.getName(job.getStatus()), GraphJobOperation.getName(GraphJobOperation.AGREE));
        }

        PsiConfigDO partnerPsiConfigDO = JsonUtils.toJavaObject(job.getPartnerConfig(), PsiConfigDO.class);
        String inputConfigPath = partnerPsiConfigDO.getInputConfig().getPath();
        String tableName = inputConfigPath.substring(inputConfigPath.lastIndexOf("/") + 1);

        //check data table
        GetProjectJobTableRequest tableRequest = new GetProjectJobTableRequest();
        tableRequest.setTableName(tableName);
        GrapDataTableVO dataTable = getDataTable(tableRequest, true);
        if (!dataTable.isResult()) {
            throw SecretpadException.of(JobErrorCode.PROJECT_DATA_NOT_EXISTS_ERROR, "check table name not exist");
        }

        //check data table header
        GetProjectJobDataHeaderRequest headerRequest = new GetProjectJobDataHeaderRequest();
        headerRequest.setTableName(tableName);
        headerRequest.setCheckTableHeader(partnerPsiConfigDO.getKeys());
        GrapDataHeaderVO dataHeader = getDataHeader(headerRequest, true);
        if (!dataHeader.isResult()) {
            throw SecretpadException.of(JobErrorCode.PROJECT_TABLE_HEADER_NOT_EXISTS_ERROR, "check table header not exists");
        }

        //convertJob
        ProjectPsiJob projectJob = ProjectPsiJob.genProjectJob(job);
        jobChain.proceed(projectJob);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pauseKusciaJob(StopProjectJobTaskRequest request) {
        LOGGER.info("Opposite party suspends task: {}", request.getJobId());
        ProjectJobDO job = openProjectJob(request.getJobId());

        // check node route
        String platformNodeId = envService.getPlatformNodeId();
        remoteRequestService.checkBothSidesNodeRouteIsReady(platformNodeId, StringUtils.equals(platformNodeId, job.getInitiatorNodeId()) ? job.getPartnerNodeId() : job.getInitiatorNodeId());

        Boolean checkOperation = GraphJobStatus.checkOperation(job.getStatus(), GraphJobOperation.PAUSE);
        if (!checkOperation) {
            throw SecretpadException.of(JobErrorCode.PROJECT_JOB_ACTION_NOT_ALLOWED, GraphJobStatus.getName(job.getStatus()), GraphJobOperation.getName(GraphJobOperation.PAUSE));
        }

        job.pause();
        projectJobRepository.save(job);
        jobStub.deleteJob(Job.DeleteJobRequest.newBuilder().setJobId(job.getUpk().getJobId()).build());
    }

    @Override
    public void pauseJob(StopProjectJobTaskRequest request) {
        LOGGER.info("We suspend the task: {}", request.getJobId());
        ProjectJobDO job = openProjectJob(request.getJobId());

        // check node route
        String platformNodeId = envService.getPlatformNodeId();
        remoteRequestService.checkBothSidesNodeRouteIsReady(platformNodeId, StringUtils.equals(platformNodeId, job.getInitiatorNodeId()) ? job.getPartnerNodeId() : job.getInitiatorNodeId());

        Boolean checkOperation = GraphJobStatus.checkOperation(job.getStatus(), GraphJobOperation.PAUSE);
        if (!checkOperation) {
            throw SecretpadException.of(JobErrorCode.PROJECT_JOB_ACTION_NOT_ALLOWED, GraphJobStatus.getName(job.getStatus()), GraphJobOperation.getName(GraphJobOperation.PAUSE));
        }

        if (StringUtils.equals(platformNodeId, job.getHostNodeId())) {
            pauseKusciaJob(request);
        } else {
            String url = JobManager.HTTP_HEADER + gateway + JobManager.PAUSE_JOB_API;
            remoteRequestService.sendPostJson(request, job.getPartnerNodeId(), url);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void continueKusciaJob(StopProjectJobTaskRequest request) {
        LOGGER.info("Opposite party continues task: {}", request.getJobId());
        ProjectJobDO job = openProjectJob(request.getJobId());

        // check node route
        String platformNodeId = envService.getPlatformNodeId();
        remoteRequestService.checkBothSidesNodeRouteIsReady(platformNodeId, StringUtils.equals(platformNodeId, job.getInitiatorNodeId()) ? job.getPartnerNodeId() : job.getInitiatorNodeId());

        Boolean checkOperation = GraphJobStatus.checkOperation(job.getStatus(), GraphJobOperation.CONTINUE);
        if (!checkOperation) {
            throw SecretpadException.of(JobErrorCode.PROJECT_JOB_ACTION_NOT_ALLOWED, GraphJobStatus.getName(job.getStatus()), GraphJobOperation.getName(GraphJobOperation.CONTINUE));
        }

        Job.DeleteJobResponse deleteJobResponse = jobStub.deleteJob(Job.DeleteJobRequest.newBuilder().setJobId(job.getUpk().getJobId()).build());
        LOGGER.info("delete Job response message: {}", deleteJobResponse.getStatus().getMessage());

        ProjectPsiJob projectJob = ProjectPsiJob.genProjectJob(job);
        jobChain.proceed(projectJob);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void continueJob(StopProjectJobTaskRequest request) {
        LOGGER.info("We continue the task: {}", request.getJobId());
        ProjectJobDO job = openProjectJob(request.getJobId());

        // check node route
        String platformNodeId = envService.getPlatformNodeId();
        remoteRequestService.checkBothSidesNodeRouteIsReady(platformNodeId, StringUtils.equals(platformNodeId, job.getInitiatorNodeId()) ? job.getPartnerNodeId() : job.getInitiatorNodeId());

        Boolean checkOperation = GraphJobStatus.checkOperation(job.getStatus(), GraphJobOperation.CONTINUE);
        if (!checkOperation) {
            throw SecretpadException.of(JobErrorCode.PROJECT_JOB_ACTION_NOT_ALLOWED, GraphJobStatus.getName(job.getStatus()), GraphJobOperation.getName(GraphJobOperation.CONTINUE));
        }
        LOGGER.info("continue job {}", request.getJobId());
        if (StringUtils.equals(platformNodeId, job.getHostNodeId())) {
            continueKusciaJob(request);
        } else {
            String url = JobManager.HTTP_HEADER + gateway + JobManager.CONTINUE_JOB_API;
            remoteRequestService.sendPostJson(request, job.getPartnerNodeId(), url);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createKusciaJob(CreateProjectJobTaskRequest request) {
        LOGGER.info("Opposite party creates a task: {}", request.getJobId());
        FileUtils.fileNameCheck(request.getJobId());
        ProjectJobDO jobDO = saveKusciaJob(request);
        createFile(jobDO);
    }

    @Override
    public void rejectJob(RejectProjectJobTaskRequest request) {
        LOGGER.info("We reject the task: {}", request.getJobId());
        ProjectJobDO job = openProjectJob(request.getJobId());

        // check node route
        String platformNodeId = envService.getPlatformNodeId();
        remoteRequestService.checkBothSidesNodeRouteIsReady(platformNodeId, StringUtils.equals(platformNodeId, job.getInitiatorNodeId()) ? job.getPartnerNodeId() : job.getInitiatorNodeId());

        Boolean checkOperation = GraphJobStatus.checkOperation(job.getStatus(), GraphJobOperation.REJECT);
        if (!checkOperation) {
            throw SecretpadException.of(JobErrorCode.PROJECT_JOB_ACTION_NOT_ALLOWED, GraphJobStatus.getName(job.getStatus()), GraphJobOperation.getName(GraphJobOperation.REJECT));
        }

        if (StringUtils.isNotBlank(request.getRejectMsg())) {
            job.setErrMsg("审核拒绝：拒绝原因" + request.getRejectMsg());
        } else {
            job.setErrMsg("审核拒绝");
        }
        job.setStatus(GraphJobStatus.REJECTED);
        projectJobRepository.save(job);
    }

    @Override
    public void syncHostNodeProjectJobs(List<ProjectJobVO> jobs) {
        if (CollectionUtils.isEmpty(jobs)) {
            LOGGER.warn("request query jobs is empty");
            return;
        }
        List<ProjectJobDO> projectJobDOList = projectJobRepository.findByJobIds(jobs.stream().map(ProjectJobBaseVO::getJobId).toList());
        if (CollectionUtils.isEmpty(projectJobDOList)) {
            LOGGER.warn("edge filter jobs is empty");
            return;
        }
        List<ProjectJobDO> jobDOListIsFinished = projectJobDOList.stream().filter(ProjectJobDO::isFinished).toList();
        if (CollectionUtils.isEmpty(jobDOListIsFinished)) {
            LOGGER.warn("edge finished jobs is empty");
        }
        LOGGER.info("delete finished jobs tmp file");
        jobDOListIsFinished.forEach(job -> deleteFile(job.getUpk().getJobId()));

        List<ProjectJobDO> jobDOListNotFinished = projectJobDOList.stream().filter(job -> !job.isFinished()).toList();
        if (CollectionUtils.isEmpty(jobDOListNotFinished)) {
            LOGGER.info("edge not finished jobs is empty");
            return;
        }

        // job list in edge, it must be not finished
        jobDOListNotFinished.forEach(job -> {
            List<ProjectJobVO> jobVOList = jobs.stream().filter(t -> StringUtils.equalsIgnoreCase(t.getJobId(), job.getUpk().getJobId())).toList();
            ProjectJobVO projectJobVO = jobVOList.get(0);
            job.setStatus(projectJobVO.getStatus());
            if (StringUtils.isNotBlank(projectJobVO.getGmtFinished())) {
                job.setFinishedTime(DateTimes.eightUtcFromRfc3339(projectJobVO.getGmtFinished()));
            }
            if (StringUtils.isNotBlank(projectJobVO.getStartTime())) {
                job.setStartTime(DateTimes.eightUtcFromRfc3339(projectJobVO.getStartTime()));
            }
            job.setErrMsg(projectJobVO.getErrMsg());
            // delete tmp file
            if ((StringUtils.equalsIgnoreCase(GraphJobStatus.FAILED.name(), projectJobVO.getStatus().name()) && !projectJobVO.getInitiatorConfig().getRecoveryConfig().getEnabled())) {
                LOGGER.info("delete not finished jobs tmp file");
                deleteFile(projectJobVO.getJobId());
            }
        });
        projectJobRepository.saveAll(jobDOListNotFinished);
    }

    private void deleteFile(String jobId) {
        FileUtils.deleteAllFile(storeDir + JobManager.PROJECT_JOB_TASK_TMP + jobId);
    }

    @Override
    public String downloadProjectResult(String jobId) {
        Future<ProjectJobDO> projectJobDOFuture = jobManager.openProjectJob(jobId);
        ProjectJobDO job;
        try {
            job = projectJobDOFuture.get();
        } catch (Exception e) {
            LOGGER.error("query project job result is null");
            throw SecretpadException.of(JobErrorCode.PROJECT_JOB_NOT_EXISTS);
        }

        Boolean checkOperation = GraphJobStatus.checkOperation(job.getStatus(), GraphJobOperation.DOWNLOAD_RESULT);
        if (!checkOperation) {
            throw SecretpadException.of(JobErrorCode.PROJECT_JOB_ACTION_NOT_ALLOWED, GraphJobStatus.getName(job.getStatus()), GraphJobOperation.getName(GraphJobOperation.DOWNLOAD_RESULT));
        }

        // get edge node id
        String platformNodeId = envService.getPlatformNodeId();
        // get psiConfig data object
        String psiConfig = StringUtils.equalsIgnoreCase(platformNodeId, job.getInitiatorNodeId()) ?
                job.getInitiatorConfig() : job.getPartnerConfig();
        if (StringUtils.isBlank(psiConfig)) {
            throw SecretpadException.of(JobErrorCode.PROJECT_JOB_RESULT_DOWNLOAD_ERROR, "node psi config not exists");
        }
        PsiConfigDO psiConfigDO = JsonUtils.toJavaObject(psiConfig, PsiConfigDO.class);
        // single receiver and not equals
        if (!psiConfigDO.getProtocolConfig().getBroadcastResult() && !psiConfigDO.getProtocolConfig().getRole().equals(JobConstants.RoleEnum.ROLE_RECEIVER)) {
            throw SecretpadException.of(JobErrorCode.PROJECT_JOB_RESULT_DOWNLOAD_ERROR, "node is not the receiver");
        }
        String enginePath = psiConfigDO.getOutputConfig().getPath();
        String path = StringUtils.isBlank(enginePath) ? "" : enginePath.replace(JobManager.KUSCIA_DATA_PATH, storeDir);
        String dir = storeDir;
        String relativeUri = path.replace(dir, "");

        String hash = UUIDUtils.newUUID();
        Date date = new Date();
        jobResult.put(hash, ProjectJobResultVO.builder().hash(hash).path(path).dir(dir).relativeUri(relativeUri)
                .expirationTime(new Date(date.getTime() + 30000)).build());
        ProjectJobResultVO.builder().build();
        return hash;
    }

    @Override
    public DownloadInfo getloadProjectResult(String hash) {
        ProjectJobResultVO projectJobResultVO = jobResult.get(hash);
        Date date = new Date();
        if (ObjectUtils.isEmpty(projectJobResultVO) || date.compareTo(projectJobResultVO.getExpirationTime()) > 0) {
            jobResult.remove(hash);
            throw SecretpadException.of(JobErrorCode.PROJECT_JOB_RESULT_HASH_EXPIRED_ERROR);
        }
        LOGGER.debug("load project result : {}", projectJobResultVO);
        jobResult.remove(hash);
        String path = projectJobResultVO.getPath();
        String dir = projectJobResultVO.getDir();
        String relativeUri = projectJobResultVO.getRelativeUri();
        return FileUtils.download(path, dir, relativeUri);
    }

    @Override
    public List<ProjectJobVO> queryEdgeProjectJobs(String requestNodeId) {
        String platformNodeId = envService.getPlatformNodeId();
        if (StringUtils.equalsIgnoreCase(requestNodeId, platformNodeId)) {
            return Collections.emptyList();
        }
        List<ProjectJobDO> projectJobDOS = projectJobRepository.queryJobsByHostNodeIdAndRequestNodeId(platformNodeId, requestNodeId);
        return projectJobDOS.stream().map(t -> this.buildProjectJobVO(t, requestNodeId)).collect(Collectors.toList());
    }

    @Override
    public GraphNodeJobLogsVO getProjectJobInFeilLogs(GetProjectJobLogRequest request) {
        ProjectJobDO job = openProjectJob(request.getJobId());

        Boolean checkOperation = GraphJobStatus.checkOperation(job.getStatus(), GraphJobOperation.LOG);
        if (!checkOperation) {
            throw SecretpadException.of(JobErrorCode.PROJECT_JOB_ACTION_NOT_ALLOWED, GraphJobStatus.getName(job.getStatus()), GraphJobOperation.getName(GraphJobOperation.LOG));
        }

        return new GraphNodeJobLogsVO(job.getStatus(),
                getProjectJobLogs(request));
    }


    @Override
    public GrapDataHeaderVO getDataHeader(GetProjectJobDataHeaderRequest request, boolean check) {
        if (check) {
            if (ObjectUtils.isEmpty(request.getCheckTableHeader())) {
                throw SecretpadException.of(JobErrorCode.PROJECT_TABLE_HEADER_NOT_EXISTS_ERROR, "csv table header is empty");
            }
            GrapDataHeaderVO dataHeader = getDataHeader(request);
            ArrayList<String> existHeader = new ArrayList<>(dataHeader.getDataHeader());
            existHeader.retainAll(request.getCheckTableHeader());
            ArrayList<String> absentHeader = new ArrayList<>(request.getCheckTableHeader());
            absentHeader.removeAll(existHeader);
            return GrapDataHeaderVO.builder()
                    .tableName(dataHeader.getTableName())
                    .dataHeader(dataHeader.getDataHeader())
                    .result(absentHeader.isEmpty())
                    .existHeader(existHeader)
                    .absentHeader(absentHeader).build();
        }
        return getDataHeader(request);
    }

    @Override
    public GrapDataTableVO getDataTable(GetProjectJobTableRequest request, boolean check) {
        if (check) {
            if (ObjectUtils.isEmpty(request.getTableName())) {
                throw SecretpadException.of(JobErrorCode.PROJECT_DATA_NOT_EXISTS_ERROR, "csv table name is empty");
            }
            LOGGER.debug("verify the data table exists check table name:{}", request.getTableName());
            FileUtils.fileNameCheck(request.getTableName());
            List<String> dataTable = getDataTable().getDataTable();
            return GrapDataTableVO.builder()
                    .dataTable(dataTable)
                    .result(dataTable.contains(request.getTableName()))
                    .build();
        }
        return GrapDataTableVO.builder()
                .dataTable(getDataTable().getDataTable())
                .build();
    }

    private List<ProjectJobListVO> convert2ListVO(Page<ProjectJobDO> page, String nodeId) {
        List<ProjectJobListVO> projectJobListVOS = new ArrayList<>();
        page.forEach(it -> {
            ProjectJobListVO vo = new ProjectJobListVO();
            vo.setJobId(it.getUpk().getJobId());
            vo.setName(it.getName());
            vo.setStatus(it.getStatus());
            vo.setGmtCreate(DateTimes.toRfc3339(it.getGmtCreate()));
            vo.setSrcNodeId(it.getInitiatorNodeId());
            vo.setDstNodeId(it.getPartnerNodeId());
            //TODO:It will be optimized later.
            PsiConfigDO initiatorConfig = JsonUtils.toJavaObject(it.getInitiatorConfig(), PsiConfigDO.class);
            PsiConfigDO partnerConfig = JsonUtils.toJavaObject(it.getPartnerConfig(), PsiConfigDO.class);
            vo.setInitiatorDataTableInformation(DataTableInformationVo.DataTableInformation.builder()
                    .nodeId(it.getInitiatorNodeId())
                    .dataTableName(getTableName(initiatorConfig.getInputConfig().getPath()))
                    .dataTableCount(initiatorConfig.getDatatableCount())
                    .build());
            vo.setPartnerdstDataTableInformation(DataTableInformationVo.DataTableInformation.builder()
                    .nodeId(it.getPartnerNodeId())
                    .dataTableName(getTableName(partnerConfig.getInputConfig().getPath()))
                    .dataTableCount(partnerConfig.getDatatableCount())
                    .build());
            PsiConfigDO partnerPsiConfigDO = JsonUtils.toJavaObject(it.getPartnerConfig(), PsiConfigDO.class);
            vo.setEnabled(partnerPsiConfigDO.getRecoveryConfig().getEnabled());

            if (StringUtils.isNotBlank(it.getErrMsg())) {
                vo.setErrMsg(it.getErrMsg());
            }

            List<GraphJobOperation> operations = GraphJobStatus.fromStatusToOperation(it.getStatus(), nodeId, it);
            vo.setOperation(operations);
            projectJobListVOS.add(vo);
        });
        return projectJobListVOS;
    }

    private String getTableName(String path) {
        if (!path.contains("/")) {
            return path;
        }
        return path.substring(path.lastIndexOf("/") + 1);
    }

    private ProjectJobVO buildProjectJobVO(ProjectJobDO job, String nodeId) {
        ProjectJobVO vo = new ProjectJobVO();
        vo.setJobId(job.getUpk().getJobId());
        vo.setName(job.getName());
        vo.setDescription(job.getDescription());
        vo.setGmtCreate(DateTimes.toRfc3339(job.getGmtCreate()));
        vo.setStatus(job.getStatus());
        vo.setStartTime(DateTimes.toRfc3339(job.getStartTime()));
        vo.setErrMsg(job.getErrMsg());

        if (StringUtils.equals(job.getStatus().name(), GraphJobStatus.FAILED.name()) || StringUtils.equals(job.getStatus().name(), GraphJobStatus.SUCCEEDED.name())) {
            vo.setGmtFinished(DateTimes.toRfc3339(job.getFinishedTime()));
        }

        CreateProjectJobTaskRequest.PsiConfig partnerConfig = new CreateProjectJobTaskRequest.PsiConfig();
        PsiConfigDO partnerPsiConfigDO = JsonUtils.toJavaObject(job.getPartnerConfig(), PsiConfigDO.class);
        partnerConfig = convertPsiConfigVO(partnerConfig, partnerPsiConfigDO);
        vo.setPartnerConfig(partnerConfig);

        CreateProjectJobTaskRequest.PsiConfig initiatorConfig = new CreateProjectJobTaskRequest.PsiConfig();
        PsiConfigDO initiatorPsiConfigDO = JsonUtils.toJavaObject(job.getInitiatorConfig(), PsiConfigDO.class);
        initiatorConfig = convertPsiConfigVO(initiatorConfig, initiatorPsiConfigDO);
        vo.setInitiatorConfig(initiatorConfig);

        List<GraphJobOperation> operations = GraphJobStatus.fromStatusToOperation(vo.getStatus(), nodeId, job);
        vo.setOperation(operations);
        return vo;
    }

    private CreateProjectJobTaskRequest.PsiConfig convertPsiConfigVO(CreateProjectJobTaskRequest.PsiConfig psiConfig, PsiConfigDO psiConfigDO) {
        BeanUtils.copyProperties(psiConfigDO, psiConfig);

        CreateProjectJobTaskRequest.PsiConfig.InputConfig inputConfig = new CreateProjectJobTaskRequest.PsiConfig.InputConfig();
        String inputConfigPath = psiConfigDO.getInputConfig().getPath();
        inputConfig.setPath(inputConfigPath.substring(inputConfigPath.lastIndexOf("/") + 1));
        inputConfig.setType(psiConfigDO.getInputConfig().getType());
        psiConfig.setInputConfig(inputConfig);
        LOGGER.debug("convert psi configVO input config:{}", inputConfig);
        psiConfig.setDisableAlignment(psiConfigDO.getDisableAlignment());

        CreateProjectJobTaskRequest.PsiConfig.OutputConfig outputConfig = new CreateProjectJobTaskRequest.PsiConfig.OutputConfig();
        String outputConfigPath = psiConfigDO.getOutputConfig().getPath();
        if (StringUtils.isNotBlank(outputConfigPath)) {
            String outputPath = outputConfigPath.substring(outputConfigPath.lastIndexOf("/") + 1);
            outputConfig.setPath(StringUtils.isNotBlank(outputPath) ? outputPath : null);
        }
        outputConfig.setType(psiConfigDO.getOutputConfig().getType());
        psiConfig.setOutputConfig(outputConfig);
        LOGGER.debug("convert psi configVO output config:{}", outputConfig);
        CreateProjectJobTaskRequest.PsiConfig.ContextDescProto contextDescProto = new CreateProjectJobTaskRequest.PsiConfig.ContextDescProto();
        contextDescProto.setRecvTimeoutMs(psiConfigDO.getLinkConfig().getRecvTimeoutMs());
        contextDescProto.setHttpTimeoutMs(psiConfigDO.getLinkConfig().getHttpTimeoutMs());
        psiConfig.setLinkConfig(contextDescProto);

        CreateProjectJobTaskRequest.PsiConfig.ProtocolConfig protocolConfig = new CreateProjectJobTaskRequest.PsiConfig.ProtocolConfig();
        protocolConfig.setProtocol(psiConfigDO.getProtocolConfig().getProtocol());
        protocolConfig.setRole(psiConfigDO.getProtocolConfig().getRole());
        protocolConfig.setBroadcastResult(psiConfigDO.getProtocolConfig().getBroadcastResult());

        PsiConfigDO.ProtocolConfig protocolConfigDO = psiConfigDO.getProtocolConfig();
        if (!ObjectUtils.isEmpty(protocolConfigDO.getEcdhConfig())) {
            CreateProjectJobTaskRequest.PsiConfig.ProtocolConfig.EcdhConfig ecdhConfig = new CreateProjectJobTaskRequest.PsiConfig.ProtocolConfig.EcdhConfig();
            ecdhConfig.setCurve(protocolConfigDO.getEcdhConfig().getCurve());
            protocolConfig.setEcdhConfig(ecdhConfig);
        }
        if (!ObjectUtils.isEmpty(protocolConfigDO.getKkrtConfig())) {
            CreateProjectJobTaskRequest.PsiConfig.ProtocolConfig.KkrtConfig kkrtConfig = new CreateProjectJobTaskRequest.PsiConfig.ProtocolConfig.KkrtConfig();
            kkrtConfig.setBucketSize(protocolConfigDO.getKkrtConfig().getBucketSize());
            protocolConfig.setKkrtConfig(kkrtConfig);
        }
        if (!ObjectUtils.isEmpty(protocolConfigDO.getRr22Config())) {
            CreateProjectJobTaskRequest.PsiConfig.ProtocolConfig.Rr22Config rr22Config = new CreateProjectJobTaskRequest.PsiConfig.ProtocolConfig.Rr22Config();
            rr22Config.setBucketSize(protocolConfigDO.getRr22Config().getBucketSize());
            rr22Config.setLowCommMode(protocolConfigDO.getRr22Config().getLowCommMode());
            protocolConfig.setRr22Config(rr22Config);
        }

        psiConfig.setProtocolConfig(protocolConfig);
        LOGGER.debug("convert psi configVO protocol config:{}", protocolConfig);

        CreateProjectJobTaskRequest.PsiConfig.RecoveryConfig config = new CreateProjectJobTaskRequest.PsiConfig.RecoveryConfig();
        config.setEnabled(psiConfigDO.getRecoveryConfig().getEnabled());
        config.setFolder(psiConfigDO.getRecoveryConfig().getFolder());
        psiConfig.setRecoveryConfig(config);
        LOGGER.debug("convert psi configVO recovery config:{}", config);
        psiConfig.setOutputDifference(psiConfigDO.getOutputDifference());
        LOGGER.debug("Get data table level from psi configuration, path:{}, data count: {}", psiConfigDO.getInputConfig().getPath(), psiConfigDO.getDatatableCount());
        psiConfig.setDatatableCount(psiConfigDO.getDatatableCount());
        LOGGER.debug("convert psi configVO psi config:{}", psiConfig);
        return psiConfig;
    }

    private ProjectJobDO saveKusciaJob(CreateProjectJobTaskRequest request) {
        ProjectJobDO jobDO = new ProjectJobDO();
        ProjectJobDO.UPK upk = new ProjectJobDO.UPK();
        upk.setJobId(request.getJobId());
        jobDO.setUpk(upk);
        jobDO.setName(request.getName());
        jobDO.setDescription(request.getDescription());

        CreateProjectJobTaskRequest.PsiConfig partnerConfig = request.getPartnerConfig();
        PsiConfigDO partnerPsiConfigDO = convertPsiConfig(partnerConfig, jobDO.getUpk().getJobId());
        jobDO.setPartnerConfig(JsonUtils.toJSONString(partnerPsiConfigDO));

        CreateProjectJobTaskRequest.PsiConfig initiatorConfig = request.getInitiatorConfig();
        PsiConfigDO initatorPsiConfigDO = convertPsiConfig(initiatorConfig, request.getJobId());
        jobDO.setInitiatorConfig(JsonUtils.toJSONString(initatorPsiConfigDO));

        jobDO.setInitiatorNodeId(request.getInitiatorConfig().getNodeId());
        jobDO.setPartnerNodeId(request.getPartnerConfig().getNodeId());
        jobDO.setHostNodeId(request.getPartnerConfig().getNodeId());
        jobDO.setStatus(GraphJobStatus.PENDING_REVIEW);
        projectJobRepository.save(jobDO);
        return jobDO;
    }

    private List<String> removeExtraCharacters(List<String> str) {
        return str.stream().map(s -> s.trim().replaceAll("^\"|\"$", "").replaceAll("\"\"", "")).collect(Collectors.toList());
    }

    public GrapDataTableVO getDataTable() {
        List<String> data = FileUtils.traverseDirectories(new File(JobManager.CSV_DATA_PATH), ".csv", FileUtils.FILE_NAME);
        if (data == null) {
            throw SecretpadException.of(JobErrorCode.PROJECT_DATA_PATH_NOT_EXISTS_ERROR);
        }
        return GrapDataTableVO.builder()
                .dataTable(data)
                .build();
    }

    private boolean deleteFile(File file) {
        if (file == null || !file.exists()) {
            LOGGER.error("File deletion failed. Please check if the file exists and if the file path is correct");
            return false;
        }
        File[] files = file.listFiles();
        for (File f : files) {
            if (f.isDirectory()) {
                deleteFile(f);
            } else {
                f.delete();
            }
        }
        file.delete();
        return true;
    }

    private GrapDataHeaderVO getDataHeader(GetProjectJobDataHeaderRequest request) {
        LOGGER.info("get data header check data name: {}", request.getTableName());
        FileUtils.fileNameCheck(request.getTableName());
        String filepath = JobManager.CSV_DATA_PATH + File.separator + request.getTableName();
        File file = new File(filepath);
        if (!file.exists()) {
            throw SecretpadException.of(JobErrorCode.PROJECT_DATA_NOT_EXISTS_ERROR, request.getTableName() + " csv data not exists");
        }
        BufferedReader reader;
        List<String> collect;
        try (FileReader fileReader = new FileReader(filepath)) {
            reader = new BufferedReader(fileReader);
            reader.mark(1);
            if (reader.read() != 0xFEFF) {
                reader.reset();
            }
            collect = Arrays.stream(reader.readLine().split(",")).collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return GrapDataHeaderVO.builder()
                .tableName(file.getName())
                .dataHeader(removeExtraCharacters(collect))
                .build();
    }

    private List<String> getProjectJobLogs(GetProjectJobLogRequest request) {
        String directoryPath = JobManager.PROJECT_JOB_LOGS;
        LOGGER.info("get project job logs check job id: {}", request.getJobId());
        FileUtils.fileNameCheck(request.getJobId());
        String pattern = envService.getPlatformNodeId() + "_" + request.getJobId() + "-0";
        File directory = new File(directoryPath);
        FilenameFilter filenameFilter = (dir, name) -> name.startsWith(pattern);
        File[] matchingFiles = directory.listFiles(filenameFilter);
        String fileName = null;
        List<String> projectJobLogs = new ArrayList<>();
        long lastModified = 0L;
        if (matchingFiles != null) {
            for (File file : matchingFiles) {
                if (file.lastModified() > lastModified) {
                    fileName = file.getName();
                    lastModified = file.lastModified();
                }
            }
        }
        List<String> filePaths = FileUtils.traverseDirectories(new File(directoryPath + File.separator + fileName + File.separator + "secretflow"), ".log", FileUtils.FILE_PATH);
        if (filePaths == null) {
            LOGGER.warn("{} task log path not exit", request.getJobId());
            return Collections.emptyList();
        }
        try {
            for (String filePath : filePaths) {
                projectJobLogs.addAll(Files.lines(Paths.get(filePath)).collect(Collectors.toList()));
            }
        } catch (Exception e) {
            LOGGER.error("{} task log not exit", request.getJobId());
            return Collections.emptyList();
        }

        if (ObjectUtils.isEmpty(fileName)) {
            LOGGER.warn("{} task log not exit", request.getJobId());
            return Collections.emptyList();
        }
        return projectJobLogs;
    }

    private void createFile(ProjectJobDO jobDO) {
        String resFilepath = storeDir + JobManager.PROJECT_JOB_TASK_RES + jobDO.getUpk().getJobId() + File.separator;
        File resFile = new File(resFilepath);
        resFile.mkdirs();

        String tmpFilepath = storeDir + JobManager.PROJECT_JOB_TASK_TMP + jobDO.getUpk().getJobId() + File.separator;
        File tmpFile = new File(tmpFilepath);
        tmpFile.mkdirs();
    }


    private CreateProjectJobTaskRequest convertRequest(CreateProjectJobRequest request, String jobId) {
        CreateProjectJobTaskRequest taskRequest = new CreateProjectJobTaskRequest();
        taskRequest.setName(request.getName());
        taskRequest.setDescription(request.getDescription());

        CreateProjectJobTaskRequest.PsiConfig initiatorConfig = convertPsiConfig(request, request.getInitiatorConfig(), jobId);
        if (initiatorConfig.getProtocolConfig().getBroadcastResult()) {
            initiatorConfig.getProtocolConfig().setRole(JobConstants.RoleEnum.ROLE_SENDER);
        }
        taskRequest.setInitiatorConfig(initiatorConfig);

        CreateProjectJobTaskRequest.PsiConfig partnerConfig = convertPsiConfig(request, request.getPartnerConfig(), jobId);
        taskRequest.setPartnerConfig(partnerConfig);

        return taskRequest;
    }

    private CreateProjectJobTaskRequest.PsiConfig convertPsiConfig(CreateProjectJobRequest request, CreateProjectJobRequest.PsiConfig requestPsiConfig, String jobId) {
        CreateProjectJobTaskRequest.PsiConfig psiConfig = new CreateProjectJobTaskRequest.PsiConfig();
        psiConfig.setNodeId(requestPsiConfig.getNodeId());

        CreateProjectJobRequest.AdvancedConfig.ProtocolConfig requestProtocolConfig = request.getAdvancedConfig().getProtocolConfig();
        LOGGER.debug("convert request psi config protocol config:{}", requestProtocolConfig);
        CreateProjectJobTaskRequest.PsiConfig.ProtocolConfig protocolConfig = new CreateProjectJobTaskRequest.PsiConfig.ProtocolConfig();
        protocolConfig.setProtocol(requestProtocolConfig.getProtocol());

        if (!ObjectUtils.isEmpty(requestProtocolConfig.getEcdhConfig())) {
            CreateProjectJobTaskRequest.PsiConfig.ProtocolConfig.EcdhConfig ecdhConfig = new CreateProjectJobTaskRequest.PsiConfig.ProtocolConfig.EcdhConfig();
            ecdhConfig.setCurve(requestProtocolConfig.getEcdhConfig().getCurve());
            protocolConfig.setEcdhConfig(ecdhConfig);
        }
        if (!ObjectUtils.isEmpty(requestProtocolConfig.getRr22Config())) {
            CreateProjectJobTaskRequest.PsiConfig.ProtocolConfig.Rr22Config rr22Config = new CreateProjectJobTaskRequest.PsiConfig.ProtocolConfig.Rr22Config();
            rr22Config.setBucketSize(requestProtocolConfig.getRr22Config().getBucketSize());
            rr22Config.setLowCommMode(requestProtocolConfig.getRr22Config().getLowCommMode());
            protocolConfig.setRr22Config(rr22Config);
        }
        if (!ObjectUtils.isEmpty(requestProtocolConfig.getKkrtConfig())) {
            CreateProjectJobTaskRequest.PsiConfig.ProtocolConfig.KkrtConfig kkrtConfig = new CreateProjectJobTaskRequest.PsiConfig.ProtocolConfig.KkrtConfig();
            kkrtConfig.setBucketSize(requestProtocolConfig.getKkrtConfig().getBucketSize());
            protocolConfig.setKkrtConfig(kkrtConfig);
        }


        List<String> broadcastResult = request.getOutputConfig().getBroadcastResult();
        LOGGER.debug("convert request psi config broadcast result:{}", broadcastResult);

        CreateProjectJobTaskRequest.PsiConfig.OutputConfig outputConfig = new CreateProjectJobTaskRequest.PsiConfig.OutputConfig();

        if (broadcastResult.size() == 2) {
            protocolConfig.setRole(JobConstants.RoleEnum.ROLE_RECEIVER);
            protocolConfig.setBroadcastResult(true);
            outputConfig.setPath(request.getOutputConfig().getPath());
        } else {
            if (broadcastResult.contains(psiConfig.getNodeId())) {
                protocolConfig.setRole(JobConstants.RoleEnum.ROLE_RECEIVER);
                outputConfig.setPath(request.getOutputConfig().getPath());
            } else {
                protocolConfig.setRole(JobConstants.RoleEnum.ROLE_SENDER);
            }
            protocolConfig.setBroadcastResult(false);
        }
        psiConfig.setProtocolConfig(protocolConfig);
        LOGGER.debug("convert request psi config protocol config:{}", protocolConfig);
        psiConfig.setOutputConfig(outputConfig);
        LOGGER.debug("convert request psi config output config:{}", outputConfig);
        CreateProjectJobTaskRequest.PsiConfig.InputConfig inputConfig = new CreateProjectJobTaskRequest.PsiConfig.InputConfig();
        inputConfig.setPath(requestPsiConfig.getPath());
        psiConfig.setInputConfig(inputConfig);
        LOGGER.debug("convert request psi config input config:{}", inputConfig);
        CreateProjectJobRequest.AdvancedConfig requestAdvancedConfig = request.getAdvancedConfig();
        LOGGER.debug("convert request psi config advanced config:{}", requestAdvancedConfig);
        CreateProjectJobTaskRequest.PsiConfig.ContextDescProto contextDescProto = new CreateProjectJobTaskRequest.PsiConfig.ContextDescProto();
        contextDescProto.setHttpTimeoutMs(requestAdvancedConfig.getLinkConfig());
        contextDescProto.setRecvTimeoutMs(requestAdvancedConfig.getLinkConfig());
        psiConfig.setLinkConfig(contextDescProto);
        LOGGER.debug("convert request psi config context desc proto:{}", contextDescProto);
        psiConfig.setKeys(requestPsiConfig.getKeys());
        psiConfig.setAdvancedJoinType(requestAdvancedConfig.getAdvancedJoinType());
        psiConfig.setOutputDifference(requestAdvancedConfig.getOutputDifference());
        psiConfig.setDisableAlignment(requestAdvancedConfig.getDisableAlignment());
        psiConfig.setSkipDuplicatesCheck(requestAdvancedConfig.getSkipDuplicatesCheck());
        LOGGER.info("data table name:{}, data interval: {}", requestPsiConfig.getPath(), DataServiceImpl.dataTableCountCache.get(DataServiceImpl.spliceNodeTable(psiConfig.getNodeId(), requestPsiConfig.getPath())));
        psiConfig.setDatatableCount(DataServiceImpl.dataTableCountCache.get(DataServiceImpl.spliceNodeTable(psiConfig.getNodeId(), requestPsiConfig.getPath())));

        CreateProjectJobTaskRequest.PsiConfig.RecoveryConfig recoveryConfig = new CreateProjectJobTaskRequest.PsiConfig.RecoveryConfig();
        recoveryConfig.setEnabled(request.getAdvancedConfig().getRecoveryEnabled());
        recoveryConfig.setFolder(JobManager.KUSCIA_DATA_PATH + JobManager.PROJECT_JOB_TASK_TMP + jobId + File.separator);
        psiConfig.setRecoveryConfig(recoveryConfig);
        LOGGER.debug("convert psi config result:{}", psiConfig);
        return psiConfig;
    }

    /**
     * Open the project job information by projectId and jobId
     *
     * @param jobId target jobId
     * @return project job data object
     */
    private ProjectJobDO openProjectJob(String jobId) {
        Optional<ProjectJobDO> jobOpt = projectJobRepository.findByJobId(jobId);
        if (jobOpt.isEmpty()) {
            throw SecretpadException.of(JobErrorCode.PROJECT_JOB_NOT_EXISTS);
        }
        return jobOpt.get();
    }
}

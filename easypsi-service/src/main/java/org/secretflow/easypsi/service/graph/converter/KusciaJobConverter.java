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

package org.secretflow.easypsi.service.graph.converter;

import org.secretflow.easypsi.common.util.ProtoUtils;
import org.secretflow.easypsi.service.EnvService;
import org.secretflow.easypsi.service.constant.JobConstants;
import org.secretflow.easypsi.service.model.graph.ProjectPsiJob;
import org.secretflow.easypsi.service.model.project.CreateProjectJobTaskRequest;

import com.google.protobuf.util.JsonFormat;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.secretflow.proto.component.Data;
import org.secretflow.proto.kuscia.LaunchConfigOuterClass;
import org.secretflow.proto.kuscia.PsiConfig;
import org.secretflow.proto.kuscia.PsiTaskConfig;
import org.secretflow.v1alpha1.kusciaapi.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Job converter for message in apiLite
 *
 * @author yansi
 * @date 2023/5/30
 */
@Slf4j
@Component
@ConfigurationProperties(prefix = "sfcluster-desc")
public class KusciaJobConverter implements JobConverter {
    @Value("${job.max-parallelism:1}")
    private int maxParallelism;

    @Autowired
    private EnvService envService;


    /**
     * Converter create job request from project job
     *
     * @param job project job class
     * @return create job request message
     */
    public Job.CreateJobRequest psiConverter(ProjectPsiJob job) {
        List<ProjectPsiJob.JobTask> tasks = job.getTasks();
        List<Job.Task> jobTasks = new ArrayList<>();
        String initiator = "";
        if (!CollectionUtils.isEmpty(tasks)) {
            for (ProjectPsiJob.JobTask task : tasks) {
                String taskId = task.getTaskId();
                List<Job.Party> taskParties = new ArrayList<>();
                List<String> parties = task.getParties();
                if (!CollectionUtils.isEmpty(parties)) {
                    //Kuscia's definition of initiator and partner is opposite to that of the ezpsi platform
                    initiator = job.getPartnerConfig().getNodeId();
                    taskParties = parties.stream().map(party -> Job.Party.newBuilder().setDomainId(party).build()).collect(Collectors.toList());
                }
                String taskInputConfig = renderPsiTaskInputConfig(job);
                Job.Task.Builder jobTaskBuilder = Job.Task.newBuilder()
                        .setTaskId(taskId)
                        .setAlias(taskId)
                        .setAppImage(JobConstants.PSI_IMAGE)
                        .addAllParties(taskParties)
                        .setTaskInputConfig(taskInputConfig);
                jobTasks.add(jobTaskBuilder.build());
            }
        }
        return Job.CreateJobRequest.newBuilder()
                .setJobId(job.getJobId())
                .setInitiator(initiator)
                .setMaxParallelism(maxParallelism)
                .addAllTasks(jobTasks)
                .build();
    }

    private String renderPsiTaskInputConfig(ProjectPsiJob job) {
        JsonFormat.TypeRegistry typeRegistry = JsonFormat.TypeRegistry.newBuilder().add(Data.IndividualTable.getDescriptor()).build();

        CreateProjectJobTaskRequest.PsiConfig partnerConfig = job.getPartnerConfig();
        CreateProjectJobTaskRequest.PsiConfig initiatorConfig = job.getInitiatorConfig();


        org.secretflow.easypsi.common.constant.JobConstants.RoleEnum role =
                initiatorConfig.getLeftSide().equals(initiatorConfig.getNodeId()) ? initiatorConfig.getProtocolConfig().getRole() : partnerConfig.getProtocolConfig().getRole() ;

        PsiTaskConfig.PsiConfig partnerInputConfig = getInputConfig(partnerConfig,role);
        PsiTaskConfig.PsiConfig initiatorInputConfig = getInputConfig(initiatorConfig,role);



        PsiTaskConfig.ContextDescProto PartcontextDescProto = PsiTaskConfig.ContextDescProto.newBuilder()
                .setRecvTimeoutMs(Long.parseLong(partnerConfig.getLinkConfig().getRecvTimeoutMs()) * 1000)
                .setHttpTimeoutMs(Integer.parseInt(partnerConfig.getLinkConfig().getRecvTimeoutMs()) * 1000)
                .build();

        PsiTaskConfig.ContextDescProto InitContextDescProto = PsiTaskConfig.ContextDescProto.newBuilder()
                .setRecvTimeoutMs(Long.parseLong(initiatorConfig.getLinkConfig().getRecvTimeoutMs()) * 1000)
                .setHttpTimeoutMs(Integer.parseInt(initiatorConfig.getLinkConfig().getRecvTimeoutMs()) * 1000)
                .build();

        Map<String, LaunchConfigOuterClass.LaunchConfig> launchConfigMap = new HashMap<>();
        launchConfigMap.put(partnerConfig.getNodeId(), LaunchConfigOuterClass.LaunchConfig.newBuilder()
                .setLinkConfig(PartcontextDescProto)
                .setPsiConfig(partnerInputConfig)
                .build());
        launchConfigMap.put(initiatorConfig.getNodeId(), LaunchConfigOuterClass.LaunchConfig.newBuilder()
                .setLinkConfig(InitContextDescProto)
                .setPsiConfig(initiatorInputConfig)
                .build());

        LaunchConfigOuterClass.PsiTaskConfigMap psiTaskConfigMap = LaunchConfigOuterClass.PsiTaskConfigMap.newBuilder()
                .putAllSfPsiConfigMap(launchConfigMap)
                .build();

        return ProtoUtils.toJsonString(psiTaskConfigMap, typeRegistry);
    }

    @NotNull
    private PsiTaskConfig.PsiConfig getInputConfig(CreateProjectJobTaskRequest.PsiConfig psiConfig, org.secretflow.easypsi.common.constant.JobConstants.RoleEnum role) {
        CreateProjectJobTaskRequest.PsiConfig.ProtocolConfig psiProtocolConfig = psiConfig.getProtocolConfig();

        PsiTaskConfig.ProtocolConfig protocolConfig = PsiTaskConfig.ProtocolConfig.newBuilder()
                .setProtocol(PsiTaskConfig.Protocol.valueOf(psiProtocolConfig.getProtocol().name()))
                .setRole(PsiTaskConfig.Role.valueOf(psiProtocolConfig.getRole().name()))
                .setBroadcastResult(psiProtocolConfig.getBroadcastResult())
                .setEcdhConfig(PsiTaskConfig.EcdhConfig.newBuilder().setCurve(
                        ObjectUtils.isEmpty(psiProtocolConfig.getEcdhConfig()) ? PsiConfig.CurveType.valueOf(org.secretflow.easypsi.common.constant.JobConstants.CurveType.CURVE_FOURQ.name())
                                : PsiConfig.CurveType.valueOf(psiProtocolConfig.getEcdhConfig().getCurve())))
                .setKkrtConfig(PsiTaskConfig.KkrtConfig.newBuilder().setBucketSize(
                        ObjectUtils.isEmpty(psiProtocolConfig.getKkrtConfig()) ? Long.parseLong(org.secretflow.easypsi.common.constant.JobConstants.BUCKET_SIZE)
                                : Long.parseLong(psiProtocolConfig.getKkrtConfig().getBucketSize())))
                .setRr22Config(PsiTaskConfig.Rr22Config.newBuilder()
                        .setBucketSize(ObjectUtils.isEmpty(psiProtocolConfig.getRr22Config()) ? Long.parseLong(org.secretflow.easypsi.common.constant.JobConstants.BUCKET_SIZE)
                                : Long.parseLong(psiProtocolConfig.getRr22Config().getBucketSize()))
                        .setLowCommMode(!ObjectUtils.isEmpty(psiProtocolConfig.getRr22Config()) && psiProtocolConfig.getRr22Config().getLowCommMode()))
                .build();
        log.debug("get input config protocol config:{}", protocolConfig);
        CreateProjectJobTaskRequest.PsiConfig.InputConfig psiInputConfig = psiConfig.getInputConfig();
        PsiTaskConfig.InputConfig inputConfig = PsiTaskConfig.InputConfig.newBuilder()
                .setType(PsiTaskConfig.IoType.valueOf(psiInputConfig.getType()))
                .setPath(psiInputConfig.getPath())
                .build();
        log.debug("get input config psi input config:{}", psiInputConfig);
        CreateProjectJobTaskRequest.PsiConfig.OutputConfig psiOutputConfig = psiConfig.getOutputConfig();
        PsiTaskConfig.OutputConfig outputConfig = PsiTaskConfig.OutputConfig.newBuilder()
                .setType(PsiTaskConfig.IoType.valueOf(psiOutputConfig.getType()))
                .setPath(psiOutputConfig.getPath())
                .build();
        log.debug("get input config psi output config:{}", psiOutputConfig);
        CreateProjectJobTaskRequest.PsiConfig.RecoveryConfig psiRecoveryConfig = psiConfig.getRecoveryConfig();
        PsiTaskConfig.RecoveryConfig recoveryConfig = PsiTaskConfig.RecoveryConfig.newBuilder()
                .setEnabled(psiRecoveryConfig.getEnabled())
                .setFolder(psiRecoveryConfig.getFolder())
                .build();
        log.debug("get input config psi recovery config:{}", psiRecoveryConfig);

        PsiTaskConfig.PsiConfig receiverInputConfig = PsiTaskConfig.PsiConfig.newBuilder()
                .setProtocolConfig(protocolConfig)
                .setInputConfig(inputConfig)
                .setOutputConfig(outputConfig)
                .addAllKeys(psiConfig.getKeys())
                .setSkipDuplicatesCheck(!psiConfig.getSkipDuplicatesCheck())
                .setDisableAlignment(!psiConfig.getDisableAlignment())
                .setRecoveryConfig(recoveryConfig)
                .setAdvancedJoinType(PsiTaskConfig.PsiConfig.AdvancedJoinType.valueOf(psiConfig.getAdvancedJoinType().name()))
                .setLeftSide(PsiTaskConfig.Role.valueOf(role.name()))
                .build();
        log.debug("get input config receiver input config:{}", receiverInputConfig);
        return receiverInputConfig;
    }

}

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

package org.secretflow.secretpad.service.model.graph;

import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.persistence.entity.ProjectJobDO;
import org.secretflow.secretpad.persistence.model.GraphNodeTaskStatus;
import org.secretflow.secretpad.service.model.project.CreateProjectJobTaskRequest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Project job
 *
 * @author guyu
 * @date 2023/10/31
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectPsiJob implements Serializable {
    /**
     * project id
     */
    private String projectId;
    /**
     * job id
     */
    private String jobId;
    /**
     * name
     */
    private String name;

    /**
     * description
     */
    private String description;

    /**
     * initiatorConfig
     */
    private CreateProjectJobTaskRequest.PsiConfig initiatorConfig;

    /**
     * partnerConfig
     */
    private CreateProjectJobTaskRequest.PsiConfig partnerConfig;

    /**
     * Job task list
     */
    private List<JobTask> tasks;

    private LocalDateTime startTime;

    public static ProjectPsiJob genProjectJob(ProjectJobDO job) {
        ProjectPsiJob psiJob = new ProjectPsiJob();
        BeanUtils.copyProperties(job, psiJob);
        psiJob.setJobId(job.getUpk().getJobId());
        psiJob.setInitiatorConfig(JsonUtils.toJavaObject(job.getInitiatorConfig(), CreateProjectJobTaskRequest.PsiConfig.class));
        psiJob.setPartnerConfig(JsonUtils.toJavaObject(job.getPartnerConfig(), CreateProjectJobTaskRequest.PsiConfig.class));
        psiJob.setStartTime(LocalDateTime.now(ZoneId.of("UTC")));

        List<JobTask> tasks = new ArrayList<>();
        List<String> parties = new ArrayList<>();
        parties.add(job.getInitiatorNodeId());
        parties.add(job.getPartnerNodeId());
        JobTask task = JobTask.builder()
                .projectId(job.getUpk().getProjectId())
                .taskId(job.getUpk().getJobId())
                .parties(parties)
                .build();
        tasks.add(task);
        psiJob.setTasks(tasks);

        return psiJob;
    }

    public static ProjectJobDO toDO(ProjectPsiJob job) {
        ProjectJobDO jobDO = new ProjectJobDO();
        BeanUtils.copyProperties(job, jobDO);
        return jobDO;
    }

    /**
     * Job task
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class JobTask implements Serializable {
        /**
         * Task id
         */
        private String taskId;
        /**
         * parties
         */
        private List<String> parties;
        /**
         * Graph node task status
         */
        private GraphNodeTaskStatus status;
        /**
         * The dependencies of the task
         */
        private List<String> dependencies;

        private String projectId;
    }
}

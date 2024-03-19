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

package org.secretflow.easypsi.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.secretflow.easypsi.persistence.converter.SqliteLocalDateTimeConverter;
import org.secretflow.easypsi.persistence.model.GraphJobStatus;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Project job data object
 *
 * @author yansi
 * @date 2023/5/30
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "project_job")
@SQLDelete(sql = "update project_job set is_deleted = 1 where job_id = ? and project_id = ?")
@Where(clause = "is_deleted = 0")
public class ProjectJobDO extends BaseAggregationRoot<ProjectJobDO> {

    /**
     * Project job unique primary key
     */
    @EmbeddedId
    private ProjectJobDO.UPK upk;

    /**
     * Project job name
     */
    @Column(name = "name", nullable = false, length = 40)
    private String name;

    /**
     * Project graph job status
     * When created, it must be running.
     */
    @Column(name = "status", nullable = false)
    @Builder.Default
    @Enumerated(value = EnumType.STRING)
    private GraphJobStatus status = GraphJobStatus.RUNNING;

    /**
     * Project job finish time
     * NOTE: this time is UTC time
     */
    @Column(name = "finished_time", nullable = true)
    @Convert(converter = SqliteLocalDateTimeConverter.class)
    private LocalDateTime finishedTime;

    /**
     * Project job error message
     */
    @Column(name = "err_msg", nullable = true)
    private String errMsg;

    /**
     * Initiator node id
     */
    @Column(name = "initiator_node_id", nullable = true)
    private String initiatorNodeId;

    /**
     * Partner node id
     */
    @Column(name = "partner_node_id", nullable = true)
    private String partnerNodeId;

    /**
     * Host node id
     */
    @Column(name = "host_node_id", nullable = true)
    private String hostNodeId;

    /**
     * Description
     */
    @Column(name = "description", nullable = true)
    private String description;

    /**
     * Project job start time
     * NOTE: this time is UTC time
     */
    @Column(name = "start_time", nullable = true)
    @Convert(converter = SqliteLocalDateTimeConverter.class)
    private LocalDateTime startTime;

    /**
     * Receiver config
     */
    @Column(name = "initiator_config", nullable = true)
    private String initiatorConfig;

    /**
     * Sender config
     */
    @Column(name = "partner_config", nullable = true)
    private String partnerConfig;

    /**
     * Whether the graph job status is finished
     *
     * @return whether finished
     */
    public boolean isFinished() {
        return GraphJobStatus.checkJobFinalStatus(this.status.name());
    }

    /**
     * Stop the graph job and associated tasks
     */
    public void stop(String errMsg) {
        // if job status is not PAUSED then set FAILED
        this.setStatus(GraphJobStatus.CANCELED);
        this.setErrMsg(errMsg);
    }

    /**
     * Pause the graph job and associated tasks
     */
    public void pause() {
        this.setStatus(GraphJobStatus.PAUSED);
    }

    /**
     * Project job unique primary key
     */
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UPK implements Serializable {
        /**
         * Project id
         */
        @Column(name = "project_id", nullable = false, length = 64)
        private String projectId = "ezpsi";
        /**
         * Job id
         */
        @Column(name = "job_id", nullable = false, length = 64)
        private String jobId;
    }

}

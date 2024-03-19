/*
 * Copyright 2024 Ant Group Co., Ltd.
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
package org.secretflow.easypsi.service.model.project;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.secretflow.easypsi.persistence.model.GraphJobOperation;
import org.secretflow.easypsi.persistence.model.GraphJobStatus;

import java.util.List;

/**
 * @author chixian
 * @date 2024/03/05
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectJobListByBlackScreenVO {
    /**
     * Job id
     */
    @Schema(description = "job id")
    private String jobId;
    /**
     * name
     */
    @Schema(description = "job name")
    private String name;

    /**
     * srcNodeId
     */
    @Schema(description = "srcNodeId")
    private String srcNodeId;

    /**
     * dstNodeId
     */
    @Schema(description = "dstNodeId")
    private String dstNodeId;

    /**
     * operation
     */
    @Schema(description = "operation")
    private List<GraphJobOperation> operation;

    /**
     * Job status
     */
    @Schema(description = "job status")
    private GraphJobStatus status;
    /**
     * Job start time
     */
    @Schema(description = "job start time")
    private String gmtCreate;
    /**
     * Job finish time
     */
    @Schema(description = "job finish time")
    private String gmtFinished;


    /**
     * Job error message
     */
    @Schema(description = "job error message")
    private String errMsg;

    public static ProjectJobListByBlackScreenVO from(ProjectJobListVO projectJobListVO){
        return ProjectJobListByBlackScreenVO.builder()
                .jobId(projectJobListVO.getJobId())
                .name(projectJobListVO.getName())
                .srcNodeId(projectJobListVO.getSrcNodeId())
                .dstNodeId(projectJobListVO.getDstNodeId())
                .operation(projectJobListVO.getOperation())
                .status(projectJobListVO.getStatus())
                .gmtCreate(projectJobListVO.getGmtCreate())
                .gmtFinished(projectJobListVO.getGmtFinished())
                .errMsg(projectJobListVO.getErrMsg())
                .build();
    }
}

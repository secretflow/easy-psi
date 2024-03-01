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

package org.secretflow.secretpad.service.model.project;

import org.secretflow.secretpad.persistence.model.GraphJobOperation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Project job view object
 *
 * @author jiezi
 * @date 2023/5/31
 */
@Getter
@Setter
public class ProjectJobVO extends ProjectJobBaseVO {
    /**
     * name
     */
    @Schema(description = "job name")
    private String name;

    /**
     * description
     */
    @Schema(description = "description")
    private String description;

    /**
     * initiatorConfig
     */
    @Schema(description = "initiatorConfig")
    private CreateProjectJobTaskRequest.PsiConfig initiatorConfig;

    /**
     * partnerConfig
     */
    @Schema(description = "partnerConfig")
    private CreateProjectJobTaskRequest.PsiConfig partnerConfig;

    @Schema(description = "job start time")
    private String startTime;

    @Schema(description = "operation")
    private List<GraphJobOperation> operation;
}

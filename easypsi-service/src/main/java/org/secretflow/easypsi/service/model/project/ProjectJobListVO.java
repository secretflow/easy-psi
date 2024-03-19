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

package org.secretflow.easypsi.service.model.project;

import org.secretflow.easypsi.persistence.model.GraphJobOperation;
import org.secretflow.easypsi.service.model.data.DataTableInformationVo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

/**
 * Project job list view object
 *
 * @author guyu
 * @date 2023/10/24
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectJobListVO extends ProjectJobBaseVO {
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
     * enabled
     */
    @Schema(description = "enabled")
    private Boolean enabled;

    /**
     * DataTableInformation
     */
    @Schema(description = "initiatorDataTableInformation")
    private DataTableInformationVo.DataTableInformation initiatorDataTableInformation;

    /**
     * DataTableInformation
     */
    @Schema(description = "partnerdstDataTableInformation")
    private DataTableInformationVo.DataTableInformation partnerdstDataTableInformation;

    /**
     * dataTableConfirmation
     */
    @Schema(description = "dataTableConfirmation")
    private Boolean dataTableConfirmation;
}

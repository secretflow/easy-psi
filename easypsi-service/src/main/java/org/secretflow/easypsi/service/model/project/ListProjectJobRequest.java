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

import org.secretflow.easypsi.common.constant.DatabaseConstants;
import org.secretflow.easypsi.persistence.model.GraphJobStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

/**
 * List project job request
 *
 * @author yansi
 * @date 2023/5/25
 */
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ListProjectJobRequest extends PageRequest {

    @Schema(description = "status filter")
    private List<GraphJobStatus> statusFilter;

    @Schema(description = "search")
    private String search;

    @Schema(description = "sortKey")
    private String sortKey = DatabaseConstants.GMT_CREATE;

    @Schema(description = "sortType")
    private String sortType = DatabaseConstants.DEFAULT_SORT_TYPE;
}

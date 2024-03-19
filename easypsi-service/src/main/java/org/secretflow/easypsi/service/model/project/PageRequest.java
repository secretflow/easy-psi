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

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

/**
 * Paging query request
 *
 * @author yansi
 * @date 2023/5/25
 */
@Getter
@Setter
public class PageRequest {
    /**
     * What page is currently requested? Note that starting at 1 represents the first page
     */
    @Schema(description = "page number，starting at 1")
    @Min(1)
    @Max(100)
    private Integer pageNum;
    /**
     * How many pieces of data are in each page
     */
    @Schema(description = "page size")
    @Min(1)
    @Max(100)
    private Integer pageSize;
}

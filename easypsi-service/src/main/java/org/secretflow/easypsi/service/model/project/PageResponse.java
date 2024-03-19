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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Paging query response
 *
 * @author yansi
 * @date 2023/5/25
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
public class PageResponse<T> {
    /**
     * The total count of page
     */
    @Schema(description = "the total count of page")
    private Integer pageTotal;
    /**
     * How many pieces of data are in each page
     */
    @Schema(description = "page size")
    private Integer pageSize;

    /**
     * Page data list
     */
    @Schema(description = "page data list")
    private List<T> data;

    public static <T> PageResponse<T> of(Integer pageTotal, Integer pageSize, List<T> data) {
        return new PageResponse<>(pageTotal, pageSize, data);
    }
}

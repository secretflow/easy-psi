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
import lombok.*;

import java.util.Date;

/**
 * @author liujunhao
 * @date 2024/01/17
 */
@Builder
@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ProjectJobResultVO {
    /**
     * hash
     */
    @Schema(description = "hash")
    private String hash;

    /**
     * path
     */
    @Schema(description = "path")
    private String path;

    /**
     * dir
     */
    @Schema(description = "dir")
    private String dir;

    /**
     * relativeUri
     */
    @Schema(description = "relativeUri")
    private String relativeUri;

    /**
     * expirationTime
     */
    @Schema(description = "expirationTime")
    private Date expirationTime;
}

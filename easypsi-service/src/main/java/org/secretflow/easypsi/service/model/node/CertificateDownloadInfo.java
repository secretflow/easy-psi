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

package org.secretflow.easypsi.service.model.node;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Download information
 *
 * @author guyu
 * @date 2023/10/17
 */
@Getter
@Setter
@Builder
public class CertificateDownloadInfo {

    /**
     * File name
     */
    @Schema(description = "file name")
    private String fileName;

    /**
     * File path
     */
    @Schema(description = "file path")
    private String filePath;

    /**
     * Cert text
     */
    @Schema(description = "cert text")
    private String certText;

}
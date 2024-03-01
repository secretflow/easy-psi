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

import org.secretflow.secretpad.common.constant.JobConstants;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

/**
 * Delete project job task request
 *
 * @author guyu
 * @date 2023/10/27
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class CreateProjectJobTaskRequest {

    @Schema(description = "job id")
    private String jobId;
    /**
     * name
     */
    @Schema(description = "job name")
    @NotBlank
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
    private PsiConfig initiatorConfig;

    /**
     * partnerConfig
     */
    @Schema(description = "partnerConfig")
    private PsiConfig partnerConfig;

    @Getter
    @Setter
    @ToString
    public static class PsiConfig {
        @Schema(description = "nodeId")
        private String nodeId;

        @Schema(description = "protocolConfig")
        private ProtocolConfig protocolConfig;

        @Schema(description = "inputConfig")
        private InputConfig inputConfig;

        @Schema(description = "outputConfig")
        private OutputConfig outputConfig;

        @Schema(description = "linkConfig")
        private ContextDescProto linkConfig;

        @Schema(description = "keys")
        private List<String> keys;

        @Schema(description = "skipDuplicatesCheck")
        private Boolean skipDuplicatesCheck;

        @Schema(description = "disableAlignment")
        private Boolean disableAlignment;

        @Schema(description = "recoveryConfig")
        private RecoveryConfig recoveryConfig;

        @Schema(description = "advancedJoinType")
        private JobConstants.AdvancedJoinTypeEnum advancedJoinType;

        @Schema(description = "outputDifference")
        private Boolean outputDifference;

        @Schema(description = "datatableCount")
        private String datatableCount;

        @Getter
        @Setter
        @ToString
        public static class ContextDescProto {
            private String recvTimeoutMs = JobConstants.RECV_TIMEOUT_MS;

            private String httpTimeoutMs = JobConstants.HTTP_TIMEOUT_MS;
        }

        @Getter
        @Setter
        @ToString
        public static class RecoveryConfig {
            @Schema(description = "enabled")
            private Boolean enabled;

            @Schema(description = "folder")
            private String folder;
        }

        @Getter
        @Setter
        @ToString
        public static class OutputConfig {
            @Schema(description = "type")
            private String type = JobConstants.DatatableTypeEnum.IO_TYPE_FILE_CSV.name();

            @Schema(description = "path")
            private String path;
        }

        @Getter
        @Setter
        @ToString
        public static class InputConfig {
            @Schema(description = "type")
            private String type = JobConstants.DatatableTypeEnum.IO_TYPE_FILE_CSV.name();

            @Schema(description = "path")
            private String path;
        }

        @Getter
        @Setter
        @ToString
        public static class ProtocolConfig {
            @Schema(description = "protocol")
            private JobConstants.ProtocolEnum protocol;

            @Schema(description = "role")
            private JobConstants.RoleEnum role;

            @Schema(description = "broadcastResult")
            private Boolean broadcastResult;

            @Schema(description = "ecdhConfig")
            private EcdhConfig ecdhConfig;

            @Schema(description = "kkrtConfig")
            private KkrtConfig kkrtConfig;

            @Schema(description = "rr22Config")
            private Rr22Config rr22Config;

            @Getter
            @Setter
            public static class EcdhConfig {
                @Schema(description = "curve")
                private String curve = JobConstants.CurveType.CURVE_FOURQ.name();
            }

            @Getter
            @Setter
            public static class KkrtConfig {
                @Schema(description = "bucketSize")
                private String bucketSize = JobConstants.BUCKET_SIZE;
            }

            @Getter
            @Setter
            public static class Rr22Config {
                @Schema(description = "bucketSize")
                private String bucketSize = JobConstants.BUCKET_SIZE;

                @Schema(description = "lowCommMode")
                private Boolean lowCommMode = false;
            }
        }
    }
}
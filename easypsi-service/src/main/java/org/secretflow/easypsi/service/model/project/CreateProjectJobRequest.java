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

import org.secretflow.easypsi.common.constant.JobConstants;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

/**
 * Create project job task request
 *
 * @author guyu
 * @date 2023/11/03
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class CreateProjectJobRequest {
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

    /**
     * outputConfig
     */
    @Schema(description = "outputConfig")
    private PsiConfig outputConfig;

    /**
     * advancedConfig
     */
    @Schema(description = "advancedConfig")
    private AdvancedConfig advancedConfig;

    @Getter
    @Setter
    @ToString
    public static class PsiConfig {
        @Schema(description = "nodeId")
        private String nodeId;

        @Schema(description = "path")
        private String path;

        @Schema(description = "keys")
        private List<String> keys;

        @Schema(description = "broadcastResult")
        private List<String> broadcastResult;
    }

    @Getter
    @Setter
    @ToString
    public static class AdvancedConfig {
        @Schema(description = "protocolConfig")
        private ProtocolConfig protocolConfig;

        @Schema(description = "linkConfig")
        private String linkConfig = JobConstants.RECV_TIMEOUT_MS;

        @Schema(description = "skipDuplicatesCheck")
        private Boolean skipDuplicatesCheck;

        @Schema(description = "disableAlignment")
        private Boolean disableAlignment;

        @Schema(description = "recoveryEnabled")
        private Boolean recoveryEnabled;

        @Schema(description = "advancedJoinType")
        private JobConstants.AdvancedJoinTypeEnum advancedJoinType;

        @Schema(description = "leftSide")
        private String leftSide;

        @Schema(description = "dataTableConfirmation")
        private Boolean dataTableConfirmation = false;

        @Schema(description = "dataTableCount")
        private String dataTableCount = "L0";

        @Setter
        @Getter
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
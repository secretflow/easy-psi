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
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.secretflow.easypsi.common.constant.JobConstants;
import org.secretflow.easypsi.manager.integration.job.JobManager;
import org.secretflow.easypsi.service.impl.DataServiceImpl;

import java.io.File;
import java.util.List;

/**
 * Delete project job task request
 *
 * @author guyu
 * @date 2023/10/27
 */
@Builder
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
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
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

        @Schema(description = "leftSide")
        private String leftSide;

        @Schema(description = "dataTableConfirmation")
        private Boolean dataTableConfirmation = false;

        @Schema(description = "dataTableCount")
        private String dataTableCount = "L0";

        @Getter
        @Setter
        @ToString
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        public static class ContextDescProto {
            private String recvTimeoutMs = JobConstants.RECV_TIMEOUT_MS;

            private String httpTimeoutMs = JobConstants.HTTP_TIMEOUT_MS;

            public static ContextDescProto from(CreateProjectJobRequest.AdvancedConfig advancedConfig) {
                return ContextDescProto.builder()
                        .recvTimeoutMs(advancedConfig.getLinkConfig())
                        .httpTimeoutMs(advancedConfig.getLinkConfig())
                        .build();
            }
        }

        @Getter
        @Setter
        @ToString
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        public static class RecoveryConfig {
            @Schema(description = "enabled")
            private Boolean enabled;

            @Schema(description = "folder")
            private String folder;
        }

        @Getter
        @Setter
        @ToString
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        public static class OutputConfig {
            @Schema(description = "type")
            private String type = JobConstants.DatatableTypeEnum.IO_TYPE_FILE_CSV.name();

            @Schema(description = "path")
            private String path;

            public static OutputConfig from(CreateProjectJobRequest.PsiConfig outputConfig, String nodeId) {
                return OutputConfig.builder()
                        .path(outputConfig.getBroadcastResult().contains(nodeId) ? outputConfig.getPath() : null)
                        .type(JobConstants.DatatableTypeEnum.IO_TYPE_FILE_CSV.name())
                        .build();
            }
        }

        @Getter
        @Setter
        @ToString
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        public static class InputConfig {
            @Schema(description = "type")
            private String type = JobConstants.DatatableTypeEnum.IO_TYPE_FILE_CSV.name();

            @Schema(description = "path")
            private String path;

            public static InputConfig from(CreateProjectJobRequest.PsiConfig inputConfig) {
                return InputConfig.builder()
                        .path(inputConfig.getPath())
                        .type(JobConstants.DatatableTypeEnum.IO_TYPE_FILE_CSV.name())
                        .build();
            }
        }

        @Getter
        @Setter
        @ToString
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
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
            @Builder
            @AllArgsConstructor
            @NoArgsConstructor
            public static class EcdhConfig {
                @Schema(description = "curve")
                private String curve = JobConstants.CurveType.CURVE_FOURQ.name();

                public static EcdhConfig from(CreateProjectJobRequest.AdvancedConfig.ProtocolConfig protocol) {
                    if (JobConstants.ProtocolEnum.PROTOCOL_ECDH.equals(protocol.getProtocol())) {
                        return EcdhConfig.builder()
                                .curve(protocol.getEcdhConfig().getCurve())
                                .build();
                    }
                    return null;
                }
            }

            @Getter
            @Setter
            @Builder
            @AllArgsConstructor
            @NoArgsConstructor
            public static class KkrtConfig {
                @Schema(description = "bucketSize")
                private String bucketSize = JobConstants.BUCKET_SIZE;

                public static KkrtConfig from(CreateProjectJobRequest.AdvancedConfig.ProtocolConfig protocol) {
                    if (JobConstants.ProtocolEnum.PROTOCOL_KKRT.equals(protocol.getProtocol())) {
                        return KkrtConfig.builder()
                                .bucketSize(protocol.getKkrtConfig().getBucketSize())
                                .build();
                    }
                    return null;
                }
            }

            @Getter
            @Setter
            @Builder
            @AllArgsConstructor
            @NoArgsConstructor
            public static class Rr22Config {
                @Schema(description = "bucketSize")
                private String bucketSize = JobConstants.BUCKET_SIZE;

                @Schema(description = "lowCommMode")
                private Boolean lowCommMode = false;

                public static Rr22Config from(CreateProjectJobRequest.AdvancedConfig.ProtocolConfig protocol) {
                    if (JobConstants.ProtocolEnum.PROTOCOL_RR22.equals(protocol.getProtocol())) {
                        return Rr22Config.builder()
                                .bucketSize(protocol.getRr22Config().getBucketSize())
                                .lowCommMode(protocol.getRr22Config().getLowCommMode())
                                .build();
                    }
                    return null;
                }
            }

            public static ProtocolConfig from(CreateProjectJobRequest request, CreateProjectJobRequest.PsiConfig reauestPsiConfig) {
                CreateProjectJobRequest.AdvancedConfig.ProtocolConfig requestProtocolConfig = request.getAdvancedConfig().getProtocolConfig();
                List<String> broadcast = request.getOutputConfig().getBroadcastResult();
                JobConstants.RoleEnum role;

                if (broadcast.contains(reauestPsiConfig.getNodeId())) {
                    role = JobConstants.RoleEnum.ROLE_RECEIVER;
                } else {
                    role = JobConstants.RoleEnum.ROLE_SENDER;
                }

                return ProtocolConfig.builder()
                        .protocol(requestProtocolConfig.getProtocol())
                        .role(role)
                        .broadcastResult(broadcast.size() == 2)
                        .ecdhConfig(EcdhConfig.from(requestProtocolConfig))
                        .kkrtConfig(KkrtConfig.from(requestProtocolConfig))
                        .rr22Config(Rr22Config.from(requestProtocolConfig))
                        .build();
            }


        }

        public static PsiConfig from(CreateProjectJobRequest request, CreateProjectJobRequest.PsiConfig reauestPsiConfig, String jobId) {
            CreateProjectJobRequest.AdvancedConfig requestAdvancedConfig = request.getAdvancedConfig();
            return PsiConfig.builder()
                    .nodeId(reauestPsiConfig.getNodeId())
                    .protocolConfig(ProtocolConfig.from(request, reauestPsiConfig))
                    .inputConfig(InputConfig.from(reauestPsiConfig))
                    .outputConfig(OutputConfig.from(request.getOutputConfig(), reauestPsiConfig.getNodeId()))
                    .linkConfig(ContextDescProto.from(requestAdvancedConfig))
                    .keys(reauestPsiConfig.getKeys())
                    .skipDuplicatesCheck(requestAdvancedConfig.getSkipDuplicatesCheck())
                    .disableAlignment(requestAdvancedConfig.getDisableAlignment())
                    .recoveryConfig(RecoveryConfig.builder()
                            .folder(JobManager.KUSCIA_DATA_PATH + JobManager.PROJECT_JOB_TASK_TMP + jobId + File.separator)
                            .enabled(requestAdvancedConfig.getRecoveryEnabled())
                            .build())
                    .advancedJoinType(requestAdvancedConfig.getAdvancedJoinType())
                    .leftSide(requestAdvancedConfig.getLeftSide())
                    .dataTableConfirmation(requestAdvancedConfig.getDataTableConfirmation())
                    .dataTableCount(requestAdvancedConfig.getDataTableConfirmation() ?
                            DataServiceImpl.dataTableCountCache.get(DataServiceImpl.spliceNodeTable(reauestPsiConfig.getNodeId(), reauestPsiConfig.getPath())) : null)
                    .build();
        }
    }

    public static CreateProjectJobTaskRequest fromJobRequest(CreateProjectJobRequest request, String jobId) {
        PsiConfig partnerConfig = PsiConfig.from(request, request.getPartnerConfig(), jobId);
        if (partnerConfig.getProtocolConfig().getBroadcastResult())
            partnerConfig.getProtocolConfig().setRole(JobConstants.RoleEnum.ROLE_SENDER);
        return CreateProjectJobTaskRequest.builder()
                .name(request.getName())
                .description(request.getDescription())
                .jobId(jobId)
                .initiatorConfig(PsiConfig.from(request, request.getInitiatorConfig(), jobId))
                .partnerConfig(partnerConfig)
                .build();

    }

}
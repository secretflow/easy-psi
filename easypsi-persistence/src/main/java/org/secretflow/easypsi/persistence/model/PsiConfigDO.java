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

package org.secretflow.easypsi.persistence.model;

import org.secretflow.easypsi.common.constant.JobConstants;

import lombok.*;

import java.util.List;

/**
 * Graph edge data object
 *
 * @author guyu
 * @date 2023/10/30
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PsiConfigDO {
    private String nodeId;

    private ProtocolConfig protocolConfig;

    private InputConfig inputConfig;

    private OutputConfig outputConfig;

    private ContextDescProto linkConfig;

    private List<String> keys;

    private Boolean skipDuplicatesCheck;

    private Boolean disableAlignment;

    private RecoveryConfig recoveryConfig;

    private JobConstants.AdvancedJoinTypeEnum advancedJoinType;

    private String leftSide;

    private Boolean dataTableConfirmation;

    private String dataTableCount;


    @Getter
    @Setter
    public static class RecoveryConfig {
        private Boolean enabled;

        private String folder;
    }

    @Getter
    @Setter
    public static class OutputConfig {
        private String type = JobConstants.DatatableTypeEnum.IO_TYPE_FILE_CSV.name();

        private String path;
    }

    @Getter
    @Setter
    public static class InputConfig {
        private String type = JobConstants.DatatableTypeEnum.IO_TYPE_FILE_CSV.name();

        private String path;
    }

    @Getter
    @Setter
    public static class ContextDescProto{
        private String recvTimeoutMs = JobConstants.RECV_TIMEOUT_MS;

        private String httpTimeoutMs = JobConstants.HTTP_TIMEOUT_MS;
    }

    @Getter
    @Setter
    public static class ProtocolConfig {
        private JobConstants.ProtocolEnum protocol;

        private JobConstants.RoleEnum role;

        private Boolean broadcastResult;

        private EcdhConfig ecdhConfig;

        private KkrtConfig kkrtConfig;

        private Rr22Config rr22Config;

        @Getter
        @Setter
        public static class EcdhConfig {
            private String curve = JobConstants.CurveType.CURVE_FOURQ.name();
        }
        @Getter
        @Setter
        public static class KkrtConfig {
            private String bucketSize = JobConstants.BUCKET_SIZE;
        }

        @Getter
        @Setter
        public static class Rr22Config {
            private String bucketSize = JobConstants.BUCKET_SIZE;

            private Boolean lowCommMode = false;
        }
    }
}

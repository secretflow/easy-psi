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

package org.secretflow.secretpad.common.constant;

/**
 * @author guyu
 * @date 2023/10/27
 */
public class JobConstants {

    public enum DatatableTypeEnum {
        IO_TYPE_FILE_CSV
    }

    public enum RoleEnum {
        ROLE_RECEIVER,
        ROLE_SENDER
    }

    public enum AdvancedJoinTypeEnum {
        ADVANCED_JOIN_TYPE_UNSPECIFIED,
        ADVANCED_JOIN_TYPE_INNER_JOIN,
        ADVANCED_JOIN_TYPE_DIFFERENCE
    }

    public enum ProtocolEnum {
        PROTOCOL_ECDH,
        PROTOCOL_KKRT,
        PROTOCOL_RR22
    }

    public enum CurveType {
        CURVE_25519,
        CURVE_FOURQ,
        CURVE_SM2,
        CURVE_SECP256K1,
        CURVE_25519_ELLIGATOR2
    }

    public static final String BUCKET_SIZE = "1048576";
    public static final String RECV_TIMEOUT_MS = "30";
    public static final String HTTP_TIMEOUT_MS = "30";

    public static final String LEFT_SIDE = "ROLE_RECEIVER";
}
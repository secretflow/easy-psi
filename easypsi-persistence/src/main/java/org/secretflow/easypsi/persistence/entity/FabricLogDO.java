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

package org.secretflow.easypsi.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

/**
 * Fabric log data object
 *
 * @author lihaixin
 * @date 2024/01/15
 */
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "fabric_log")
@ToString
@Getter
@Setter
@SQLDelete(sql = "update fabric_log set is_deleted = 1 where inst_id = ?")
@Where(clause = "is_deleted = 0")
public class FabricLogDO extends BaseAggregationRoot<FabricLogDO> {

    /**
     * Log path
     */
    @Column(name = "log_path", nullable = false)
    private String logPath;

    /**
     * Log hash
     */
    @Column(name = "log_hash", nullable = false)
    @Id
    private String logHash;

    /**
     * Channel name
     */
    @Column(name = "channel_name", nullable = false)
    private String channelName;

    /**
     * Chain code name
     */
    @Column(name = "chain_code_name", nullable = false)
    private String chainCodeName;

    /**
     * MspId
     */
    @Column(name = "msp_id", nullable = false)
    private String mspId;

    /**
     * Override auth
     */
    @Column(name = "override_auth", nullable = false)
    private String overrideAuth;

    /**
     * Owner
     */
    @Column(name = "owner", nullable = false)
    private String owner;

    /**
     * Owner
     */
    @Column(name = "result", nullable = false)
    private Integer result;

    /**
     * Message
     */
    @Column(name = "message", nullable = false)
    private String message;
}

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

import org.secretflow.easypsi.common.constant.UserOwnerType;
import org.secretflow.easypsi.persistence.converter.Boolean2IntConverter;
import org.secretflow.easypsi.persistence.converter.SqliteLocalDateTimeConverter;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

/**
 * User account data object
 *
 * @author : xiaonan.fhn
 * @date 2023/05/25
 */
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_accounts")
@ToString
@Getter
@Setter
@SQLDelete(sql = "update user_accounts set is_deleted = 1 where inst_id = ?")
@Where(clause = "is_deleted = 0")
public class AccountsDO extends BaseAggregationRoot<AccountsDO> {
    /**
     * User name
     */
    @Id
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    /**
     * User password
     */
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    /**
     * CENTER or EDGE
     */
    @Column(name = "owner_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private UserOwnerType ownerType;

    /**
     * nodeId or 'kuscia-system'
     */
    @Column(name = "owner_id", nullable = false)
    private String ownerId;

    /**
     * login failed attempts
     */
    @Column(name = "failed_attempts")
    private Integer failedAttempts;

    /**
     * locked invalid time
     */
    @Column(name = "locked_invalid_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Convert(converter = SqliteLocalDateTimeConverter.class)
    private LocalDateTime lockedInvalidTime;

    /**
     * reset password failed attempts
     */
    @Column(name = "passwd_reset_failed_attempts")
    private Integer passwdResetFailedAttempts;

    /**
     * reset password lock release time
     */
    @Column(name = "gmt_passwd_reset_release")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Convert(converter = SqliteLocalDateTimeConverter.class)
    private LocalDateTime gmtPasswdResetRelease;

    /**
     * Node remark
     */
    @Column(name = "initial", nullable = false, length = 1)
    @Convert(converter = Boolean2IntConverter.class)
    private Boolean initial;
}

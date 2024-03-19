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

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.secretflow.easypsi.persistence.converter.SqliteLocalDateTimeConverter;

import java.time.LocalDateTime;

/**
 * Rsa encryption key object
 *
 * @author lihaixin
 * @date 2024/02/04
 */
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "rsa_encryption_key")
@ToString
@Getter
@Setter
@SQLDelete(sql = "update rsa_encryption_key set is_deleted = 1 where inst_id = ?")
@Where(clause = "is_deleted = 0")
public class RsaEncryptionKeyDO extends BaseAggregationRoot<RsaEncryptionKeyDO> {

    /**
     * Public key
     */
    @Column(name = "public_key", nullable = false)
    private String publicKey;

    /**
     * Private key
     */
    @Column(name = "private_key", nullable = false)
    @Id
    private String privateKey;


    /**
     * key invalid time
     */
    @Column(name = "key_invalid_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Convert(converter = SqliteLocalDateTimeConverter.class)
    private LocalDateTime keyInvalidTime;

}

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

package org.secretflow.easypsi.persistence.repository;

import org.secretflow.easypsi.persistence.entity.FabricLogDO;
import org.secretflow.easypsi.persistence.entity.RsaEncryptionKeyDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * Rsa encryption key repository
 *
 * @author lihaixin
 * @date 2024/02/04
 */
public interface RsaEncryptionKeyRepository extends JpaRepository<RsaEncryptionKeyDO, String> {

    /**
     * Find by public key
     *
     * @param publicKey
     * @return {@link FabricLogDO }
     */

    @Query("from RsaEncryptionKeyDO where publicKey=:publicKey")
    RsaEncryptionKeyDO findByPublicKey(String publicKey);


    /**
     * Find last key
     *
     * @return {@link RsaEncryptionKeyDO }
     */

    @Query("from RsaEncryptionKeyDO order by keyInvalidTime desc limit 1")
    RsaEncryptionKeyDO findLastKey();

}

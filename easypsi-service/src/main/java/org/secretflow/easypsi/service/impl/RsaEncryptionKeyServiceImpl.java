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

package org.secretflow.easypsi.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.secretflow.easypsi.common.util.RsaUtils;
import org.secretflow.easypsi.persistence.entity.RsaEncryptionKeyDO;
import org.secretflow.easypsi.persistence.repository.RsaEncryptionKeyRepository;
import org.secretflow.easypsi.service.RsaEncryptionKeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * Rsa encryption key service implementation class
 *
 * @author lihaixin
 * @date 2024/02/04
 */
@Service
public class RsaEncryptionKeyServiceImpl implements RsaEncryptionKeyService {


    @Autowired
    private RsaEncryptionKeyRepository rsaEncryptionKeyRepository;


    @Value("${secretpad.encryption-key-time-minutes:15}")
    private Integer encryptionKeyTimeMinutes;

    @Override
    public RsaEncryptionKeyDO findByPublicKey(String publicKey) {
        if (StringUtils.isNotBlank(publicKey)) {
            return rsaEncryptionKeyRepository.findByPublicKey(publicKey);
        }

        LocalDateTime currentTime = LocalDateTime.now();
        RsaEncryptionKeyDO rsaEncryptionKeyDO = rsaEncryptionKeyRepository.findLastKey();
        if (Objects.nonNull(rsaEncryptionKeyDO)) {
            Duration duration = Duration.between(currentTime, rsaEncryptionKeyDO.getKeyInvalidTime());
            Long minutes = duration.toMinutes();
            if (minutes > 0) {
                return rsaEncryptionKeyDO;
            }
        }
        Map<String, String> keyMap = RsaUtils.generateRSAKeys();
        rsaEncryptionKeyDO = new RsaEncryptionKeyDO();
        rsaEncryptionKeyDO.setPublicKey(keyMap.get(RsaUtils.PUBLIC_KEY_NAME));
        rsaEncryptionKeyDO.setPrivateKey(keyMap.get(RsaUtils.PRIVATE_KEY_NAME));
        rsaEncryptionKeyDO.setKeyInvalidTime(currentTime.plusMinutes(encryptionKeyTimeMinutes));
        rsaEncryptionKeyRepository.save(rsaEncryptionKeyDO);
        return rsaEncryptionKeyDO;
    }
}
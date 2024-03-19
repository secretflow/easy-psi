/*
 * Copyright 2024 Ant Group Co., Ltd.
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

package org.secretflow.easypsi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.secretflow.easypsi.common.util.RsaUtils;
import org.secretflow.easypsi.persistence.entity.RsaEncryptionKeyDO;
import org.secretflow.easypsi.persistence.repository.RsaEncryptionKeyRepository;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.util.Map;

import static org.mockito.Mockito.when;

/**
 * Rsa Encryption Key Service Test
 *
 * @author lihaixin
 * @date 2024/03/12
 */
@SpringBootTest(classes = {RsaEncryptionKeyService.class, RsaEncryptionKeyRepository.class})
public class RsaEncryptionKeyServiceTest {

    @MockBean
    private RsaEncryptionKeyService rsaEncryptionKeyService;

    @MockBean
    private RsaEncryptionKeyRepository rsaEncryptionKeyRepository;

    public static String rsaPublicKey;
    public static String rsaPrivateKey;

    @BeforeEach
    public void setUp() {
        Map<String, String> keyMap = RsaUtils.generateRSAKeys();
        rsaPublicKey = keyMap.get(RsaUtils.PUBLIC_KEY_NAME);
        rsaPrivateKey = keyMap.get(RsaUtils.PRIVATE_KEY_NAME);
    }

    @Test
    public void testByPublicKeyExistingKey() {
        RsaEncryptionKeyDO expectedRsaKey = buildRsaEncryptionKeyDO();
        when(rsaEncryptionKeyRepository.findByPublicKey(rsaPublicKey)).thenReturn(expectedRsaKey);
        rsaEncryptionKeyService.findByPublicKey(rsaPublicKey);
    }

    @Test
    public void testLasKey() {
        LocalDateTime now = LocalDateTime.now();
        RsaEncryptionKeyDO latestKey = buildRsaEncryptionKeyDO();
        latestKey.setKeyInvalidTime(now.plusMinutes(60));
        when(rsaEncryptionKeyRepository.findLastKey()).thenReturn(latestKey);
        rsaEncryptionKeyService.findByPublicKey(null);
    }

    @Test
    public void testFindByPublicKeyNoValidKeys() {
        LocalDateTime now = LocalDateTime.now();
        RsaEncryptionKeyDO expiredKey = buildRsaEncryptionKeyDO();
        expiredKey.setKeyInvalidTime(now.minusMinutes(1));
        when(rsaEncryptionKeyRepository.findLastKey()).thenReturn(expiredKey);
        rsaEncryptionKeyService.findByPublicKey(null);
    }

    private RsaEncryptionKeyDO buildRsaEncryptionKeyDO() {
        RsaEncryptionKeyDO rsaEncryptionKeyDO = new RsaEncryptionKeyDO();
        rsaEncryptionKeyDO.setPublicKey(rsaPublicKey);
        rsaEncryptionKeyDO.setPrivateKey(rsaPrivateKey);
        return rsaEncryptionKeyDO;
    }

}
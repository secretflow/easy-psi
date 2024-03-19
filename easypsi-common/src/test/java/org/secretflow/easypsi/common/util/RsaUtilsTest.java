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

package org.secretflow.easypsi.common.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

class RsaUtilsTest {

    @Test
    public void testGenerateRSAKeys() {
        Map<String, String> keys = Assertions.assertDoesNotThrow(() -> RsaUtils.generateRSAKeys());
        Assertions.assertNotNull(keys.get(RsaUtils.PUBLIC_KEY_NAME));
        Assertions.assertNotNull(keys.get(RsaUtils.PRIVATE_KEY_NAME));

    }

    @Test
    public void testEncryptAndDecrypt() throws Exception {
        Map<String, String> keys = RsaUtils.generateRSAKeys();
        String publicKey = keys.get(RsaUtils.PUBLIC_KEY_NAME);
        String privateKey = keys.get(RsaUtils.PRIVATE_KEY_NAME);
        String sourceData = "This is a test message";
        String encryptedData = RsaUtils.encrypt(sourceData, publicKey);
        String decryptedData = RsaUtils.decrypt(encryptedData, privateKey);
        Assertions.assertEquals(sourceData, decryptedData);
        Assertions.assertThrows(RuntimeException.class, () -> RsaUtils.encrypt(sourceData, "invalid public key"), "Failed to RSA decrypt ");
        Assertions.assertThrows(RuntimeException.class, () -> RsaUtils.decrypt("invalid enc data", privateKey), "Failed to RSA decrypt ");
    }

    @Test
    public void testDecryptWithInvalidPrivateKey() {
        String invalidPrivateKey = "invalid_private_key";
        String encryptedData = "some_encrypted_data";
        Throwable thrown = Assertions.assertThrows(RuntimeException.class, () -> RsaUtils.decrypt(encryptedData, invalidPrivateKey));
        String expectedMessage = "Failed to RSA decrypt";
        Assertions.assertTrue(thrown.getMessage().contains(expectedMessage));
    }
}
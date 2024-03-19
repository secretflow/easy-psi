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

package org.secretflow.easypsi.common.util;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;

/**
 * Rsa utils
 *
 * @author lihaixin
 * @date 2024/02/04
 */
public class RsaUtils {

    private final static String ALGORITHM = "RSA";

    private final static Integer KET_SIZE = 2048;


    public final static String PUBLIC_KEY_NAME = "publicKey";

    public final static String PRIVATE_KEY_NAME = "privateKey";

    /**
     * Generates a new RSA key pair.
     *
     * @return {@link Map }<{@link String }, {@link Object }>
     */
    public static Map<String, String> generateRSAKeys() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM);
            keyPairGenerator.initialize(KET_SIZE);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
            return Map.of(PUBLIC_KEY_NAME, Base64.getEncoder().encodeToString(publicKey.getEncoded()), PRIVATE_KEY_NAME, Base64.getEncoder().encodeToString(privateKey.getEncoded()));
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate RSA key pair", e);
        }
    }


    /**
     * RSA decrypt data
     *
     * @param encryptData
     * @param privateKey
     * @return {@link String }
     */

    public static String decrypt(String encryptData, String privateKey) {
        try {
            byte[] inputByte = Base64.getDecoder().decode(encryptData.getBytes(StandardCharsets.UTF_8));
            byte[] decoded = Base64.getDecoder().decode(privateKey);
            RSAPrivateKey priKey = (RSAPrivateKey) KeyFactory.getInstance(ALGORITHM).generatePrivate(new PKCS8EncodedKeySpec(decoded));
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, priKey);
            return new String(cipher.doFinal(inputByte));
        } catch (Exception e) {
            throw new RuntimeException("Failed to RSA decrypt", e);
        }
    }

    /**
     * RSA encrypt data
     *
     * @param sourceData
     * @param publicKey
     * @return {@link String }
     */

    public static String encrypt(String sourceData, String publicKey) {
        try {
            byte[] decoded = Base64.getDecoder().decode(publicKey);
            RSAPublicKey pubKey = (RSAPublicKey) KeyFactory.getInstance(ALGORITHM).generatePublic(new X509EncodedKeySpec(decoded));
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, pubKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(sourceData.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException("Failed to RSA encrypt", e);
        }
    }
}
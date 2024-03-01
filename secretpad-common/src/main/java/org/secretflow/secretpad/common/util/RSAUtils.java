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

package org.secretflow.secretpad.common.util;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

/**
 * @author yutu
 * @date 2023/09/11
 */
public class RSAUtils {

    public static String signRs256(String input) throws Exception {
        String strPk = FileUtils.readFile2String("./config/certs/client.pem");
        return signSHA256RSA(input, strPk);
    }

    public static String signSHA256RSAByPkFilePath(String input, String strPkFilePath) throws Exception {
        String strPk = FileUtils.readFile2String(strPkFilePath);
        return signSHA256RSA(input, strPk);
    }

    /**
     * Create base64 encoded signature using SHA256/RSA.
     *
     * @param input str
     * @param strPk pk
     */
    public static String signSHA256RSA(String input, String strPk) throws Exception {
        // Remove markers and new line characters in private key
        String realPK = strPk.replaceAll("-----END PRIVATE KEY-----", "")
                .replaceAll("-----BEGIN PRIVATE KEY-----", "")
                .replaceAll("\n", "");
        byte[] b1 = Base64.getDecoder().decode(realPK);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(b1);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        Signature privateSignature = Signature.getInstance("SHA256withRSA");
        privateSignature.initSign(kf.generatePrivate(spec));
        byte[] encode = input.getBytes();
        privateSignature.update(encode);
        byte[] s = privateSignature.sign();
        return Base64.getEncoder().encodeToString(s);
    }

    public static String getClientCert() throws IOException {
        String clientCertPath = "./config/certs/client.crt";
        return FileUtils.readFile2String(clientCertPath);
    }

    public static String getCaCert() throws IOException {
        String caCertPath = "./config/certs/ca.crt";
        return FileUtils.readFile2String(caCertPath);
    }

    public static String getTaskCertChain() throws IOException {
        String clientCertPath = "./config/certs/client.crt";
        String caCertPath = "./config/certs/ca.crt";
        String ca = FileUtils.readFile2String(caCertPath);
        String caStr = ca.replaceAll("-----END CERTIFICATE-----", "")
                .replaceAll("-----BEGIN CERTIFICATE-----", "")
                .replaceAll("\n", "");
        String client = FileUtils.readFile2String(clientCertPath);
        String clientStr = client.replaceAll("-----END CERTIFICATE-----", "")
                .replaceAll("-----BEGIN CERTIFICATE-----", "")
                .replaceAll("\n", "");
        return String.format("%s.%s", clientStr, caStr);
    }
}
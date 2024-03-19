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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

/**
 * Certificate utils
 *
 * @author yansi
 * @date 2023/5/8
 */
public class CertUtils {
    /**
     * Loads an X.509 certificate from the classpath resources or filesystem
     *
     * @param filepath path of a cert file
     *                 Example:
     *                 1. classpath:./certs/ca.crt
     *                 2. file:./config/certs/ca.crt
     *                 3. ./config/certs/ca.crt
     */
    public static X509Certificate loadX509Cert(String filepath) throws CertificateException, IOException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        File file = FileUtils.readFile(filepath);
        try (InputStream in = new FileInputStream(file)) {
            return (X509Certificate) cf.generateCertificate(in);
        }
    }
}

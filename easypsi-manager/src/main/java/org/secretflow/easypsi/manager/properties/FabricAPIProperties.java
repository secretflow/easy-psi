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

package org.secretflow.easypsi.manager.properties;

import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Fabric api properties
 *
 * @author lihaixin
 * @date 2024/01/13
 */
@Data
@ConfigurationProperties(prefix = "fabric", ignoreInvalidFields = true)
@ConditionalOnProperty(name = "fabric.isOpen", havingValue = "true")
public class FabricAPIProperties {

    /**
     * Is open
     */
    private Boolean isOpen;


    /**
     * Msp id example:Org1MSP
     */
    private String mspId;

    /**
     * Channel name example:mychannel
     */
    private String channelName;

    /**
     * Chain code name  example:basic
     */
    private String chainCodeName;

    /**
     * Sign cert path
     */
    private String signCertPath;

    /**
     * Keystore path
     */
    private String keystorePath;

    /**
     * Tls cert path
     */
    private String tlsCertPath;


    /**
     * fabric address
     */
    private String address;


    /**
     * Override auth
     */
    private String overrideAuth;

    /**
     * owner
     */
    private String owner;
}

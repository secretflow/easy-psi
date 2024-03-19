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

package org.secretflow.easypsi.web.controller;

import org.secretflow.easypsi.service.RsaEncryptionKeyService;
import org.secretflow.easypsi.service.model.common.EasyPsiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * Encryption controller
 *
 * @author lihaixin
 * @date 2024/02/04
 */
@RestController
@RequestMapping(value = "/api/encryption")
public class EncryptionController {
    @Autowired
    private RsaEncryptionKeyService rsaEncryptionKeyService;


    /**
     * Get random key string.
     *
     * @return {@link String }
     */
    @GetMapping(value = "/getRandomKey")
    public EasyPsiResponse<String> getRandomKey() {
        return EasyPsiResponse.success(rsaEncryptionKeyService.findByPublicKey(null).getPublicKey());
    }
}

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

package org.secretflow.easypsi.service;

import org.secretflow.easypsi.common.dto.UserContextDTO;
import org.secretflow.easypsi.service.model.auth.LoginRequest;

/**
 * User auth service interface
 *
 * @author : xiaonan.fhn
 * @date 2023/5/25
 */
public interface AuthService {

    /**
     * User login and save user token data
     *
     * @param request
     * @return {@link UserContextDTO }
     */

    UserContextDTO login(LoginRequest request);

    /**
     * User logout and delete user token data
     *
     * @param name  userName
     * @param token user token data
     */
    void logout(String name, String token);
}

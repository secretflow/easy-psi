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

package org.secretflow.secretpad.web.controller;

import org.secretflow.secretpad.common.annotation.resource.InterfaceResource;
import org.secretflow.secretpad.common.constant.resource.InterfaceResourceCode;
import org.secretflow.secretpad.common.dto.UserContextDTO;
import org.secretflow.secretpad.common.util.UserContext;
import org.secretflow.secretpad.service.AuthService;
import org.secretflow.secretpad.service.model.auth.LoginRequest;
import org.secretflow.secretpad.service.model.common.SecretPadResponse;
import org.secretflow.secretpad.web.util.AuthUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Authorization controller
 *
 * @author : xiaonan.fhn
 * @date 2023/05/25
 */
@RestController
@RequestMapping(value = "/api")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * User login api
     *
     * @param response http servlet response
     * @param request  login request
     * @return successful SecretPadResponse with token
     */
    @ResponseBody
    @PostMapping(value = "/login", consumes = "application/json")
    @InterfaceResource(interfaceCode = InterfaceResourceCode.AUTH_LOGIN)
    public SecretPadResponse<UserContextDTO> login(HttpServletResponse response, @Valid @RequestBody LoginRequest request) {
        UserContextDTO login = authService.login(request.getName(), request.getPasswordHash());
        return SecretPadResponse.success(login);
    }

    /**
     * User logout api
     *
     * @param request http servlet request
     * @return {@link SecretPadResponse }<{@link String }>
     */

    @ResponseBody
    @PostMapping(value = "/logout", consumes = "application/json")
    @InterfaceResource(interfaceCode = InterfaceResourceCode.AUTH_LOGOUT)
    public SecretPadResponse<String> logout(HttpServletRequest request) {
        UserContextDTO userContextDTO = UserContext.getUser();
        String token = AuthUtils.findTokenInHeader(request);
        authService.logout(userContextDTO.getName(), token);
        return SecretPadResponse.success(userContextDTO.getName());
    }

}

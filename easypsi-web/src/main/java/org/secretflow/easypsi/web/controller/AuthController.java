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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.secretflow.easypsi.common.annotation.resource.InterfaceResource;
import org.secretflow.easypsi.common.constant.resource.InterfaceResourceCode;
import org.secretflow.easypsi.common.dto.UserContextDTO;
import org.secretflow.easypsi.common.util.UserContext;
import org.secretflow.easypsi.service.AuthService;
import org.secretflow.easypsi.service.model.auth.LoginRequest;
import org.secretflow.easypsi.service.model.common.EasyPsiResponse;
import org.secretflow.easypsi.web.util.AuthUtils;
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
     * @return successful EasyPsiResponse with token
     */
    @ResponseBody
    @PostMapping(value = "/login", consumes = "application/json")
    @InterfaceResource(interfaceCode = InterfaceResourceCode.AUTH_LOGIN)
    public EasyPsiResponse<UserContextDTO> login(HttpServletResponse response, @Valid @RequestBody LoginRequest request) {
        UserContextDTO login = authService.login(request);
        return EasyPsiResponse.success(login);
    }

    /**
     * User logout api
     *
     * @param request http servlet request
     * @return {@link EasyPsiResponse }<{@link String }>
     */

    @ResponseBody
    @PostMapping(value = "/logout", consumes = "application/json")
    @InterfaceResource(interfaceCode = InterfaceResourceCode.AUTH_LOGOUT)
    public EasyPsiResponse<String> logout(HttpServletRequest request) {
        UserContextDTO userContextDTO = UserContext.getUser();
        String token = AuthUtils.findTokenInHeader(request);
        authService.logout(userContextDTO.getName(), token);
        return EasyPsiResponse.success(userContextDTO.getName());
    }

    /**
     * query user info
     *
     * @return successful EasyPsiResponse with user name
     */
    @PostMapping(value = "/get")
    @InterfaceResource(interfaceCode = InterfaceResourceCode.USER_GET)
    public EasyPsiResponse<UserContextDTO> get() {
        return EasyPsiResponse.success(UserContext.getUser());
    }

}

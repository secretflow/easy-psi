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

import jakarta.validation.Valid;
import org.secretflow.easypsi.common.annotation.resource.InterfaceResource;
import org.secretflow.easypsi.common.constant.resource.InterfaceResourceCode;
import org.secretflow.easypsi.common.dto.UserContextDTO;
import org.secretflow.easypsi.common.util.UserContext;
import org.secretflow.easypsi.service.UserService;
import org.secretflow.easypsi.service.model.auth.UserUpdatePwdRequest;
import org.secretflow.easypsi.service.model.common.EasyPsiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author beiwei
 * @date 2023/9/13
 */

@RestController
@RequestMapping(value = "/api/v1alpha1/user")
public class UserController {


    @Autowired
    private UserService userService;


    /**
     * Update user pwd by userName
     *
     * @param userUpdatePwdRequest
     * @return {@link EasyPsiResponse }<{@link Boolean }>
     */

    @PostMapping(value = "/updatePwd")
    @InterfaceResource(interfaceCode = InterfaceResourceCode.USER_UPDATE_PWD)
    public EasyPsiResponse<Boolean> updatePwd(@Valid @RequestBody UserUpdatePwdRequest userUpdatePwdRequest) {
        UserContextDTO userContextDTO = UserContext.getUser();
        userUpdatePwdRequest.setName(userContextDTO.getName());
        userService.updatePwd(userUpdatePwdRequest);
        return EasyPsiResponse.success(Boolean.TRUE);
    }
}

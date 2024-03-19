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

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.secretflow.easypsi.common.dto.UserContextDTO;
import org.secretflow.easypsi.common.errorcode.AuthErrorCode;
import org.secretflow.easypsi.common.exception.EasyPsiException;

/**
 * @author yutu
 * @date 2023/08/09
 */
@Setter
@Getter
@ToString
public final class  UserContext {
    private static final ThreadLocal<UserContextDTO> USER = new ThreadLocal<>();

    private UserContext() {
    }

    public static String getUserName() {
        return getUser().getName();
    }

    public static UserContextDTO getUser() {
        UserContextDTO userContextDTO = USER.get();
        if (userContextDTO == null) {
            throw EasyPsiException.of(AuthErrorCode.AUTH_FAILED, "auth failed");
        }
        return userContextDTO;
    }

    public static void setBaseUser(UserContextDTO userContextDTO) {
        USER.set(userContextDTO);
    }

    public static void remove() {
        USER.remove();
    }
}
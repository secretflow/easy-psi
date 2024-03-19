/*
 * Copyright 2024 Ant Group Co., Ltd.
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

package org.secretflow.easypsi.web.util;

import jakarta.servlet.http.HttpServletRequest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.secretflow.easypsi.common.exception.EasyPsiException;
import org.secretflow.easypsi.web.constant.AuthConstants;

class AuthUtilsTest {

    @Test
    void findTokenInHeader() {
        String token = "token";
        HttpServletRequest request = BDDMockito.mock(HttpServletRequest.class);
        BDDMockito.given(request.getHeader(AuthConstants.TOKEN_NAME)).willReturn("token");
        String tokenInHeader = AuthUtils.findTokenInHeader(request);
        Assertions.assertThat(tokenInHeader).isNotBlank().isEqualTo(token);
    }

    @Test
    void findTokenInHeaderWithThrow() {
        HttpServletRequest request = BDDMockito.mock(HttpServletRequest.class);
        BDDMockito.given(request.getHeader(AuthConstants.TOKEN_NAME)).willReturn("");
        org.junit.jupiter.api.Assertions.assertThrows(EasyPsiException.class,()->AuthUtils.findTokenInHeader(request));
    }
}
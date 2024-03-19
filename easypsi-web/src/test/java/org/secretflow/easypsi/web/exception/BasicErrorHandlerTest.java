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

package org.secretflow.easypsi.web.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.secretflow.easypsi.common.exception.EasyPsiException;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;


import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author chenmingliang
 * @date 2024/03/13
 */
public class BasicErrorHandlerTest {

    @Test
    public void testErrorHtml() {

        DefaultErrorAttributes attributes = BDDMockito.mock(DefaultErrorAttributes.class);
        ErrorProperties errorProperties = BDDMockito.mock(ErrorProperties.class);
        BasicErrorHandler handler = new BasicErrorHandler(attributes,errorProperties,null){
            @Override
            protected HttpStatus getStatus(HttpServletRequest request) {
                return HttpStatus.NOT_FOUND;
            }
        };
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        assertThrows(EasyPsiException.class, () -> handler.errorHtml(request, response));

        BasicErrorHandler handler4xx = new BasicErrorHandler(attributes,errorProperties,null){
            @Override
            protected HttpStatus getStatus(HttpServletRequest request) {
                return HttpStatus.FORBIDDEN;
            }
        };
        assertThrows(EasyPsiException.class, () -> handler4xx.errorHtml(request, response));

        BasicErrorHandler handler5xx = new BasicErrorHandler(attributes,errorProperties,null){
            @Override
            protected HttpStatus getStatus(HttpServletRequest request) {
                return HttpStatus.GATEWAY_TIMEOUT;
            }
        };

        assertThrows(EasyPsiException.class, () -> handler5xx.errorHtml(request, response));

    }
}

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
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;

import java.io.IOException;
import java.io.PrintWriter;

class ResponseUtilsTest {

    @Test
    void buildResponse_404() throws IOException {
        HttpServletRequest request = BDDMockito.mock(HttpServletRequest.class);
        HttpServletResponse response = BDDMockito.mock(HttpServletResponse.class);
        PrintWriter writer = BDDMockito.mock(PrintWriter.class);

        BDDMockito.given(request.getRequestURI()).willReturn("uri");
        BDDMockito.given(response.getWriter()).willReturn(writer);

        ResponseUtils.buildResponse_404(response, request);

        BDDMockito.verify(writer, BDDMockito.times(1)).write(BDDMockito.anyString());
    }
}
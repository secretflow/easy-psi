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

package org.secretflow.easypsi.web.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.codec.CharEncoding;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.util.Map;

/**
 * Response utils
 *
 * @author lihaixin
 * @date 2024/02/01
 */
public class ResponseUtils {

    /**
     * Builds  404 response.
     *
     * @param response
     * @param request
     */

    public static void buildResponse_404(HttpServletResponse response, HttpServletRequest request) throws IOException {
        Map<String, Object> errorMap = Maps.newLinkedHashMap();
        errorMap.put("timestamp", System.currentTimeMillis());
        errorMap.put("status", HttpServletResponse.SC_NOT_FOUND);
        errorMap.put("error", "Not Found");
        errorMap.put("path", request.getRequestURI());
        response.setCharacterEncoding(CharEncoding.UTF_8);
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(errorMap);
        response.getWriter().write(json);
    }
}
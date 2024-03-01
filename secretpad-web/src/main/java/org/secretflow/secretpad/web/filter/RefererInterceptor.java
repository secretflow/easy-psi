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
package org.secretflow.secretpad.web.filter;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequestInterceptor;
import org.springframework.web.servlet.handler.WebRequestHandlerInterceptorAdapter;

import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author beiwei
 * @date 2023/11/22
 */
@Component
public class RefererInterceptor extends WebRequestHandlerInterceptorAdapter {
    /**
     * 白名单
     */
    private Set<String> refererDomain = new HashSet<>();
    /**
     * 是否开启referer校验
     */
    private Boolean check = true;

    public RefererInterceptor(WebRequestInterceptor requestInterceptor) {
        super(requestInterceptor);
    }


    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse resp, Object handler) throws Exception {
        if (!check) {
            return true;
        }
        String referer = req.getHeader("referer");
        String host = req.getServerName();
        // 验证非get请求
        if (!"GET".equals(req.getMethod())) {
            if (referer == null) {
                // 状态置为404
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return false;
            }
            java.net.URL url = null;
            try {
                url = new java.net.URL(referer);
            } catch (MalformedURLException e) {
                // URL解析异常，也置为404
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return false;
            }
            // 首先判断请求域名和referer域名是否相同
            if (!host.equals(url.getHost())) {
                // 如果不等，判断是否在白名单中
                if (refererDomain.contains(url.getHost())) {
                    return true;
                }
                return false;
            }
        }
        return true;
    }
}
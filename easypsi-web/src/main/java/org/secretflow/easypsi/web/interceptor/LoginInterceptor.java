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

package org.secretflow.easypsi.web.interceptor;

import jakarta.annotation.Resource;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.secretflow.easypsi.common.constant.PermissionUserType;
import org.secretflow.easypsi.common.constant.PlatformType;
import org.secretflow.easypsi.common.constant.ResourceType;
import org.secretflow.easypsi.common.constant.UserOwnerType;
import org.secretflow.easypsi.common.dto.UserContextDTO;
import org.secretflow.easypsi.common.errorcode.AuthErrorCode;
import org.secretflow.easypsi.common.exception.EasyPsiException;
import org.secretflow.easypsi.common.util.UserContext;
import org.secretflow.easypsi.persistence.entity.AccountsDO;
import org.secretflow.easypsi.persistence.entity.TokensDO;
import org.secretflow.easypsi.persistence.repository.UserTokensRepository;
import org.secretflow.easypsi.service.EnvService;
import org.secretflow.easypsi.service.SysResourcesBizService;
import org.secretflow.easypsi.service.UserService;
import org.secretflow.easypsi.web.constant.AuthConstants;
import org.secretflow.easypsi.web.util.AuthUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Login interceptor
 *
 * @author : xiaonan.fhn
 * @date 2023/05/25
 */
@Component
@Slf4j
public class LoginInterceptor implements HandlerInterceptor {

    /**
     * Expiration time
     * one hour
     */
    private static final long EXPIRE = 60 * 60 * 24;

    private final UserTokensRepository userTokensRepository;

    private final EnvService envService;

    private final SysResourcesBizService sysResourcesBizService;

    private final UserService userService;

    @Value("${easypsi.auth.enabled:true}")
    private boolean enable;

    @Value("${server.http-port-inner}")
    private Integer innerHttpPort;

    @Resource
    private InnerPortPathConfig innerPortPathConfig;

    @Autowired
    public LoginInterceptor(UserTokensRepository userTokensRepository, EnvService envService,
                            SysResourcesBizService sysResourcesBizService, UserService userService) {
        this.userTokensRepository = userTokensRepository;
        this.envService = envService;
        this.sysResourcesBizService = sysResourcesBizService;
        this.userService = userService;
    }

    private UserContextDTO createTmpUserForPlaformType(PlatformType platformType) {
        if (envService.getPlatformType().equals(PlatformType.CENTER)) {
            UserContextDTO userContextDTO = new UserContextDTO();
            userContextDTO.setName("admin");
            userContextDTO.setOwnerId("kuscia-system");
            userContextDTO.setOwnerType(UserOwnerType.CENTER);
            userContextDTO.setToken("token");
            userContextDTO.setPlatformType(platformType);
            userContextDTO.setPlatformNodeId(envService.getPlatformNodeId());
            return userContextDTO;
        }
        if (envService.getPlatformType().equals(PlatformType.P2P)) {
            UserContextDTO userContextDTO = new UserContextDTO();
            userContextDTO.setName("admin");
            userContextDTO.setOwnerId(envService.getPlatformNodeId());
            userContextDTO.setOwnerType(UserOwnerType.P2P);
            userContextDTO.setToken("token");
            userContextDTO.setPlatformType(platformType);
            userContextDTO.setPlatformNodeId(envService.getPlatformNodeId());
            return userContextDTO;
        }
        UserContextDTO userContextDTO = new UserContextDTO();
        userContextDTO.setName("admin");
        userContextDTO.setOwnerId("nodeId");
        userContextDTO.setOwnerType(UserOwnerType.EDGE);
        userContextDTO.setToken("token");
        userContextDTO.setPlatformType(platformType);
        userContextDTO.setPlatformNodeId(envService.getPlatformNodeId());

        return userContextDTO;
    }

    /**
     * Check if intercept the request
     *
     * @param request  httpServletRequest
     * @param response httpServletResponse
     * @param handler
     * @return true is passed, false is intercepted
     */
    @Override
    public boolean preHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) throws HttpRequestMethodNotSupportedException {
        csrfDefense(response);
        if (!enable) {
            UserContextDTO admin = createTmpUserForPlaformType(envService.getPlatformType());
            UserContext.setBaseUser(admin);
            return true;
        }
        if (isOptionsVerb(request)) {
            return true;
        }
        if (log.isDebugEnabled()) {
            log.debug("Process by port: {}", request.getLocalPort());
        }
        if (innerHttpPort.equals(request.getLocalPort())) {
            processByNodeRpcRequest(request);
        } else {
            processByUserRequest(request);
        }
        return true;
    }

    private void csrfDefense(HttpServletResponse response) {
        String setCookie = response.getHeader("Set-Cookie");
        if (StringUtils.isBlank(setCookie)) {
            Cookie cookie = new Cookie(AuthConstants.CSRF_SAME_SITE, AuthConstants.CSRF_SAME_SITE_VALUE);
            response.addCookie(cookie);
        } else {
            StringBuilder cookie = new StringBuilder();
            cookie.append("; ").append(AuthConstants.CSRF_SAME_SITE).append("=").append(AuthConstants.CSRF_SAME_SITE_VALUE);
            response.addHeader("Set-Cookie", cookie.toString());
        }
    }

    private void processByNodeRpcRequest(HttpServletRequest request) {
        String sourceNodeId = request.getHeader("kuscia-origin-source");
        if (StringUtils.isBlank(sourceNodeId)) {
            throw EasyPsiException.of(AuthErrorCode.AUTH_FAILED, "Cannot find node id in header for rpc.");
        }
        UserContextDTO virtualUser = new UserContextDTO();
        virtualUser.setVirtualUserForNode(true);
        virtualUser.setName(sourceNodeId);
        virtualUser.setOwnerId(sourceNodeId);
        virtualUser.setOwnerType(UserOwnerType.P2P);
        virtualUser.setToken("token");
        virtualUser.setPlatformType(PlatformType.P2P);
        virtualUser.setPlatformNodeId(envService.getPlatformNodeId());


        // fill interface resource
        Set<String> resourceCodeSet = sysResourcesBizService.queryResourceCodeByUserName(PermissionUserType.NODE, ResourceType.INTERFACE, sourceNodeId);
        virtualUser.setInterfaceResources(resourceCodeSet);

        UserContext.setBaseUser(virtualUser);
    }

    private void processByUserRequest(HttpServletRequest request) throws HttpRequestMethodNotSupportedException {
        refuseByOutPortInvokeInnerPort(request);
        if (ignoreCheck(request)) {
            return;
        }
        String token = AuthUtils.findTokenInHeader(request);
        Optional<TokensDO> tokensDO = userTokensRepository.findByToken(token);
        if (tokensDO.isEmpty()) {
            throw EasyPsiException.of(AuthErrorCode.AUTH_FAILED, "Cannot find token in db, user not login in.");
        }
        AccountsDO userByName = userService.getUserByName(tokensDO.get().getName());
        if (userByName.getInitial() && !accessibleUrlNotInitialized(request.getServletPath())) {
            throw EasyPsiException.of(AuthErrorCode.PASSWORD_NOT_INITIALIZED);
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime gmtToken = tokensDO.get().getGmtToken();
        long until = gmtToken.until(now, ChronoUnit.SECONDS);
        if (until > EXPIRE) {
            throw EasyPsiException.of(AuthErrorCode.AUTH_FAILED, "The login session is expire, please login again.");
        }
        userTokensRepository.saveAndFlush(
                TokensDO.builder()
                        .name(tokensDO.get().getName())
                        .token(tokensDO.get().getToken())
                        .gmtToken(LocalDateTime.now())
                        .sessionData(tokensDO.get().getSessionData())
                        .build()
        );

        String sessionData = tokensDO.get().getSessionData();
        if (StringUtils.isBlank(sessionData)) {
            throw EasyPsiException.of(AuthErrorCode.AUTH_FAILED, "The login session is null, please login again.");
        }
        UserContextDTO userContextDTO = UserContextDTO.fromJson(sessionData);
        UserContext.setBaseUser(userContextDTO);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                Exception ex) {
        UserContext.remove();
    }

    private boolean isOptionsVerb(HttpServletRequest request) {
        return HttpMethod.OPTIONS.matches(request.getMethod());
    }

    /**
     * uri only invoke by innerPort
     *
     * @param request request
     */
    private void refuseByOutPortInvokeInnerPort(HttpServletRequest request) throws HttpRequestMethodNotSupportedException {
        if (innerPortPathConfig.getPath().contains(request.getServletPath())) {
            throw new HttpRequestMethodNotSupportedException("404");
        }
    }

    private boolean ignoreCheck(HttpServletRequest request) {
        return request.getMethod().equalsIgnoreCase("GET") && request.getServletPath().equals("/api/v1alpha1/project/job/result/download");
    }

    private boolean accessibleUrlNotInitialized(String url) {
        return List.of("/api/v1alpha1/user/updatePwd", "/api/get").contains(url);
    }

}

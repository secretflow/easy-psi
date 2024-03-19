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

package org.secretflow.easypsi.web.aop;

import org.secretflow.easypsi.common.annotation.resource.InterfaceResource;
import org.secretflow.easypsi.common.errorcode.AuthErrorCode;
import org.secretflow.easypsi.common.exception.EasyPsiException;
import org.secretflow.easypsi.common.util.UserContext;
import org.secretflow.easypsi.service.auth.InterfaceResourceAuth;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author beiwei
 * @date 2023/9/11
 */
@Aspect
@Component
public class InterfaceResourceAspect {
    @Autowired
    private InterfaceResourceAuth interfaceResourceAuth;


    @Pointcut("@annotation(org.secretflow.easypsi.common.annotation.resource.InterfaceResource)")
    public void pointCut() {
    }

    @Around("pointCut() && args(data) && @annotation(interfaceResource)")
    public Object check(ProceedingJoinPoint joinPoint, Object data, InterfaceResource interfaceResource) throws Throwable {
        if (!interfaceResourceAuth.check(interfaceResource.interfaceCode())){
            String err = String.format("No permission to access the interface(%s). owner_type(%s), owner_id(%s)",
                    interfaceResource.interfaceCode(),
                    UserContext.getUser().getOwnerType(),UserContext.getUser().getOwnerId());
            throw EasyPsiException.of(AuthErrorCode.AUTH_FAILED, err);
        }
        return joinPoint.proceed();
    }
}

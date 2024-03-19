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

package org.secretflow.easypsi.common.exception;

import org.secretflow.easypsi.common.errorcode.ErrorCode;

/**
 * EasyPsi Exception
 *
 * @author yansi
 * @date 2023/5/10
 */
public final class EasyPsiException extends RuntimeException {
    /**
     * error code
     */
    private final ErrorCode errorCode;
    /**
     * error args
     */
    private final String[] args;

    /**
     * Fill EasyPsi Exception
     *
     * @param errorCode
     * @param cause
     * @param args
     */
    private EasyPsiException(ErrorCode errorCode, Throwable cause, String... args) {
        super(args != null && args.length > 0 ? args[0] : "", cause);
        this.errorCode = errorCode;
        this.args = args;
    }

    /**
     * Build EasyPsi Exception with args
     *
     * @param errorCode
     * @param args
     * @return EasyPsi exception
     */
    public static EasyPsiException of(ErrorCode errorCode, String... args) {
        return new EasyPsiException(errorCode, null, args);
    }

    /**
     * Build EasyPsi Exception with cause
     *
     * @param errorCode
     * @param cause
     * @return EasyPsi exception
     */
    public static EasyPsiException of(ErrorCode errorCode, Throwable cause) {
        return new EasyPsiException(errorCode, cause, cause.getMessage());
    }

    /**
     * @return error code
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }

    /**
     * @return error args
     */
    public String[] getArgs() {
        return args;
    }
}
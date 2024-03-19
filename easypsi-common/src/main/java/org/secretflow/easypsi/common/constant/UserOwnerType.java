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

package org.secretflow.easypsi.common.constant;

import org.secretflow.easypsi.common.errorcode.SystemErrorCode;
import org.secretflow.easypsi.common.exception.EasyPsiException;

/**
 * @author beiwei
 * @date 2023/9/12
 */
public enum UserOwnerType {
    EDGE,
    CENTER,
    P2P,
    ;

    public PermissionUserType toPermissionUserType() {
        if (EDGE.equals(this)) {
            return PermissionUserType.EDGE_USER;
        }
        if (CENTER.equals(this)) {
            return PermissionUserType.USER;
        }
        if (P2P.equals(this)) {
            return PermissionUserType.NODE;
        }
        throw EasyPsiException.of(SystemErrorCode.VALIDATION_ERROR, "Invalidate user owner type: " + this.toString());
    }
}

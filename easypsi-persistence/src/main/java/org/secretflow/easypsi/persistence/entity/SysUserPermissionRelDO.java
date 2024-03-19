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

package org.secretflow.easypsi.persistence.entity;

import org.secretflow.easypsi.common.constant.PermissionTargetType;
import org.secretflow.easypsi.common.constant.PermissionUserType;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

/**
 * @author beiwei
 * @date 2023/9/13
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "sys_user_permission_rel")
public class SysUserPermissionRelDO extends SuperBaseAggregationRoot<SysUserPermissionRelDO> {
    @EmbeddedId
    private UPK upk;

    @Column(name = "user_type", nullable = false, length = 16)
    @Enumerated(EnumType.STRING)
    private PermissionUserType userType;

    @Column(name = "target_type", nullable = false, length = 16, columnDefinition = "default 'ROLE'")
    @Enumerated(EnumType.STRING)
    private PermissionTargetType targetType;



    /**
     * Project task unique primary key
     */
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Embeddable
    public static class UPK implements Serializable {

        @Column(name = "user_key", nullable = false, length = 16)
        private String userKey;

        @Column(name = "target_code", nullable = false, length = 64)
        private String targetCode;

    }
}

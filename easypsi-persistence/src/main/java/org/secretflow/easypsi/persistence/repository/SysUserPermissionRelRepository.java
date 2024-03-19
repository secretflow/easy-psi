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

package org.secretflow.easypsi.persistence.repository;

import org.secretflow.easypsi.persistence.entity.SysUserPermissionRelDO;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author beiwei
 * @date 2023/9/13
 */
public interface SysUserPermissionRelRepository extends JpaRepository<SysUserPermissionRelDO, SysUserPermissionRelDO.UPK> {
    @Query("from SysUserPermissionRelDO rrr where rrr.upk.userKey = :userName")
    List<SysUserPermissionRelDO> findByName(@Param("userName") String userName);

    @Modifying
    @Query(nativeQuery = true,value = "delete from sys_user_permission_rel where user_key =:userKey")
    void deleteByUserKey(String userKey);
}

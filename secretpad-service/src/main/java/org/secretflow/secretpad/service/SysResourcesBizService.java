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

package org.secretflow.secretpad.service;

import org.secretflow.secretpad.common.constant.PermissionUserType;
import org.secretflow.secretpad.common.constant.ResourceType;
import org.secretflow.secretpad.persistence.entity.SysResourceDO;

import java.util.List;
import java.util.Set;

/**
 * @author beiwei
 * @date 2023/9/15
 */
public interface SysResourcesBizService {
    List<SysResourceDO> queryResourceByUserName(PermissionUserType userType, String userId);
    Set<String> queryResourceCodeByUserName(PermissionUserType userType, ResourceType resourceType, String userId);

}

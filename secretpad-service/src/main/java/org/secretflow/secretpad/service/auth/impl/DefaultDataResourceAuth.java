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

package org.secretflow.secretpad.service.auth.impl;

import org.secretflow.secretpad.common.constant.UserOwnerType;
import org.secretflow.secretpad.common.constant.resource.DataResourceType;
import org.secretflow.secretpad.common.util.UserContext;
import org.secretflow.secretpad.service.auth.DataResourceAuth;

import lombok.extern.slf4j.Slf4j;

/**
 * @author beiwei
 * @date 2023/9/11
 */
//@Service
@Slf4j
public class DefaultDataResourceAuth implements DataResourceAuth {
    /**
     * @param resourceType resource type
     * @param resourceId   resource id
     * @return result
     */
    @Override
    public boolean check(DataResourceType resourceType, String resourceId) {
        UserOwnerType ownerType = UserContext.getUser().getOwnerType();
        if (UserOwnerType.CENTER.equals(ownerType)) {
            // Center user has all data permission
            return true;
        }

        if (DataResourceType.NODE_ID.equals(resourceType)) {
            return UserContext.getUser().getOwnerId().equals(resourceId);
        }

//        if (DataResourceType.PROJECT_ID.equals(resourceType)) {
//            return UserContext.getUser().containProjectId(resourceId);
//        }
        return false;
    }
}

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

package org.secretflow.secretpad.common.dto;

import org.secretflow.secretpad.common.constant.PlatformType;
import org.secretflow.secretpad.common.constant.UserOwnerType;
import org.secretflow.secretpad.common.util.JsonUtils;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.Set;

/**
 * @author beiwei
 * @date 2023/9/12
 */
@Setter
@Getter
@ToString
@NoArgsConstructor
public class UserContextDTO {
    private String token;
    private String name;
    private PlatformType platformType;
    private String platformNodeId;
    private UserOwnerType ownerType;
    private String ownerId;

    // TODO cache
    private Set<String> interfaceResources;

    /**
     * only for edge platform rpc.
     */
    private boolean virtualUserForNode;

    private boolean noviceUser;


    public boolean containInterfaceResource(String resourceCode) {
        if (StringUtils.isBlank(resourceCode)) {
            return false;
        }
        if (CollectionUtils.isEmpty(interfaceResources)) {
            return false;
        }
        return interfaceResources.contains(resourceCode);
    }

    public String toJsonStr() {
        return JsonUtils.toJSONString(this);
    }

    public static UserContextDTO fromJson(String jsonStr) {
        return JsonUtils.toJavaObject(jsonStr, UserContextDTO.class);
    }
}

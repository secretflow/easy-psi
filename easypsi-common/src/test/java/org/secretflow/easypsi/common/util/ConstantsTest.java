/*
 * Copyright 2024 Ant Group Co., Ltd.
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

package org.secretflow.easypsi.common.util;

import org.junit.jupiter.api.Test;
import org.secretflow.easypsi.common.constant.CacheConstants;
import org.secretflow.easypsi.common.constant.KusciaConstants;
import org.secretflow.easypsi.common.constant.UserOwnerType;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author chenmingliang
 * @date 2024/03/12
 */
public class ConstantsTest {

    @Test
    public void test() {
        assertEquals("notls", KusciaConstants.KUSCIA_PROTOCOL_NOTLS);
        assertEquals("tls", KusciaConstants.KUSCIA_PROTOCOL_TLS);
        assertEquals("mtls", KusciaConstants.KUSCIA_PROTOCOL_MTLS);
        assertEquals("user_lock", CacheConstants.USER_LOCK_CACHE);
    }

    @Test
    public void testUserType() {
        assertEquals("P2P", UserOwnerType.P2P.name());
        assertEquals("CENTER", UserOwnerType.CENTER.name());
        assertEquals("EDGE", UserOwnerType.EDGE.name());

    }
}

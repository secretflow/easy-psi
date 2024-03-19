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

package org.secretflow.easypsi.manager.integration.fabric;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.secretflow.easypsi.manager.properties.FabricAPIProperties;

/**
 * @author chenmingliang
 * @date 2024/03/12
 */
public class FabricManagerTest {

    @Test
    public void testValidateFile() {
        FabricAPIProperties fabricAPIProperties = new FabricAPIProperties();
        fabricAPIProperties.setIsOpen(true);
        FabricManager fabricManager = new FabricManager(fabricAPIProperties);

        Assertions.assertThrows(IllegalStateException.class,()->fabricManager.validateFile());
    }
}

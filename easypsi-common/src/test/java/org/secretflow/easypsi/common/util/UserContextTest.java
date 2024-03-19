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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.secretflow.easypsi.common.dto.UserContextDTO;
import org.secretflow.easypsi.common.errorcode.AuthErrorCode;
import org.secretflow.easypsi.common.exception.EasyPsiException;

/**
 * User Context Test
 *
 * @author lihaixin
 * @date 2024/03/08
 */
public class UserContextTest {

    private UserContextDTO testUser;

    @BeforeEach
    public void setUp() {
        testUser = new UserContextDTO();
        testUser.setName("TestUser");
    }

    @AfterEach
    public void tearDown() {
        UserContext.remove();
    }

    @Test
    public void testGetUserNameWhenSet() {
        UserContext.setBaseUser(testUser);
        Assertions.assertEquals("TestUser", UserContext.getUserName());
    }

    @Test
    public void testGetUserNameWhenNotSet() {
        Assertions.assertThrows(EasyPsiException.class, UserContext::getUserName, AuthErrorCode.AUTH_FAILED.toString());
    }

    @Test
    public void testSetAndGetUser() {
        UserContext.setBaseUser(testUser);
        UserContextDTO retrievedUser = UserContext.getUser();
        Assertions.assertEquals(testUser.getName(), retrievedUser.getName());
    }

    @Test
    public void testRemoveUser() {
        UserContext.setBaseUser(testUser);
        UserContext.remove();
        Assertions.assertThrows(EasyPsiException.class, UserContext::getUser, AuthErrorCode.AUTH_FAILED.toString());
    }
}
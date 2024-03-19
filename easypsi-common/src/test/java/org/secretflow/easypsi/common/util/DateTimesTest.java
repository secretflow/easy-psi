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

package org.secretflow.easypsi.common.util;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * @author yutu
 * @date 2023/08/23
 */
public class DateTimesTest {

    @Test
    public void test() {
        LocalDateTime utc = LocalDateTime.ofInstant(Instant.parse("2023-08-23T10:07:09Z"), ZoneId.of("Asia/Shanghai"));
        String s = DateTimes.toRfc3339(utc);
        System.out.println(s);
    }
}
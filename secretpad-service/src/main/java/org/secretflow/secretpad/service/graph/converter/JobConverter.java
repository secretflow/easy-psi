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

package org.secretflow.secretpad.service.graph.converter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Job converter
 *
 * @author yansi
 * @date 2023/5/30
 */
public interface JobConverter {
    Pattern RE_UNICODE = Pattern.compile("\\\\u([0-9a-zA-Z]{4})");

    /**
     * Replace unicode char from string
     *
     * @param s target string
     * @return a new string
     */
    default String decode(String s) {
        Matcher m = RE_UNICODE.matcher(s);
        StringBuilder sb = new StringBuilder(s.length());
        while (m.find()) {
            m.appendReplacement(sb,
                    Character.toString((char) Integer.parseInt(m.group(1), 16)));
        }
        m.appendTail(sb);
        return sb.toString();
    }
}

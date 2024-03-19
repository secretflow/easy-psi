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

package org.secretflow.easypsi.service.model;

/**
 * @author lihaixin
 * @date 2024/03/11
 */

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.secretflow.easypsi.service.model.common.EasyPsiPageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * EasyPsi Page Response Test
 *
 * @author lihaixin
 * @date 2024/03/12
 */
@ExtendWith(MockitoExtension.class)
public class EasyPsiPageResponseTest {

    @Test
    public void testToPageFromPage() {
        PageRequest pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id"));
        Page<String> mockPage = new PageImpl<>(Arrays.asList("item1", "item2"), pageable, 2L);
        EasyPsiPageResponse<String> response = EasyPsiPageResponse.toPage(mockPage);
        assertNotNull(response);
        assertEquals(2, response.getList().size());
        assertEquals(Arrays.asList("item1", "item2"), response.getList());
        assertEquals(2L, response.getTotal());
    }

    @Test
    public void testToPageFromContentAndTotal() {
        List<String> content = Arrays.asList("item1", "item2");
        long totalElements = 2L;
        EasyPsiPageResponse<String> response = EasyPsiPageResponse.toPage(content, totalElements);
        assertNotNull(response);
        assertEquals(2, response.getList().size());
        assertEquals(content, response.getList());
        assertEquals(totalElements, response.getTotal());
    }
}
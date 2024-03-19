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

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.secretflow.easypsi.service.model.node.UpdateNodeRequest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Update Node Request Test
 *
 * @author lihaixin
 * @date 2024/03/12
 */
@ExtendWith(MockitoExtension.class)
public class UpdateNodeRequestTest {


    @Test
    public void testNoArgsConstructor() {
        UpdateNodeRequest request = new UpdateNodeRequest();
        assertThat(request, notNullValue());
    }

    @Test
    public void testAllArgsConstructor() {
        String nodeId = "testNodeId";
        boolean trust = true;
        UpdateNodeRequest request = UpdateNodeRequest.builder()
                .nodeId(nodeId)
                .trust(trust)
                .build();

        assertThat(request.getNodeId(), equalTo(nodeId));
        assertThat(request.getTrust(), equalTo(trust));
    }

    @Test
    public void testSettersAndGetters() {
        UpdateNodeRequest request = new UpdateNodeRequest();

        String nodeId = "testNodeId";
        boolean trust = false;

        request.setNodeId(nodeId);
        request.setTrust(trust);

        assertThat(request.getNodeId(), Matchers.equalTo(nodeId));
        assertThat(request.getTrust(), Matchers.equalTo(trust));
    }

}
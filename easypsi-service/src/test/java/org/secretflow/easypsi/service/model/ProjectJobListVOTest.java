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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.secretflow.easypsi.persistence.model.GraphJobOperation;
import org.secretflow.easypsi.service.model.data.DataTableInformationVo;
import org.secretflow.easypsi.service.model.project.ProjectJobListVO;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Project Job List VO Test
 *
 * @author lihaixin
 * @date 2024/03/12
 */
@ExtendWith(MockitoExtension.class)
public class ProjectJobListVOTest {


    @Test
    public void testNoArgsConstructor() {
        ProjectJobListVO projectJobListVO = new ProjectJobListVO();
        assertNotNull(projectJobListVO);
    }

    @Test
    public void testAllArgsConstructor() {
        List<GraphJobOperation> operations = Arrays.asList(GraphJobOperation.AGREE, GraphJobOperation.REJECT);

        DataTableInformationVo.DataTableInformation initiatorInfo = new DataTableInformationVo.DataTableInformation();
        DataTableInformationVo.DataTableInformation partnerInfo = new DataTableInformationVo.DataTableInformation();

        ProjectJobListVO projectJobListVO = ProjectJobListVO.builder()
                .name("jobName")
                .srcNodeId("srcNodeId")
                .dstNodeId("dstNodeId")
                .operation(operations)
                .enabled(true)
                .initiatorDataTableInformation(initiatorInfo)
                .partnerdstDataTableInformation(partnerInfo)
                .dataTableConfirmation(true)
                .build();

        assertEquals("jobName", projectJobListVO.getName());
        assertEquals("srcNodeId", projectJobListVO.getSrcNodeId());
        assertEquals("dstNodeId", projectJobListVO.getDstNodeId());
        assertEquals(operations, projectJobListVO.getOperation());
        assertEquals(true, projectJobListVO.getEnabled());
        assertEquals(initiatorInfo, projectJobListVO.getInitiatorDataTableInformation());
        assertEquals(partnerInfo, projectJobListVO.getPartnerdstDataTableInformation());
        assertEquals(true, projectJobListVO.getDataTableConfirmation());
    }
}
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
import org.secretflow.easypsi.persistence.model.GraphJobStatus;
import org.secretflow.easypsi.service.model.project.ProjectJobListByBlackScreenVO;
import org.secretflow.easypsi.service.model.project.ProjectJobListVO;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Project Job List By Black Screen VO Test
 *
 * @author lihaixin
 * @date 2024/03/12
 */
@ExtendWith(MockitoExtension.class)
public class ProjectJobListByBlackScreenVOTest {


    @Test
    public void testFrom() {
        // 创建Mock对象
        ProjectJobListVO projectJobListVO = mock(ProjectJobListVO.class);

        // 设置Mock对象属性值
        String expectedJobId = "test_job_id";
        String expectedName = "test_name";
        String expectedSrcNodeId = "src_node";
        String expectedDstNodeId = "dst_node";
        List<GraphJobOperation> expectedOperations = Arrays.asList(GraphJobOperation.REJECT, GraphJobOperation.AGREE);
        GraphJobStatus expectedStatus = GraphJobStatus.RUNNING;
        String expectedGmtCreate = "2022-01-01 00:00:00";
        String expectedGmtFinished = "2022-01-02 00:00:00";
        String expectedErrMsg = "test_error_message";

        when(projectJobListVO.getJobId()).thenReturn(expectedJobId);
        when(projectJobListVO.getName()).thenReturn(expectedName);
        when(projectJobListVO.getSrcNodeId()).thenReturn(expectedSrcNodeId);
        when(projectJobListVO.getDstNodeId()).thenReturn(expectedDstNodeId);
        when(projectJobListVO.getOperation()).thenReturn(expectedOperations);
        when(projectJobListVO.getStatus()).thenReturn(expectedStatus);
        when(projectJobListVO.getGmtCreate()).thenReturn(expectedGmtCreate);
        when(projectJobListVO.getGmtFinished()).thenReturn(expectedGmtFinished);
        when(projectJobListVO.getErrMsg()).thenReturn(expectedErrMsg);

        // 调用待测试方法
        ProjectJobListByBlackScreenVO result = ProjectJobListByBlackScreenVO.from(projectJobListVO);

        // 验证结果
        assertEquals(expectedJobId, result.getJobId());
        assertEquals(expectedName, result.getName());
        assertEquals(expectedSrcNodeId, result.getSrcNodeId());
        assertEquals(expectedDstNodeId, result.getDstNodeId());
        assertEquals(expectedOperations, result.getOperation());
        assertEquals(expectedStatus, result.getStatus());
        assertEquals(expectedGmtCreate, result.getGmtCreate());
        assertEquals(expectedGmtFinished, result.getGmtFinished());
        assertEquals(expectedErrMsg, result.getErrMsg());
    }
}
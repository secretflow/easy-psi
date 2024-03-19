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

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.secretflow.easypsi.common.constant.JobConstants;
import org.secretflow.easypsi.service.model.project.CreateProjectJobRequest;
import org.secretflow.easypsi.service.model.project.CreateProjectJobTaskRequest;

import java.util.Arrays;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Create Project Job Task Request Test
 *
 * @author lihaixin
 * @date 2024/03/12
 */
@ExtendWith(MockitoExtension.class)
public class CreateProjectJobTaskRequestTest {

    private Validator validator;

    private CreateProjectJobTaskRequest taskRequest;
    private CreateProjectJobTaskRequest.PsiConfig taskPsiConfig;

    private CreateProjectJobRequest jobRequest;
    private CreateProjectJobRequest.PsiConfig jobPsiConfig;

    private CreateProjectJobRequest.AdvancedConfig advancedConfig;
    private CreateProjectJobTaskRequest.PsiConfig.OutputConfig outputConfig;
    private CreateProjectJobTaskRequest.PsiConfig.RecoveryConfig recoveryConfig;

    @BeforeEach
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        taskRequest = mock(CreateProjectJobTaskRequest.class);
        taskPsiConfig = mock(CreateProjectJobTaskRequest.PsiConfig.class);

        jobRequest = mock(CreateProjectJobRequest.class);
        jobPsiConfig = mock(CreateProjectJobRequest.PsiConfig.class);

        advancedConfig = mock(CreateProjectJobRequest.AdvancedConfig.class);
        outputConfig = mock(CreateProjectJobTaskRequest.PsiConfig.OutputConfig.class);
        recoveryConfig = mock(CreateProjectJobTaskRequest.PsiConfig.RecoveryConfig.class);
    }


    @Test
    public void testNoArgsConstructor() {
        CreateProjectJobTaskRequest request = new CreateProjectJobTaskRequest();
        assertNotNull(request);
    }

    @Test
    public void testSetNameAndGet() {
        CreateProjectJobTaskRequest request = new CreateProjectJobTaskRequest();
        request.setName("jobName");
        request.setJobId("testJobId");
        request.setDescription("testDescription");
        request.setPartnerConfig(taskPsiConfig);
        request.setInitiatorConfig(taskPsiConfig);
        assertNotNull(request.toString());
        assertNotNull(taskPsiConfig.toString());
        assertNotNull(mockAdvancedConfig().toString());
        assertNotNull(mockInputConfig().toString());
        assertNotNull(mockOutputConfig().toString());
        assertEquals("jobName", request.getName());
    }


    @Test
    public void testNameNotBlankValidation() {
        CreateProjectJobTaskRequest request = new CreateProjectJobTaskRequest();
        request.setName("");
        Set<ConstraintViolation<CreateProjectJobTaskRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
    }


    @Test
    public void testPsiConfigConstructor() {
        CreateProjectJobTaskRequest.PsiConfig psiConfig = new CreateProjectJobTaskRequest.PsiConfig(
                "testNodeId",
                mockProtocolConfig(),
                mockInputConfig(),
                mockOutputConfig(),
                mock(CreateProjectJobTaskRequest.PsiConfig.ContextDescProto.class),
                Arrays.asList("key1", "key2"),
                true,
                false,
                mockRecoveryConfig(),
                JobConstants.AdvancedJoinTypeEnum.ADVANCED_JOIN_TYPE_DIFFERENCE,
                "leftSide",
                true,
                "L0"
        );
        assertEquals("testNodeId", psiConfig.getNodeId());
    }

    @Test
    public void testFromMethod() {
        when(jobRequest.getAdvancedConfig()).thenReturn(mockAdvancedConfig());
        when(jobRequest.getOutputConfig()).thenReturn(mockPsiConfig());
        when(jobRequest.getName()).thenReturn("name");
        when(jobRequest.getDescription()).thenReturn("description");
        when(jobRequest.getPartnerConfig()).thenReturn(mockPsiConfig());
        when(jobRequest.getInitiatorConfig()).thenReturn(mockPsiConfig());
        when(jobPsiConfig.getNodeId()).thenReturn("testNodeId");


        CreateProjectJobTaskRequest.PsiConfig actualPsiConfig = CreateProjectJobTaskRequest.PsiConfig.from(jobRequest, jobPsiConfig, "testJobId");
        CreateProjectJobTaskRequest testJob = CreateProjectJobTaskRequest.fromJobRequest(jobRequest, "testJobId");
        assertEquals("name", testJob.getName());
        assertEquals("testNodeId", actualPsiConfig.getNodeId());
        assertEquals(true, actualPsiConfig.getSkipDuplicatesCheck());
        assertEquals(true, actualPsiConfig.getDisableAlignment());
        assertEquals(JobConstants.AdvancedJoinTypeEnum.ADVANCED_JOIN_TYPE_FULL_JOIN, actualPsiConfig.getAdvancedJoinType());
        assertEquals("leftSide", actualPsiConfig.getLeftSide());
        assertEquals(true, actualPsiConfig.getDataTableConfirmation());
    }

    private CreateProjectJobTaskRequest.PsiConfig.ProtocolConfig mockProtocolConfig() {
        return CreateProjectJobTaskRequest.PsiConfig.ProtocolConfig.builder()
                .protocol(JobConstants.ProtocolEnum.PROTOCOL_ECDH)
                .role(JobConstants.RoleEnum.ROLE_SENDER)
                .build();
    }

    private CreateProjectJobTaskRequest.PsiConfig.InputConfig mockInputConfig() {
        return CreateProjectJobTaskRequest.PsiConfig.InputConfig.builder()
                .type(JobConstants.DatatableTypeEnum.IO_TYPE_FILE_CSV.name())
                .path("mockInputPath")
                .build();
    }

    private CreateProjectJobTaskRequest.PsiConfig.OutputConfig mockOutputConfig() {
        return CreateProjectJobTaskRequest.PsiConfig.OutputConfig.builder()
                .type(JobConstants.DatatableTypeEnum.IO_TYPE_FILE_CSV.name())
                .path("mockOutputPath")
                .build();
    }

    private CreateProjectJobTaskRequest.PsiConfig.RecoveryConfig mockRecoveryConfig() {
        return CreateProjectJobTaskRequest.PsiConfig.RecoveryConfig.builder()
                .enabled(true)
                .folder("mockFolder")
                .build();
    }

    private CreateProjectJobRequest.AdvancedConfig mockAdvancedConfig() {
        CreateProjectJobRequest.AdvancedConfig.ProtocolConfig protocolConfig = new CreateProjectJobRequest.AdvancedConfig.ProtocolConfig();
        protocolConfig.setProtocol(JobConstants.ProtocolEnum.PROTOCOL_RR22);
        protocolConfig.setRr22Config(new CreateProjectJobRequest.AdvancedConfig.ProtocolConfig.Rr22Config());
        protocolConfig.setRole(JobConstants.RoleEnum.ROLE_RECEIVER);
        protocolConfig.setBroadcastResult(true);
        CreateProjectJobRequest.AdvancedConfig advancedConfig = new CreateProjectJobRequest.AdvancedConfig();
        advancedConfig.setProtocolConfig(protocolConfig);
        advancedConfig.setLinkConfig("linkConfigValue");
        advancedConfig.setSkipDuplicatesCheck(true);
        advancedConfig.setRecoveryEnabled(Boolean.TRUE);
        advancedConfig.setLeftSide("leftSide");
        advancedConfig.setAdvancedJoinType(JobConstants.AdvancedJoinTypeEnum.ADVANCED_JOIN_TYPE_FULL_JOIN);
        advancedConfig.setDisableAlignment(Boolean.TRUE);
        advancedConfig.setDataTableCount("10");
        advancedConfig.setDataTableConfirmation(Boolean.TRUE);
        return advancedConfig;
    }

    private CreateProjectJobRequest.PsiConfig mockPsiConfig() {
        CreateProjectJobRequest.PsiConfig psiConfig = new CreateProjectJobRequest.PsiConfig();
        psiConfig.setNodeId("testNodeId");
        psiConfig.setPath("path");
        psiConfig.setKeys(Arrays.asList("key1", "key2"));
        psiConfig.setBroadcastResult(Arrays.asList("result1", "result2"));
        return psiConfig;
    }


}
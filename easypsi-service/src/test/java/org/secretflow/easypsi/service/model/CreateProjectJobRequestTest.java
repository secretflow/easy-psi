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

import java.util.Arrays;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Create Project Job Request Test
 *
 * @author lihaixin
 * @date 2024/03/12
 */
@ExtendWith(MockitoExtension.class)
public class CreateProjectJobRequestTest {

    private Validator validator;

    @BeforeEach
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testNoArgsConstructor() {
        CreateProjectJobRequest request = new CreateProjectJobRequest();
        assertNotNull(request);
    }

    @Test
    public void testSetNameAndGet() {
        CreateProjectJobRequest request = new CreateProjectJobRequest();
        request.setName("jobName");
        assertEquals("jobName", request.getName());
    }

    @Test
    public void testNameNotBlankValidation() {
        CreateProjectJobRequest request = new CreateProjectJobRequest();
        request.setName("");
        Set<ConstraintViolation<CreateProjectJobRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
    }

    @Test
    public void testPsiConfigNoArgsConstructorAndSetterGetter() {
        CreateProjectJobRequest.PsiConfig psiConfig = new CreateProjectJobRequest.PsiConfig();
        psiConfig.setNodeId("nodeId");
        psiConfig.setPath("path");
        psiConfig.setKeys(Arrays.asList("key1", "key2"));
        psiConfig.setBroadcastResult(Arrays.asList("result1", "result2"));
        assertEquals("nodeId", psiConfig.getNodeId());
        assertEquals("path", psiConfig.getPath());
        assertEquals(Arrays.asList("key1", "key2"), psiConfig.getKeys());
        assertEquals(Arrays.asList("result1", "result2"), psiConfig.getBroadcastResult());
    }

    @Test
    public void testAdvancedConfigNoArgsConstructorAndSetterGetter() {
        CreateProjectJobRequest.AdvancedConfig.ProtocolConfig protocolConfig = new CreateProjectJobRequest.AdvancedConfig.ProtocolConfig();
        protocolConfig.setProtocol(JobConstants.ProtocolEnum.PROTOCOL_RR22);
        protocolConfig.setRole(JobConstants.RoleEnum.ROLE_RECEIVER);
        protocolConfig.setBroadcastResult(true);

        CreateProjectJobRequest.AdvancedConfig advancedConfig = new CreateProjectJobRequest.AdvancedConfig();
        advancedConfig.setProtocolConfig(protocolConfig);
        advancedConfig.setLinkConfig("linkConfigValue");
        advancedConfig.setSkipDuplicatesCheck(true);
        advancedConfig.setRecoveryEnabled(Boolean.TRUE);
        advancedConfig.setLeftSide("leftSide");
        advancedConfig.setAdvancedJoinType(JobConstants.AdvancedJoinTypeEnum.ADVANCED_JOIN_TYPE_DIFFERENCE);
        advancedConfig.setDisableAlignment(Boolean.TRUE);
        advancedConfig.setDataTableCount("10");
        advancedConfig.setDataTableConfirmation(Boolean.TRUE);

        assertEquals(protocolConfig, advancedConfig.getProtocolConfig());
        assertEquals("linkConfigValue", advancedConfig.getLinkConfig());
        assertEquals(true, advancedConfig.getSkipDuplicatesCheck());
        assertEquals(true, advancedConfig.getRecoveryEnabled());
        assertEquals("leftSide", advancedConfig.getLeftSide());
        assertEquals(JobConstants.AdvancedJoinTypeEnum.ADVANCED_JOIN_TYPE_DIFFERENCE, advancedConfig.getAdvancedJoinType());
        assertEquals(true, advancedConfig.getDisableAlignment());
        assertEquals("10", advancedConfig.getDataTableCount());
        assertEquals(true, advancedConfig.getDataTableConfirmation());

        assertEquals(JobConstants.ProtocolEnum.PROTOCOL_RR22, protocolConfig.getProtocol());
        assertEquals(JobConstants.RoleEnum.ROLE_RECEIVER, protocolConfig.getRole());
        assertEquals(true, protocolConfig.getBroadcastResult());
    }

    @Test
    public void testProtocolConfigNoArgsConstructorAndSetterGetter() {
        CreateProjectJobRequest.AdvancedConfig.ProtocolConfig.EcdhConfig ecdhConfig = new CreateProjectJobRequest.AdvancedConfig.ProtocolConfig.EcdhConfig();
        ecdhConfig.setCurve(JobConstants.CurveType.CURVE_FOURQ.name());

        CreateProjectJobRequest.AdvancedConfig.ProtocolConfig.KkrtConfig kkrtConfig = new CreateProjectJobRequest.AdvancedConfig.ProtocolConfig.KkrtConfig();
        kkrtConfig.setBucketSize(JobConstants.BUCKET_SIZE);

        CreateProjectJobRequest.AdvancedConfig.ProtocolConfig.Rr22Config rr22Config = new CreateProjectJobRequest.AdvancedConfig.ProtocolConfig.Rr22Config();
        rr22Config.setBucketSize(JobConstants.BUCKET_SIZE);
        rr22Config.setLowCommMode(false);

        CreateProjectJobRequest.AdvancedConfig.ProtocolConfig protocolConfig = new CreateProjectJobRequest.AdvancedConfig.ProtocolConfig();
        protocolConfig.setProtocol(JobConstants.ProtocolEnum.PROTOCOL_RR22);
        protocolConfig.setRole(JobConstants.RoleEnum.ROLE_RECEIVER);
        protocolConfig.setBroadcastResult(true);
        protocolConfig.setEcdhConfig(ecdhConfig);
        protocolConfig.setKkrtConfig(kkrtConfig);
        protocolConfig.setRr22Config(rr22Config);

        assertEquals(JobConstants.ProtocolEnum.PROTOCOL_RR22, protocolConfig.getProtocol());
        assertEquals(JobConstants.RoleEnum.ROLE_RECEIVER, protocolConfig.getRole());
        assertEquals(ecdhConfig, protocolConfig.getEcdhConfig());
        assertEquals(kkrtConfig, protocolConfig.getKkrtConfig());
        assertEquals(rr22Config, protocolConfig.getRr22Config());
    }
}
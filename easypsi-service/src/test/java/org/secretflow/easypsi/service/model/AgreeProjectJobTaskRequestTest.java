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

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.secretflow.easypsi.service.model.project.AgreeProjectJobTaskRequest;

import java.util.Set;

/**
 * Agree Project Job Task Request Test
 *
 * @author lihaixin
 * @date 2024/03/12
 */
@ExtendWith(MockitoExtension.class)
public class AgreeProjectJobTaskRequestTest {

    private Validator validator;

    @BeforeEach
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testValidJobId() {
        AgreeProjectJobTaskRequest request = AgreeProjectJobTaskRequest.builder().jobId("valid-job-id").build();
        Set<?> violations = validator.validate(request);
        assert violations.isEmpty();
    }

    @Test
    public void testBlankJobId() {
        AgreeProjectJobTaskRequest request = AgreeProjectJobTaskRequest.builder().jobId("").build();
        Set<?> violations = validator.validate(request);
        assert !violations.isEmpty();
    }

    @Test
    public void testNullJobId() {
        AgreeProjectJobTaskRequest request = AgreeProjectJobTaskRequest.builder().jobId(null).build();
        Set<?> violations = validator.validate(request);
        assert !violations.isEmpty();
    }
}
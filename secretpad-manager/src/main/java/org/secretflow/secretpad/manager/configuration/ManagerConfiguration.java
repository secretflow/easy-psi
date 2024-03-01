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

package org.secretflow.secretpad.manager.configuration;

import org.secretflow.secretpad.manager.integration.job.AbstractJobManager;
import org.secretflow.secretpad.manager.integration.job.JobManager;
import org.secretflow.secretpad.persistence.repository.ProjectJobRepository;

import org.secretflow.v1alpha1.kusciaapi.JobServiceGrpc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Manager configuration init bean
 *
 * @author yansi
 * @date 2023/5/23
 */
@Configuration
public class ManagerConfiguration {

    /**
     * Create a new abstract job manager via repositories and stubs
     *
     * @param projectJobRepository
     * @param jobStub
     * @return abstract job manager
     */
    @Bean
    AbstractJobManager jobManager(
            ProjectJobRepository projectJobRepository,
            JobServiceGrpc.JobServiceBlockingStub jobStub
    ) {
        return new JobManager(projectJobRepository, jobStub);
    }

}

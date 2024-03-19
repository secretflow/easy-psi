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

package org.secretflow.easypsi.service.configuration;

import org.secretflow.easypsi.service.graph.JobChain;
import org.secretflow.easypsi.service.graph.chain.AbstractJobHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;

/**
 * Configuration for service layer
 *
 * @author yansi
 * @date 2023/5/30
 */
@EnableScheduling
@Configuration
public class ServiceConfiguration {

    /**
     * Job chain for all job handlers
     *
     * @param jobHandlers all job handlers
     * @return job chain
     */
    @Bean
    JobChain jobChain(List<AbstractJobHandler> jobHandlers) {
        return new JobChain<>(jobHandlers);
    }
}

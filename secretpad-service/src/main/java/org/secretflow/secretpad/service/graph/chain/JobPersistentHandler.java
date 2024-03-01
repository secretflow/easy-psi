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

package org.secretflow.secretpad.service.graph.chain;

import org.secretflow.secretpad.manager.integration.job.JobManager;
import org.secretflow.secretpad.persistence.repository.ProjectJobRepository;
import org.secretflow.secretpad.service.model.graph.ProjectPsiJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * Persist federal table, model, rule
 *
 * @author yansi
 * @date 2023/5/30
 */
@Component
public class JobPersistentHandler extends AbstractJobHandler<ProjectPsiJob> {

    private final static Logger LOGGER = LoggerFactory.getLogger(JobManager.class);
    @Autowired
    private ProjectJobRepository projectJobRepository;

    @Override
    public int getOrder() {
        return 1;
    }

    /**
     * Persist job tasks and add logs
     *
     * @param job target job
     */
    @Override
    public void doHandler(ProjectPsiJob job) {
        projectJobRepository.startJob(job.getJobId(), job.getStartTime());
        if (next != null) {
            next.doHandler(job);
        }
    }
}

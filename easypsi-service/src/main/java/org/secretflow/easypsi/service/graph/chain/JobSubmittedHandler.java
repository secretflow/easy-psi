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

package org.secretflow.easypsi.service.graph.chain;

import org.secretflow.easypsi.manager.integration.job.AbstractJobManager;
import org.secretflow.easypsi.service.graph.converter.KusciaJobConverter;
import org.secretflow.easypsi.service.model.graph.ProjectPsiJob;

import org.secretflow.v1alpha1.kusciaapi.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Submit job handler
 *
 * @author yansi
 * @date 2023/6/8
 */
@Component
public class JobSubmittedHandler extends AbstractJobHandler<ProjectPsiJob> {
    @Autowired
    private AbstractJobManager jobManager;
    @Autowired
    private KusciaJobConverter jobConverter;

    @Override
    public int getOrder() {
        return 3;
    }

    /**
     * Save project job data and create a new job
     *
     * @param job target job
     */
    @Override
    public void doHandler(ProjectPsiJob job) {
        Job.CreateJobRequest request = jobConverter.psiConverter(job);
        jobManager.createJob(request);
        if (next != null) {
            next.doHandler(job);
        }
    }
}

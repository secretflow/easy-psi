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

package org.secretflow.easypsi.manager.integration.job;


import org.secretflow.easypsi.persistence.entity.ProjectJobDO;

import org.secretflow.v1alpha1.kusciaapi.Job;

import java.util.concurrent.Future;

/**
 * @author yansi
 * @date 2023/5/23
 */
public abstract class AbstractJobManager {
    /**
     * Start synchronized job
     */
    public abstract void startSync();

    /**
     * Create a new job
     *
     * @param request create job request
     */
    public abstract void createJob(Job.CreateJobRequest request);

    public abstract Future<ProjectJobDO> openProjectJob(String jobId);
}

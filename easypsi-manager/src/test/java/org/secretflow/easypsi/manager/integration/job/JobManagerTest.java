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

package org.secretflow.easypsi.manager.integration.job;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.secretflow.easypsi.common.exception.EasyPsiException;
import org.secretflow.v1alpha1.common.Common;
import org.secretflow.v1alpha1.kusciaapi.Job;
import org.secretflow.v1alpha1.kusciaapi.JobServiceGrpc;


/**
 * @author chenmingliang
 * @date 2024/03/12
 */
@ExtendWith(MockitoExtension.class)
public class JobManagerTest {

    @Mock
    JobServiceGrpc.JobServiceBlockingStub jobStub;

    @Test
    public void startSyncTest() {
        JobManager jobManager = new JobManager(null,jobStub);
        jobManager.startSync();
    }

    @Test
    public void createJob() {
        JobManager jobManager = new JobManager(null,jobStub);
        Job.CreateJobRequest createJobRequest = Job.CreateJobRequest.newBuilder().build();
        Job.CreateJobResponse createJobResponse = Job.CreateJobResponse.newBuilder().build();
        Mockito.when(jobStub.createJob(createJobRequest)).thenReturn(createJobResponse);
        Assertions.assertThrows(EasyPsiException.class,()->jobManager.createJob(createJobRequest));

        Job.CreateJobRequest createJobRequestA = Job.CreateJobRequest.newBuilder().build();
        Job.CreateJobResponse createJobResponseA = Job.CreateJobResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(400).build()).build();

        Mockito.when(jobStub.createJob(createJobRequestA)).thenReturn(createJobResponseA);
        Assertions.assertThrows(EasyPsiException.class,()->jobManager.createJob(createJobRequestA));
    }
}

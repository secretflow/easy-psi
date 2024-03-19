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

package org.secretflow.easypsi.manager.kuscia.grpc.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.secretflow.easypsi.manager.kuscia.grpc.KusciaJobRpc;
import org.secretflow.v1alpha1.kusciaapi.Job;
import org.secretflow.v1alpha1.kusciaapi.JobServiceGrpc;

/**
 * @author chenmingliang
 * @date 2024/03/12
 */
@ExtendWith(MockitoExtension.class)
public class KusciaJobRpcImplTest {

    @Mock
    JobServiceGrpc.JobServiceBlockingStub jobServiceStub;
    @Test
    public void testQueryJob() {
        KusciaJobRpc kusciaJobRpc = new KusciaJobRpcImpl(jobServiceStub);
        Mockito.when(jobServiceStub.queryJob(Mockito.any())).thenReturn(Job.QueryJobResponse.newBuilder().build());
        kusciaJobRpc.queryJob(Job.QueryJobRequest.newBuilder().build());
    }

    @Test
    public void approveJob() {
        KusciaJobRpc kusciaJobRpc = new KusciaJobRpcImpl(jobServiceStub);
        Mockito.when(jobServiceStub.approveJob(Mockito.any())).thenReturn(Job.ApproveJobResponse.newBuilder().build());
        kusciaJobRpc.approveJob(Job.ApproveJobRequest.newBuilder().build());
    }
}

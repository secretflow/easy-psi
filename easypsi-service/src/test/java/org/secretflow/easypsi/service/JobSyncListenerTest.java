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

package org.secretflow.easypsi.service;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.secretflow.easypsi.manager.integration.job.JobManager;
import org.secretflow.easypsi.service.listener.JobSyncListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

/**
 * Job Sync Listener Test
 *
 * @author lihaixin
 * @date 2024/03/08
 */
@SpringBootTest(classes = {JobManager.class, JobSyncListener.class})
public class JobSyncListenerTest {

    @MockBean
    private JobManager jobManager;

    @MockBean
    private JobSyncListener jobSyncListener;


    @Test
    public void testOnApplicationEvent() {
        ApplicationReadyEvent event = Mockito.mock(ApplicationReadyEvent.class);
        Mockito.doNothing().when(jobManager).startSync();
        jobSyncListener.onApplicationEvent(event);
        //exception
        Mockito.doThrow(new RuntimeException()).when(jobManager).startSync();
        jobSyncListener.onApplicationEvent(event);
    }
}
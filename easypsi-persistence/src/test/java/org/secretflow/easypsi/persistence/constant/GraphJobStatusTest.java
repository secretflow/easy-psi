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

package org.secretflow.easypsi.persistence.constant;

import org.junit.jupiter.api.Test;
import org.secretflow.easypsi.persistence.model.GraphJobOperation;
import org.secretflow.easypsi.persistence.model.GraphJobStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Graph Job Status Test
 *
 * @author lihaixin
 * @date 2024/03/13
 */
public class GraphJobStatusTest {


    @Test
    public void testFormKusciaJobStatus() {
        assertEquals(GraphJobStatus.SUCCEEDED, GraphJobStatus.formKusciaJobStatus("Succeeded"));
        assertEquals(GraphJobStatus.FAILED, GraphJobStatus.formKusciaJobStatus("Failed"));
        assertEquals(GraphJobStatus.CANCELED, GraphJobStatus.formKusciaJobStatus("Cancelled"));
        assertEquals(GraphJobStatus.RUNNING, GraphJobStatus.formKusciaJobStatus("Running"));
        assertEquals(GraphJobStatus.PENDING_REVIEW, GraphJobStatus.formKusciaJobStatus("AwaitingApproval"));
        assertEquals(GraphJobStatus.REJECTED, GraphJobStatus.formKusciaJobStatus("ApprovalReject"));
        assertEquals(GraphJobStatus.RUNNING, GraphJobStatus.formKusciaJobStatus("UnknownStatus"));
    }

    @Test
    public void testCheckJobFinalStatus() {
        assertTrue(GraphJobStatus.checkJobFinalStatus("Succeeded"));
        assertTrue(GraphJobStatus.checkJobFinalStatus("Failed"));
        assertTrue(GraphJobStatus.checkJobFinalStatus("Running"));
        assertTrue(GraphJobStatus.checkJobFinalStatus("PendingCert"));
        assertTrue(GraphJobStatus.checkJobFinalStatus("PendingReview"));
        assertTrue(GraphJobStatus.checkJobFinalStatus("Paused"));
        assertTrue(GraphJobStatus.checkJobFinalStatus("Timeout"));
        assertTrue(GraphJobStatus.checkJobFinalStatus("Canceled"));
    }

    @Test
    public void testGetUnFinalStatus() {
        List<String> unFinalStatuses = GraphJobStatus.getUnFinalStatus();
        assertTrue(unFinalStatuses.containsAll(List.of("RUNNING", "PENDING_CERT", "PENDING_REVIEW", "PAUSED", "TIMEOUT", "FAILED")));
        assertFalse(unFinalStatuses.contains("SUCCEEDED"));
    }


    @Test
    public void testCheckOperation() {
        assertTrue(GraphJobStatus.checkOperation(GraphJobStatus.PENDING_REVIEW, GraphJobOperation.AGREE));
        assertFalse(GraphJobStatus.checkOperation(GraphJobStatus.RUNNING, GraphJobOperation.AGREE));

        assertTrue(GraphJobStatus.checkOperation(GraphJobStatus.PENDING_REVIEW, GraphJobOperation.REJECT));
        assertFalse(GraphJobStatus.checkOperation(GraphJobStatus.RUNNING, GraphJobOperation.REJECT));

        assertTrue(GraphJobStatus.checkOperation(GraphJobStatus.RUNNING, GraphJobOperation.PAUSE));
        assertFalse(GraphJobStatus.checkOperation(GraphJobStatus.SUCCEEDED, GraphJobOperation.PAUSE));

        assertTrue(GraphJobStatus.checkOperation(GraphJobStatus.PAUSED, GraphJobOperation.CONTINUE));
        assertFalse(GraphJobStatus.checkOperation(GraphJobStatus.RUNNING, GraphJobOperation.CONTINUE));

        assertTrue(GraphJobStatus.checkOperation(GraphJobStatus.RUNNING, GraphJobOperation.CANCEL));
        assertFalse(GraphJobStatus.checkOperation(GraphJobStatus.SUCCEEDED, GraphJobOperation.CANCEL));

        assertTrue(GraphJobStatus.checkOperation(GraphJobStatus.TIMEOUT, GraphJobOperation.DELETE));
        assertFalse(GraphJobStatus.checkOperation(GraphJobStatus.RUNNING, GraphJobOperation.DELETE));

        assertTrue(GraphJobStatus.checkOperation(GraphJobStatus.SUCCEEDED, GraphJobOperation.DOWNLOAD_RESULT));
        assertFalse(GraphJobStatus.checkOperation(GraphJobStatus.RUNNING, GraphJobOperation.DOWNLOAD_RESULT));
    }
}
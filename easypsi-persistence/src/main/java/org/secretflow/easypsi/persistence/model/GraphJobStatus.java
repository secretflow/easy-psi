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

package org.secretflow.easypsi.persistence.model;

import org.secretflow.easypsi.common.constant.JobConstants;
import org.secretflow.easypsi.common.util.JsonUtils;
import org.secretflow.easypsi.persistence.entity.ProjectJobDO;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Graph job status enum
 *
 * @author yansi
 * @date 2023/5/30
 */
public enum GraphJobStatus {

    /**
     * PendingCert status, cert is empty
     */
    PENDING_CERT("PendingCert", "待审核"),
    /**
     * PendingReview status
     */
    PENDING_REVIEW("PendingReview", "待审核"),
    /**
     * Running status
     * For a node, it is fired and still running by the backend.
     * For a pipeline, at least one of its nodes is still running.
     */
    RUNNING("Running", "运行中"),

    /**
     * Paused
     */
    PAUSED("Paused", "已暂停"),
    /**
     * Failed, timeout
     */
    TIMEOUT("Timeout", "已超时"),
    /**
     * Failed, canceled
     */
    CANCELED("Canceled", "已取消"),
    /**
     * Failed, rejected
     */
    REJECTED("Rejected", "已拒绝"),
    /**
     * Finished, Succeeded
     */
    SUCCEEDED("Succeeded", "成功"),

    /**
     * Finished, run exception
     */
    FAILED("Failed", "失败");

    private final String val;

    private final String name;

    GraphJobStatus(String val, String name) {
        this.val = val;
        this.name = name;
    }

    /**
     * Convert graph job status from apiLite job status
     *
     * @param status
     * @return graph job status class
     */
    public static GraphJobStatus formKusciaJobStatus(String status) {
        switch (status) {
            case "Succeeded":
                return SUCCEEDED;
            case "Failed":
                return FAILED;
            case "Cancelled":
                return CANCELED;
            case "Running":
                return RUNNING;
            case "AwaitingApproval":
                return PENDING_REVIEW;
            case "ApprovalReject":
                return REJECTED;
            default:
                return RUNNING;
        }
    }

    public static boolean checkJobFinalStatus(String status) {
        return !(RUNNING.name().equals(status) || PENDING_CERT.name().equals(status) || PENDING_REVIEW.name().equals(status)
                || PAUSED.name().equals(status) || FAILED.name().equals(status));
    }

    public static List<String> getUnFinalStatus() {
        return List.of(
                RUNNING.name(),
                PENDING_CERT.name(),
                PENDING_REVIEW.name(),
                PAUSED.name(),
                TIMEOUT.name(),
                FAILED.name()
        );
    }

    public static List<GraphJobOperation> fromStatusToOperation(GraphJobStatus status, String nodeId, ProjectJobDO jobDO) {
        List<GraphJobOperation> operations = new ArrayList<>();
        switch (status) {
            case PENDING_REVIEW:
                if (StringUtils.equals(nodeId, jobDO.getInitiatorNodeId())) {
                    operations.add(GraphJobOperation.CANCEL);
                    return operations;
                }
                if (StringUtils.equals(nodeId, jobDO.getPartnerNodeId())) {
                    operations.add(GraphJobOperation.AGREE);
                    operations.add(GraphJobOperation.REJECT);
                    return operations;
                }
            case RUNNING:
                operations.add(GraphJobOperation.LOG);
                operations.add(GraphJobOperation.PAUSE);
                operations.add(GraphJobOperation.CANCEL);
                return operations;
            case PAUSED:
                operations.add(GraphJobOperation.LOG);
                operations.add(GraphJobOperation.CONTINUE);
                operations.add(GraphJobOperation.CANCEL);
                return operations;
            case TIMEOUT:
                operations.add(GraphJobOperation.LOG);
                operations.add(GraphJobOperation.DELETE);
                operations.add(GraphJobOperation.CONTINUE);
                return operations;
            case CANCELED:
                operations.add(GraphJobOperation.LOG);
                operations.add(GraphJobOperation.DELETE);
                return operations;
            case REJECTED:
                operations.add(GraphJobOperation.DELETE);
                return operations;
            case FAILED:
                operations.add(GraphJobOperation.LOG);
                operations.add(GraphJobOperation.CONTINUE);
                operations.add(GraphJobOperation.CANCEL);
                return operations;
            case SUCCEEDED:
                operations.add(GraphJobOperation.LOG);
                operations.add(GraphJobOperation.DELETE);
                if (StringUtils.equals(nodeId, jobDO.getPartnerNodeId())) {
                    PsiConfigDO partnerPsiConfigDO = JsonUtils.toJavaObject(jobDO.getPartnerConfig(), PsiConfigDO.class);
                    if (partnerPsiConfigDO.getProtocolConfig().getBroadcastResult()) {
                        operations.add(GraphJobOperation.DOWNLOAD_RESULT);
                    } else if (StringUtils.equals(partnerPsiConfigDO.getProtocolConfig().getRole().name(), JobConstants.RoleEnum.ROLE_RECEIVER.name())) {
                        operations.add(GraphJobOperation.DOWNLOAD_RESULT);
                    }
                }
                if (StringUtils.equals(nodeId, jobDO.getInitiatorNodeId())) {
                    PsiConfigDO initiatorPsiConfigDO = JsonUtils.toJavaObject(jobDO.getInitiatorConfig(), PsiConfigDO.class);
                    if (initiatorPsiConfigDO.getProtocolConfig().getBroadcastResult()) {
                        operations.add(GraphJobOperation.DOWNLOAD_RESULT);
                    } else if (StringUtils.equals(initiatorPsiConfigDO.getProtocolConfig().getRole().name(), JobConstants.RoleEnum.ROLE_RECEIVER.name())) {
                        operations.add(GraphJobOperation.DOWNLOAD_RESULT);
                    }
                }
                return operations;
            default:
                return operations;
        }
    }

    public static String getName(GraphJobStatus status) {
        return status.name;
    }

    public static Boolean checkOperation(GraphJobStatus status, GraphJobOperation operation) {
        switch (operation) {
            case AGREE:
                switch (status) {
                    case PENDING_REVIEW:
                        return true;
                    default:
                        return false;
                }
            case REJECT:
                switch (status) {
                    case PENDING_REVIEW:
                        return true;
                    default:
                        return false;
                }
            case PAUSE:
                switch (status) {
                    case RUNNING:
                        return true;
                    default:
                        return false;
                }
            case CONTINUE:
                switch (status) {
                    case PAUSED:
                    case TIMEOUT:
                    case FAILED:
                        return true;
                    default:
                        return false;
                }
            case CANCEL:
                switch (status) {
                    case PENDING_REVIEW:
                    case RUNNING:
                    case PAUSED:
                    case FAILED:
                        return true;
                    default:
                        return false;
                }
            case DELETE:
                switch (status) {
                    case TIMEOUT:
                    case CANCELED:
                    case REJECTED:
                    case SUCCEEDED:
                        return true;
                    default:
                        return false;
                }
            case LOG:
                switch (status) {
                    case RUNNING:
                    case PAUSED:
                    case TIMEOUT:
                    case CANCELED:
                    case FAILED:
                    case SUCCEEDED:
                        return true;
                    default:
                        return false;
                }
            case DOWNLOAD_RESULT:
                switch (status) {
                    case SUCCEEDED:
                        return true;
                    default:
                        return false;
                }
            default:
                return false;
        }
    }
}

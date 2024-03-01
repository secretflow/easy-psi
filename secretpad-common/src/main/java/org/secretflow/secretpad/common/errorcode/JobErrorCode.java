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

package org.secretflow.secretpad.common.errorcode;

/**
 * Job errorCode
 *
 * @author yansi
 * @date 2023/5/30
 */
public enum JobErrorCode implements ErrorCode {
    /**
     * The project job does not exist
     */
    PROJECT_JOB_NOT_EXISTS(202011901),
    /**
     * Failed to create the project job
     */
    PROJECT_JOB_CREATE_ERROR(202011902),
    /**
     * Csv data not exists
     */
    PROJECT_DATA_NOT_EXISTS_ERROR(202011905),
    /**
     * project job logs not exists
     */
    PROJECT_LOG_NOT_EXISTS_ERROR(202011906),
    /**
     * project job logs not exists
     */
    PROJECT_JOB_RESULT_DOWNLOAD_ERROR(202011908),
    /**
     * check projec job table header not exists
     */
    PROJECT_TABLE_HEADER_NOT_EXISTS_ERROR(202011909),

    PROJECT_JOB_RPC_ERROR(202011910),

    PROJECT_JOB_ACTION_NOT_ALLOWED(202011911),
      /**
     * Csv data path not exists
     */
    PROJECT_DATA_PATH_NOT_EXISTS_ERROR(202011912),
    /**
     * project job logs path not exists
     */
    PROJECT_LOG_PATH_NOT_EXISTS_ERROR(202011913),
    /**
     * project job result expired
     */
    PROJECT_JOB_RESULT_HASH_EXPIRED_ERROR(202011914),




    ;

    private final int code;

    JobErrorCode(int code) {
        this.code = code;
    }

    @Override
    public String getMessageKey() {
        return "project_job." + this.name();
    }

    @Override
    public Integer getCode() {
        return code;
    }
}

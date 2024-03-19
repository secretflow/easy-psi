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

package org.secretflow.easypsi.service.model.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * EasyPsi common response
 *
 * @author yansi
 * @date 2023/5/10
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class EasyPsiResponse<T> {
    @Schema(description = "status information")
    private EasyPsiResponseStatus status;
    @Schema
    private T data;

    /**
     * Build successful easyPsi response with data
     *
     * @param data return data
     * @param <T>
     * @return successful easyPsi response with data
     */
    public static <T> EasyPsiResponse<T> success(T data) {
        return new EasyPsiResponse<>(new EasyPsiResponseStatus(0, "操作成功"), data);
    }

    /**
     * Build successful EasyPsi response
     *
     * @param <T>
     * @return successful EasyPsi response
     */
    public static <T> EasyPsiResponse<T> success() {
        return success(null);
    }

    /**
     * EasyPsi response status class
     */
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class EasyPsiResponseStatus {
        /**
         * Status code
         */
        @Schema(description = "status code")
        private Integer code;
        /**
         * Status message
         */
        @Schema(description = "status message")
        private String msg;
    }
}

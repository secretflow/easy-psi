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

/**
 * Graph job operation enum
 *
 * @author guyu
 * @date 2023/10/27
 */
public enum GraphJobOperation {

    AGREE("agree", "同意"),

    REJECT("reject", "拒绝"),

    PAUSE("pause", "暂停"),

    CONTINUE("continue", "继续"),

    CANCEL("cancel", "取消"),

    DELETE("delete", "删除"),

    LOG("log", "查看日志"),

    DOWNLOAD_RESULT("downloadResult", "下载结果"),

    UPLOAD_CERT("uploadCert", "上传证书");


    private final String val;

    private final String name;

    GraphJobOperation(String val, String name) {
        this.val = val;
        this.name = name;
    }

    public static String getName(GraphJobOperation operation) {
        return operation.name;
    }
}

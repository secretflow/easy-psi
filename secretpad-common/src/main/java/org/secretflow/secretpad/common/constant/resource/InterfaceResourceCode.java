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

package org.secretflow.secretpad.common.constant.resource;

/**
 * @author beiwei
 * @date 2023/9/11
 */
public interface InterfaceResourceCode {
    String ALL_INTERFACE_RESOURCE = "ALL_INTERFACE_RESOURCE";
    String NODE_CREATE = "NODE_CREATE";
    String NODE_GET = "NODE_GET";
    String NODE_DELETE = "NODE_DELETE";
    String AUTH_LOGIN = "AUTH_LOGIN";
    String AUTH_LOGOUT = "AUTH_LOGOUT";
    String DATA_HOST_PATH = "DATA_HOST_PATH";
    String DATA_VERSION = "DATA_VERSION";
    String DATA_COUNT = "DATA_COUNT";
    String DATA_COUNT_KUSCIA = "DATA_COUNT_KUSCIA";

    String INDEX = "INDEX";
    String NODE_ROUTE_UPDATE = "NODE_ROUTE_UPDATE";
    String NODE_ROUTE_REFRESH = "NODE_ROUTE_REFRESH";
    String NODE_ROUTE_TEST = "NODE_ROUTE_TEST";
    String NODE_ROUTE_LIST = "NODE_ROUTE_LIST";
    String PRJ_JOB_LIST = "PRJ_JOB_LIST";
    String PRJ_JOB_GET = "PRJ_JOB_GET";
    String PRJ_JOB_STOP = "PRJ_JOB_STOP";
    String PRJ_JOB_STOP_KUSCIA = "PRJ_JOB_STOP_KUSCIA";
    String PRJ_JOB_LOGS = "PRJ_JOB_LOGS";
    String PRJ_DATA_HEADER = "PRJ_DATA_HEADER";
    String USER_GET = "USER_GET";

    String USER_UPDATE_PWD = "USER_UPDATE_PWD";
    String NODE_CERTIFICATE_DOWNLOAD = "NODE_CERTIFICATE_DOWNLOAD";
    String NODE_CERTIFICATE_UPLOAD = "NODE_CERTIFICATE_UPLOAD";
    String PRJ_JOB_DELETE = "PRJ_JOB_DELETE";
    String PRJ_JOB_CREATE = "PRJ_JOB_CREATE";
    String PRJ_JOB_CREATE_KUSCIA = "PRJ_JOB_CREATE_KUSCIA";
    String PRJ_JOB_AGREE = "PRJ_JOB_AGREE";
    String PRJ_JOB_PAUSE = "PRJ_JOB_PAUSE";
    String PRJ_JOB_PAUSE_KUSCIA = "PRJ_JOB_PAUSE_KUSCIA";
    String PRJ_JOB_CONTINUE = "PRJ_JOB_CONTINUE";
    String PRJ_JOB_CONTINUE_KUSCIA = "PRJ_JOB_CONTINUE_KUSCIA";
    String PRJ_JOB_REJECT = "PRJ_JOB_REJECT";
    String PRJ_JOB_RESULT_DOWNLOAD = "PRJ_JOB_RESULT_DOWNLOAD";
    String PRJ_EDGE_JOB_LIST = "PRJ_EDGE_JOB_LIST";
}

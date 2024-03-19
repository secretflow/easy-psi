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

-- the `id` is integer because of AUTOINCREMENT is only allowed on an INTEGER PRIMARY KEY.
-- default: id is varchar(64), name is varchar(256).

create table if not exists `project_job`
(
    id            integer primary key autoincrement,
    project_id    varchar(64) not null,
    job_id        varchar(64) not null,
    `name`        varchar(40) not null,                          -- Job name
    status        varchar(32) not null,                          -- Job status
    err_msg       text,                                          -- err_msg
    finished_time datetime   default null,                       -- finished_time
    is_deleted    tinyint(1) default '0' not null,               -- delete flag
    gmt_create    datetime   default CURRENT_TIMESTAMP not null, -- create time
    gmt_modified  datetime   default CURRENT_TIMESTAMP not null, -- modified time
    initiator_node_id varchar(64) not null,                      -- initiator node id
    partner_node_id   varchar(64) not null,                      -- partner node id
    host_node_id   varchar(64) not null,                         -- host node id
    description    varchar(64) default '',                       -- description
    start_time     DATETIME ,                                    -- start time
    initiator_config  TEXT default '',                           -- initiator config
    partner_config    TEXT default ''                            -- partner config
);
create unique index `upk_project_job_id` on project_job (`project_id`, `job_id`);
create unique index `upk_job_id` on project_job (`job_id`); -- Kuscia，Job unique

create table if not exists 'user_accounts'
(
    id            integer primary key autoincrement,
    name          varchar(128) not null,                         -- username
    password_hash varchar(128) not null,                         -- password_hash
    is_deleted    tinyint(1) default '0' not null,               -- delete flag
    gmt_create    datetime   default CURRENT_TIMESTAMP not null, -- create time
    gmt_modified  datetime   default CURRENT_TIMESTAMP not null, -- modified time
    failed_attempts integer  default null,                       -- failed attempts
    locked_invalid_time datetime default null,                   -- invalid time
    passwd_reset_failed_attempts  integer     default null,      -- passwd reset failed attempts
    gmt_passwd_reset_release       datetime   default null,      -- passwd reset invalid time
    owner_type    varchar(16) default 'CENTER' not null,         -- owner type
    owner_id      varchar(64) default 'kuscia-system'  not null, -- owner id
    `initial`     tinyint(1)  default '1' not null               -- initial
);

create table if not exists 'user_tokens'
(
    id           integer primary key autoincrement,
    name         varchar(128) not null,                          -- username
    token        varchar(64) default null,                       -- login token
    gmt_token    datetime    default null,                       -- token effective time
    is_deleted   tinyint(1)  default '0' not null,               -- delete flag
    gmt_create   datetime    default CURRENT_TIMESTAMP not null, -- create time
    gmt_modified datetime    default CURRENT_TIMESTAMP not null, -- modified time
    CONSTRAINT 'fk_name' FOREIGN KEY ('name') REFERENCES user_accounts ('name')
);
alter table user_tokens add column session_data text null;

--------------------------------------------------------------------------------------------------------------------------------

create table if not exists `node`
(
    id              integer primary key autoincrement,
    node_id         varchar(64)  not null,
    name            varchar(256) not null,
    auth            text,                                           -- ca
    description     text         default '',                        -- description
    control_node_id varchar(64)  not null,                          -- node control id
    net_address     varchar(100),                                   -- node net address
    is_deleted      tinyint(1)   default '0' not null,              -- delete flag
    gmt_create      datetime     default CURRENT_TIMESTAMP not null,-- create time
    gmt_modified    datetime     default CURRENT_TIMESTAMP not null,-- modified time
    cert_text       TEXT         default ''  not null,              -- cert text
    node_remark     varchar(256),                                   -- node remark
    trust           tinyint(1)  default '1' not null                -- trust
);
create unique index `upk_node_id` on node (`node_id`);
create index `key_node_name` on node (`name`);

create table if not exists `node_route`
(
    id              integer primary key autoincrement,
    src_node_id     varchar(64)  not null,
    dst_node_id     varchar(64)  not null,
    src_net_address varchar(100),                                  -- node net address
    dst_net_address varchar(100),                                  -- cooperate node net address
    is_deleted      tinyint(1) default '0' not null,               -- delete flag
    gmt_create      datetime   default CURRENT_TIMESTAMP not null, -- create time
    gmt_modified    datetime   default CURRENT_TIMESTAMP not null  -- modified time
);
create unique index `upk_route_src_dst` on node_route (`src_node_id`, `dst_node_id`);
create index `key_router_src` on node_route (`src_node_id`);
create index `key_router_dst` on node_route (`dst_node_id`);



CREATE table if not exists sys_resource (
    id            integer primary key autoincrement,
    resource_type  varchar(16) not null , -- comment 'INTERFACE|NODE'
    resource_code VARCHAR (64) not null UNIQUE, -- comment '{Code} or ALL'
    resource_name VARCHAR (64),
    is_deleted      tinyint(1)  default '0' not null,               -- delete flag
    gmt_create      datetime    default CURRENT_TIMESTAMP not null, -- create time
    gmt_modified    datetime    default CURRENT_TIMESTAMP not null -- modified time
);


CREATE table if not exists  sys_role (
    id            integer primary key autoincrement,
    role_code     VARCHAR (64) UNIQUE NOT NULL,
    role_name      VARCHAR (64),
    is_deleted      tinyint(1)  default '0' not null,               -- delete flag
    gmt_create      datetime    default CURRENT_TIMESTAMP not null, -- create time
    gmt_modified    datetime    default CURRENT_TIMESTAMP not null -- modified time
);


CREATE TABLE sys_role_resource_rel (
    id            integer primary key autoincrement,
    role_code     VARCHAR (64) NOT NULL,
    resource_code VARCHAR (64) NOT NULL,
    gmt_create      datetime    default CURRENT_TIMESTAMP not null, -- create time
    gmt_modified    datetime    default CURRENT_TIMESTAMP not null -- modified time
);
create unique INDEX `uniq_role_code_resource_code` on sys_role_resource_rel (`role_code`, `resource_code`);


CREATE TABLE sys_user_permission_rel (
    id            integer primary key autoincrement,
    user_type    VARCHAR (16)       NOT NULL,
    user_key     VARCHAR (64)      NOT NULL,
    target_type   VARCHAR (16) NOT NULL DEFAULT 'ROLE',
    target_code  VARCHAR (16) NOT NULL,
    gmt_create      datetime    default CURRENT_TIMESTAMP not null, -- create time
    gmt_modified    datetime    default CURRENT_TIMESTAMP not null -- modified time
);
create unique INDEX `uniq_user_key_target_code` on sys_user_permission_rel (`user_key`, `target_code`);

create table if not exists 'fabric_log'
(
    id             integer primary key autoincrement,
    log_path       varchar(1000) not null,                       -- log path
    log_hash       varchar(128) not null,                        -- log hash
    channel_name   varchar(128) not null,                        -- channel name
    chain_code_name varchar(128) not null,                        -- chaincode name
    msp_id         varchar(128) not null,                        -- msp id
    override_auth  varchar(128) not null,                        -- override auth
    owner  varchar(128) not null,                                -- owner
    result        tinyint(1) default '0' not null,               -- result
    message  varchar(500) not null,                              -- message
    is_deleted    tinyint(1) default '0' not null,               -- delete flag
    gmt_create    datetime   default CURRENT_TIMESTAMP not null, -- create time
    gmt_modified  datetime   default CURRENT_TIMESTAMP not null  -- modified time
);

create table if not exists 'rsa_encryption_key'
(
    id             integer primary key autoincrement,
    public_key     varchar(1000) not null,                       -- public key
    private_key    varchar(128) not null,                        -- private key
    key_invalid_time datetime default null,                      -- invalid time
    is_deleted    tinyint(1) default '0' not null,               -- delete flag
    gmt_create    datetime   default CURRENT_TIMESTAMP not null, -- create time
    gmt_modified  datetime   default CURRENT_TIMESTAMP not null  -- modified time
);

--------------------------------------------------------------------------------------------------------------------------------

-- resource
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'ALL_INTERFACE_RESOURCE','ALL_INTERFACE_RESOURCE');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'NODE_UPDATE','NODE_UPDATE');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'NODE_CREATE','NODE_CREATE');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'NODE_PAGE','NODE_PAGE');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'NODE_GET','NODE_GET');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'NODE_DELETE','NODE_DELETE');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'NODE_TOKEN','NODE_TOKEN');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'NODE_NEW_TOKEN','NODE_NEW_TOKEN');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'NODE_REFRESH','NODE_REFRESH');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'NODE_LIST','NODE_LIST');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'NODE_RESULT_LIST','NODE_RESULT_LIST');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'NODE_RESULT_DETAIL','NODE_RESULT_DETAIL');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'DATA_CREATE','DATA_CREATE');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'DATA_CREATE_DATA','DATA_CREATE_DATA');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'DATA_UPLOAD','DATA_UPLOAD');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'DATA_DOWNLOAD','DATA_DOWNLOAD');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'DATA_LIST_DATASOURCE','DATA_LIST_DATASOURCE');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'DATATABLE_LIST','DATATABLE_LIST');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'DATATABLE_GET','DATATABLE_GET');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'DATATABLE_DELETE','DATATABLE_DELETE');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'GRAPH_COMM_I18N','GRAPH_COMM_I18N');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'GRAPH_COMM_LIST','GRAPH_COMM_LIST');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'GRAPH_COMM_GET','GRAPH_COMM_GET');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'GRAPH_COMM_BATH','GRAPH_COMM_BATH');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'GRAPH_CREATE','GRAPH_CREATE');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'GRAPH_DELETE','GRAPH_DELETE');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'GRAPH_META_UPDATE','GRAPH_META_UPDATE');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'GRAPH_UPDATE','GRAPH_UPDATE');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'GRAPH_LIST','GRAPH_LIST');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'GRAPH_NODE_UPDATE','GRAPH_NODE_UPDATE');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'GRAPH_START','GRAPH_START');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'GRAPH_NODE_STATUS','GRAPH_NODE_STATUS');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'GRAPH_STOP','GRAPH_STOP');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'GRAPH_DETAIL','GRAPH_DETAIL');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'GRAPH_NODE_OUTPUT','GRAPH_NODE_OUTPUT');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'GRAPH_NODE_LOGS','GRAPH_NODE_LOGS');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'INDEX','INDEX');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'NODE_ROUTE_CREATE','NODE_ROUTE_CREATE');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'NODE_ROUTE_PAGE','NODE_ROUTE_PAGE');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'NODE_ROUTE_GET','NODE_ROUTE_GET');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'NODE_ROUTE_UPDATE','NODE_ROUTE_UPDATE');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'NODE_ROUTE_LIST_NODE','NODE_ROUTE_LIST_NODE');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'NODE_ROUTE_REFRESH','NODE_ROUTE_REFRESH');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'NODE_ROUTE_DELETE','NODE_ROUTE_DELETE');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'PRJ_CREATE','PRJ_CREATE');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'PRJ_LIST','PRJ_LIST');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'PRJ_GET','PRJ_GET');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'PRJ_UPDATE','PRJ_UPDATE');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'PRJ_DELETE','PRJ_DELETE');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'PRJ_ADD_INST','PRJ_ADD_INST');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'PRJ_ADD_NODE','PRJ_ADD_NODE');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'PRJ_ADD_TABLE','PRJ_ADD_TABLE');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'PRJ_DATATABLE_DELETE','PRJ_DATATABLE_DELETE');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'PRJ_DATATABLE_GET','PRJ_DATATABLE_GET');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'PRJ_JOB_LIST','PRJ_JOB_LIST');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'PRJ_JOB_GET','PRJ_JOB_GET');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'PRJ_JOB_STOP','PRJ_JOB_STOP');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'PRJ_TASK_LOGS','PRJ_TASK_LOGS');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'PRJ_TASK_OUTPUT','PRJ_TASK_OUTPUT');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'USER_CREATE','USER_CREATE');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'USER_GET','USER_GET');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'REMOTE_USER_RESET_PWD','REMOTE_USER_RESET_PWD');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'REMOTE_USER_CREATE','REMOTE_USER_CREATE');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'REMOTE_USER_LIST_BY_NODE','REMOTE_USER_LIST_BY_NODE');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'NODE_USER_RESET_PWD','NODE_USER_RESET_PWD');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'NODE_USER_CREATE','NODE_USER_CREATE');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'NODE_USER_LIST_BY_NODE','NODE_USER_LIST_BY_NODE');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'AUTH_LOGIN','AUTH_LOGIN');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'AUTH_LOGOUT','AUTH_LOGOUT');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'ENV_GET','ENV_GET');

insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'NODE_CERTIFICATE_DOWNLOAD','NODE_CERTIFICATE_DOWNLOAD');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'NODE_CERTIFICATE_UPLOAD','NODE_CERTIFICATE_UPLOAD');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'PRJ_JOB_DELETE','PRJ_JOB_DELETE');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'PRJ_JOB_STOP_KUSCIA','PRJ_JOB_STOP_KUSCIA');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'PRJ_JOB_CREATE_KUSCIA','PRJ_JOB_CREATE_KUSCIA');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'PRJ_JOB_PAUSE_KUSCIA','PRJ_JOB_PAUSE_KUSCIA');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'PRJ_JOB_CONTINUE_KUSCIA','PRJ_JOB_CONTINUE_KUSCIA');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'PRJ_EDGE_JOB_LIST','PRJ_EDGE_JOB_LIST');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'NODE_ROUTE_STATUS','NODE_ROUTE_STATUS');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'DATA_COUNT','DATA_COUNT');
insert into sys_resource(resource_type, resource_code, resource_name) values('INTERFACE', 'DATA_COUNT_KUSCIA','DATA_COUNT_KUSCIA');

-----------
--  role --
insert into sys_role(role_code, role_name) values('P2P_NODE', 'P2P 用户');

insert into sys_role_resource_rel(role_code, resource_code) values('P2P_NODE', 'PRJ_JOB_STOP');
insert into sys_role_resource_rel(role_code, resource_code) values('P2P_NODE', 'PRJ_JOB_GET');
insert into sys_role_resource_rel(role_code, resource_code) values('P2P_NODE', 'PRJ_JOB_CREATE_KUSCIA');
insert into sys_role_resource_rel(role_code, resource_code) values('P2P_NODE', 'PRJ_JOB_STOP_KUSCIA');
insert into sys_role_resource_rel(role_code, resource_code) values('P2P_NODE', 'PRJ_JOB_CONTINUE_KUSCIA');
insert into sys_role_resource_rel(role_code, resource_code) values('P2P_NODE', 'PRJ_JOB_PAUSE_KUSCIA');
insert into sys_role_resource_rel(role_code, resource_code) values('P2P_NODE', 'PRJ_EDGE_JOB_LIST');
insert into sys_role_resource_rel(role_code, resource_code) values('P2P_NODE', 'NODE_ROUTE_STATUS');
insert into sys_role_resource_rel(role_code, resource_code) values('P2P_NODE', 'DATA_COUNT_KUSCIA');

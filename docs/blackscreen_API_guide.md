# Easy PSI 黑屏 API 指南（仅适用于0.3.0及之后版本）

注：

- 使用黑屏操作psi任务时，需要先在宿主机上获取到后端的token
- 操作加密之前，默认服务器是安装openSSL的（linux默认安装）
   - openssl version # 查看openssl版本
- 常见的linxu版本安装openssl
   - # Red Hat、CentOS 或 Fedora 等基于 Red Hat 的发行版
      - sudo yum install openssl
   - # Debian、Ubuntu 或其他基于Debian的发行版
      - sudoapt-get update
      - sudoapt-getinstall openssl
## 获取token
方法一：

```
# 前置条件是部署好easypsi的容器,配置平台的端口到环境变量
export EASYPSI_PORT=8088
# 获取初始化密码
docker logs {CONTAINER ID} | grep pwd
. ezpsi_get_token.sh -w 'your password' -p 8088
# 脚本的-p参数为选填，如果环境变量中没有EASYPSI_PORT参数，则需要指定-p参数未端口号，若不填且环境变量中没有端口参数则默认使用8088端口

### 注：
# 密码需要用单括号包裹起来，否则存在特殊符号的话会导致加密错误
# 使用脚本时，会在当前目录生成一个public_key.pem文件，若已存在则会覆盖
# 使用sh ezpsi_get_token.sh的方式运行，否则可能会导致获取不到环境变量，需要手动添加一下token

# 如果访问成功，则最后则会打印这句话，其中包含token信息
User-Token: <token> ,token has been added to the environment variable

# 验证一下token是否添加成功
echo $EASYPSI_TOKEN
# 如果查询为空值，则需要手动添加一下token值
export EASYPSI_TOKEN= <token>
```
方法二：

```
# 前置条件是部署好easypsi的容器,配置平台的端口到环境变量
export EASYPSI_PORT=8088
# 获取公私钥
curl -k -X GET 'http://localhost:$EASYPSI_PORT/api/encryption/getRandomKey'

# 得到响应信息，data数据为公钥信息
{
  "status": {
    "code": 0,
    "msg": "操作成功"
  },
  "data": "..."
}

# 创建一个文件用来存储公钥信息
vi public_key.pem

-----BEGIN PUBLIC KEY-----
${公钥数据 MIIB......}
-----END PUBLIC KEY-----
# 保存退出

# 将公钥保存到环境变量中
export EASYPSI_PUBLIC_KEY={公钥数据}

# 将密码先进行sha256加密，然后在使用公钥进行RSA加密
export EASYPSI_PASSWORD_256=$(echo -n "{你的密码数据}" | openssl dgst -sha256 | awk '{print $2}')
### 注意 如果密码中含有$符号，则需要使用'\'进行转移 如："12#$qwER"->"12#\$qwER"

# 将公钥加密后的密码放到环境变量中
export EASYPSI_ENCRYPTED=$(echo -n $EASYPSI_PASSWORD_256 | openssl pkeyutl  -encrypt -pubin -inkey public_key.pem | base64 | tr -d '\n')

# 执行登录操作，获取token数据
curl -k -X POST 'http://localhost:$EASYPSI_PORT/api/login' \
--header 'Content-Type: application/json' \
-d '{
  "name": "admin",
  "passwordHash":"'"$EASYPSI_ENCRYPTED"'", 
  "publicKey":"'"$EASYPSI_PUBLIC_KEY"'"
}'

# 获取登录的响应信息
{
  "status": {
    "code": 0,
    "msg": "操作成功"
  },
  "data": {
    "token": "...",
    "name": "admin",
    "platformType": "P2P",
    "platformNodeId": "bob",
    "ownerType": "P2P",
    "ownerId": "bob",
    "interfaceResources": [],
    "virtualUserForNode": false,
    "noviceUser": false
  }
}

# 将token存入到环境变量中
export EASYPSI_TOKEN=<token>
```

```
#!/bin/bash
#
# Copyright 2024 Ant Group Co., Ltd.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
set -e

GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

usage() {
  echo "$(basename "$0") DEPLOY_MODE [OPTIONS]

 OPTIONS:

    -w              [optional]  Password of the account, required
    -p              [optional]  The port exposed by easypsi-edge, The port must NOT be occupied by other processes, default 8088
    "
}

PASSWORD=
easypsi_port=${EASYPSI_PORT}
while getopts 'w:p:' option; do
  case "$option" in
  w)
    PASSWORD=$OPTARG
    ;;
  p)
    easypsi_port=$OPTARG
    ;;
  :)
    printf "missing argument for -%s\n" "$OPTARG" >&2
    usage
    exit 1
    ;;
  \?)
    printf "illegal option: -%s\n" "$OPTARG" >&2
    usage
    exit 1
    ;;
  esac
done

shift $((OPTIND - 1))

function log() {
  local log_content=$1
  echo -e "${GREEN}${log_content}${NC}"
}

function error() {
  local log_content=$1
  echo -e "${RED}${log_content}${NC}"
}

if [[ ${PASSWORD} == "" ]]; then
    echo "-w parameter missing，Password cannot be empty"
    exit 1
fi

if [[ ${easypsi_port} == "" ]]; then
    easypsi_port="8088"
    log "No port specified, using default port 8088"
    export EASYPSI_PORT=$easypsi_port
fi

function public_key_check() {
  file_path="$1"
  if [ -f "$file_path" ]; then
    log "$file_path already exists. Do you want to overwrite it? (y/n)"
    read input
    if [ "$input" = "y" ]; then
        log "Overwriting the file..."
    else
        log "Aborting the overwrite. "
        exit
    fi
  else
    echo "$file_path does not exist."
  fi
}

# Message code validation
function response_code_check() {
  response="$1"
  response_status=$(echo $response | grep -o '"code": *[0-9]*' | awk -F ': *' '{print $2}')
  log "Response code is $response_status"
  if [[ $response_status == '0' ]]; then
      log "Response success！"
  elif [[ $response_status == '404' ]]; then
      error "Port Error! Please reconfigure the port"
      exit
  else
      error "Login feature request failed"
      exit
  fi
}

# Get the RSA public key
encryption_response=$(curl -k -X GET "http://localhost:$EASYPSI_PORT/api/encryption/getRandomKey")

log "Public key request return value："
echo $encryption_response
response_code_check $encryption_response

EASYPSI_PUBLIC_KEY=$(echo $encryption_response | grep -o '"data": *"[^"]*' | awk -F '": *"' '{print $2}')

log "Intercepted public key data："
echo $EASYPSI_PUBLIC_KEY

export EASYPSI_PASSWORD_256=$(echo -n ${PASSWORD} | openssl dgst -sha256 | awk '{print $2}')
log "password after sha256 encryption:"
echo ${EASYPSI_PASSWORD_256}

# Check if the file already exists
public_key_check 'public_key.pem'
echo "-----BEGIN PUBLIC KEY-----" > public_key.pem
echo "$EASYPSI_PUBLIC_KEY" >> public_key.pem
echo "-----END PUBLIC KEY-----" >> public_key.pem

log "Public key information:"
cat public_key.pem

export EASYPSI_ENCRYPTED=$(echo -n $EASYPSI_PASSWORD_256 | openssl pkeyutl  -encrypt -pubin -inkey public_key.pem | base64 | tr -d '\n')
log "Password after public key encryption:"
echo ${EASYPSI_ENCRYPTED}

# Login to obtain token
login_response=$(curl -k -X POST "http://localhost:$EASYPSI_PORT/api/login" \
--header 'Content-Type: application/json' \
-d '{
  "name": "admin",
  "passwordHash":"'"$EASYPSI_ENCRYPTED"'",
  "publicKey":"'"$EASYPSI_PUBLIC_KEY"'"
}')

log "login_response:$login_response"
response_code_check $login_response

token_value=$(echo $login_response | grep -o '"token": *"[^"]*' | awk -F '": *"' '{print $2}')
export EASYPSI_TOKEN=$token_value
if [[ -n $EASYPSI_TOKEN ]]; then
    log "User-Token: $EASYPSI_TOKEN ,token has been added to the environment variable!"
fi
```
## 修改密码
账户第一次登录后需要修改密码才能继续操作

```
# 依赖环境变量EASYPSI_PORT、EASYPSI_PUBLIC_KEY
sh ezpsi_update_password.sh -o '12#$qwER' -n '12#$dsvda' -p 8088
# 参数说明：
# 	-o 旧密码 -n 新密码 -p 端口号（可选，默认8088）
```
# 节点管理
## 查询本方节点信息
### 请求参数

```
curl -k -X POST "http://localhost:$EASYPSI_PORT/api/v1alpha1/node/get" \
--header "User-Token:$EASYPSI_TOKEN" \
--header 'Content-Type: application/json'
```
### 响应格式

```
{
    "status": {
        "code": "状态码",
        "msg": "状态信息"
    },
    "data": {
        "nodeId": "节点ID",
        "nodeName": "节点名称",
        "controlNodeId": "节点ID",
      	"description": "描述",
        "netAddress": "通讯地址",
        "nodeStatus": "节点状态",
        "gmtCreate": "2023-10-17T09:52:04+08:00",
        "gmtModified": "2023-10-17T09:52:04+08:00",
        "certText": "证书信息",
      	"nodeRemark": "节点别名"
    }
}
```
## 创建合作节点
### 请求参数
| **参数**            | **类型**      | **描述**                 |
|-------------------|-------------|------------------------|
| **certText**      | **String**  | **节点证书**               |
| **dstNetAddress** | **String**  | **合作方地址**              |
| **nodeRemark**    | **String**  | **节点名称备注（选填：默认空）**     |
| **trust**         | **boolean** | **是否信任节点（选填：默认false）** |


```
curl -k -X POST "http://localhost:$EASYPSI_PORT/api/v1alpha1/node/create" \
--header "User-Token:$EASYPSI_TOKEN" \
--header 'Content-Type: application/json' \
-d '{
    "certText": "...",
    "dstNetAddress": "127.0.0.1:8080",
  	"nodeRemark":"张三",
    "trust":true
}'
```
### 响应格式

```
{
    "status": {
        "code": "状态码",
        "msg": "状态信息"
    },
    "data": "创建节点Id"
}
```
## 查看合作节点
### 请求参数

```
curl -k -X POST "http://localhost:$EASYPSI_PORT/api/v1alpha1/nodeRoute/collaborationRoute" \
--header "User-Token:$EASYPSI_TOKEN" \
--header 'Content-Type: application/json'
```
### 响应格式

```
{
    "status": {
       "code": 0,
       "msg": "操作成功"
    },
    "data": {
        "list": [
            {
       		  	"routeId": "路由Id",
 				      "srcNodeId": "我方节点Id",
  			      "dstNodeId": "合作方节点Id",
              "srcNode": {
        					"nodeId": "节点ID",
        					"nodeName": "节点名称",
        					"controlNodeId": "节点ID",
      						"description": "描述",
        					"netAddress": "通讯地址",
        					"nodeStatus": "节点状态",
        					"gmtCreate": "2023-10-17T09:52:04+08:00",
        					"gmtModified": "2023-10-17T09:52:04+08:00",
        					"certText": "证书信息",
      						"nodeRemark": "节点别名"
                },
              "dstNode": {
        					"nodeId": "节点ID",
        					"nodeName": "节点名称",
        					"controlNodeId": "节点ID",
      						"description": "描述",
        					"netAddress": "通讯地址",
        					"nodeStatus": "节点状态",
        					"gmtCreate": "2023-10-17T09:52:04+08:00",
        					"gmtModified": "2023-10-17T09:52:04+08:00",
        					"certText": "证书信息",
      						"nodeRemark": "节点别名"
                },
    						"srcNetAddress": "我方地址",
     					  "dstNetAddress": "合作方地址",
     					 	"status": "路由状态",
    				  	"gmtCreate": "2023-10-17T09:52:04+08:00",
    				  	"gmtModified": "2023-10-17T09:52:04+08:00",
            }
        ],
        "total": 1
    }
}
```
## 修改合作节点
### 请求参数
| **参数**     | **类型**      | **描述**     |
|------------|-------------|------------|
| **nodeId** | **String**  | **节点Id**   |
| **trust**  | **boolean** | **是否信任节点** |


```
curl -k -X POST "http://localhost:$EASYPSI_PORT/api/v1alpha1/node/update" \
--header "User-Token:$EASYPSI_TOKEN" \
--header 'Content-Type: application/json' \
-d '{
    "nodeId": "alice",
    "trust":true
}'
```
### 响应格式

```
{
    "status": {
        "code": 0,
        "msg": "操作成功"
    },
    "data": {
        "nodeId": "alice",
        "nodeName": "alice",
        "controlNodeId": "alice",
        "description": null,
        "netAddress": "172.19.190.91:8080",
        "nodeStatus": null,
        "gmtCreate": "2024-03-11T09:05:42+08:00",
        "gmtModified": "2024-03-11T09:05:42+08:00",
        "certText": "...",
        "nodeRemark": null,
        "trust": true
    }
}
```
## 删除合作节点
### 请求参数
| **参数**       | **类型**     | **描述**   |
|--------------|------------|----------|
| **routerId** | **String** | **路由Id** |


```
curl -k -X POST "http://localhost:$EASYPSI_PORT/api/v1alpha1/node/delete" \
--header "User-Token:$EASYPSI_TOKEN" \
--header 'Content-Type: application/json' \
-d '{
  "routerId": 3
}'
```
**响应格式**

```
{
  "status":
  	{
      "code":0,
      "msg":"操作成功"
    },
  "data":null
}
```
# 任务管理
## 创建任务接口
### 请求参数说明
| **参数**                                                            | **类型**                                                | **描述**                                                                                                                                                                                                                                                        |
|-------------------------------------------------------------------|-------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **name**                                                          | **String**                                            | **任务名称**                                                                                                                                                                                                                                                      |
| **description**                                                   | **String**                                            | **描述（选填：默认空）**                                                                                                                                                                                                                                                |
| **initiatorConfig**                                               | **PsiConfig**                                         | **发起方参数**                                                                                                                                                                                                                                                     |
| **partnerConfig**                                                 | **PsiConfig**                                         | **合作方参数**                                                                                                                                                                                                                                                     |
| **outputConfig**                                                  | **PsiConfig**                                         | **结果参数**                                                                                                                                                                                                                                                      |
| **advancedConfig**                                                | **AdvancedConfig**                                    | **高级配置**                                                                                                                                                                                                                                                      |
| **PsiConfig.nodeId**                                              | **String**                                            | **节点id**                                                                                                                                                                                                                                                      |
| **PsiConfig.path**                                                | **String**                                            | **求交表名**                                                                                                                                                                                                                                                      |
| **PsiConfig.keys**                                                | **List<String>**                                      | **表关联健**                                                                                                                                                                                                                                                      |
| **PsiConfig.broadcastResult**                                     | **List<String>**                                      | **结果获取方**                                                                                                                                                                                                                                                     |
| **AdvancedConfig.protocolConfig**                                 | **AdvancedConfig.ProtocolConfig**                     | **协议配置**                                                                                                                                                                                                                                                      |
| **AdvancedConfig.linkConfig**                                     | **String**                                            | **超时时间**                                                                                                                                                                                                                                                      |
| **AdvancedConfig.skipDuplicatesCheck**                            | **boolean**                                           | **重复项检查**<br />**(开启重复项检查传true，反之传false)**                                                                                                                                                                                                                    |
| **AdvancedConfig.disableAlignment**                               | **boolean**                                           | **重排序**<br />**(开启重排序传true，反之传false)**                                                                                                                                                                                                                        |
| **AdvancedConfig.recoveryEnabled**                                | **boolean**                                           | **断点续传**                                                                                                                                                                                                                                                      |
| **AdvancedConfig.advabcedJoinType**                               | **String**                                            | **求交方式（填写参数：**<br />**ADVANCED_JOIN_TYPE_UNSPECIFIED,**<br />**ADVANCED_JOIN_TYPE_INNER_JOIN,**<br />**ADVANCED_JOIN_TYPE_LEFT_JOIN,**<br />**ADVANCED_JOIN_TYPE_RIGHT_JOIN,**<br />**ADVANCED_JOIN_TYPE_FULL_JOIN,**<br />**ADVANCED_JOIN_TYPE_DIFFERENCE）** |
| **AdvancedConfig.leftSide**                                       | **String**                                            | **求交左方**                                                                                                                                                                                                                                                      |
| **AdvancedConfig.ProtocolConfig.Protocol**                        | **String**                                            | **协议（填写参数：**<br />**PROTOCOL_ECDH,**<br />**PROTOCOL_KKRT,**<br />**PROTOCOL_RR22）**                                                                                                                                                                          |
| **AdvancedConfig.ProtocolConfig.Protocol.ecdhConfig**             | **AdvancedConfig.ProtocolConfig.Protocol.EcdhConfig** | **ecdh协议配置**                                                                                                                                                                                                                                                  |
| **AdvancedConfig.ProtocolConfig.Protocol.kkrtConfig**             | **AdvancedConfig.ProtocolConfig.Protocol.KkrtConfig** | **kkrt协议配置**                                                                                                                                                                                                                                                  |
| **AdvancedConfig.ProtocolConfig.Protocol.rr22Config**             | **AdvancedConfig.ProtocolConfig.Protocol.Rr22Config** | **rr22协议配置**                                                                                                                                                                                                                                                  |
| **AdvancedConfig.ProtocolConfig.Protocol.EcdhConfig.curve**       | **String**                                            | **ecdh曲线类型（填写参数：**<br />**CURVE_25519,**<br />**CURVE_FOURQ,**<br />**CURVE_SM2,**<br />**CURVE_SECP256K1,**<br />**CURVE_25519_ELLIGATOR2）**<br />**（默认：CURVE_FOURQ）**                                                                                       |
| **AdvancedConfig.ProtocolConfig.Protocol.kkrtConfig.bucketSize**  | **Stirng**                                            | **kkrt桶大小**<br />**（默认：1048576）**                                                                                                                                                                                                                             |
| **AdvancedConfig.ProtocolConfig.Protocol.rr22Config.bucketSize**  | **Stirng**                                            | **rr22桶大小**<br />**（默认：1048576）**                                                                                                                                                                                                                             |
| **AdvancedConfig.ProtocolConfig.Protocol.rr22Config.lowCommMode** | **boolean**                                           | **低通信模式**<br />**（默认：false）**                                                                                                                                                                                                                                 |


```
curl -k -X POST "http://localhost:$EASYPSI_PORT/api/v1alpha1/project/job/create" \
--header "User-Token:$EASYPSI_TOKEN" \
--header 'Content-Type: application/json' \
-d '{
  "name": "RR22",
  "initiatorConfig": {
    "nodeId": "psi1",
    "path": "alice.csv",
    "keys": [
      "id1"
    ]
  },
  "partnerConfig": {
    "nodeId": "psi2",
    "path": "bob.csv",
    "keys": [
      "id2"
    ]
  },
  "outputConfig": {
    "path": "aa.csv",
    "broadcastResult": [
      "psi1",
      "psi2"
    ]
  },
  "advancedConfig": {
    "protocolConfig": {
      "protocol": "PROTOCOL_ECDH",
      "ecdhConfig": {
        "curve": "CURVE_25519"
      }
    },
    "advancedJoinType": "ADVANCED_JOIN_TYPE_LEFT_JOIN",
    "leftSide": "bob",
    "recoveryEnabled": true,
    "skipDuplicatesCheck": false,
    "disableAlignment": true,
    "linkConfig": "30"
  }
}'
```
### 响应格式

```
{
    "status": {
        "code": 0,
        "msg": "操作成功"
    },
    "data": {
        "jobId": "任务id",
        "name": "任务名称"
    }
}
```
## 任务同意接口
### 请求参数
| **参数**    | **类型**     | **描述**   |
|-----------|------------|----------|
| **jobId** | **String** | **任务ID** |


```
curl -k -X POST "http://localhost:$EASYPSI_PORT/api/v1alpha1/project/job/agree" \
--header "User-Token:$EASYPSI_TOKEN" \
--header 'Content-Type: application/json' \
-d '{
    "jobId":"fzkshvif"
}'
```
### 响应格式
```
{
    "status": {
        "code": 0,
        "msg": "操作成功"
    },
    "data": {}
}
```
## 任务拒绝接口
### 请求参数
| **参数**        | **类型**     | **描述**   |
|---------------|------------|----------|
| **jobId**     | **String** | **任务ID** |
| **rejectMsg** | **String** | **拒绝理由** |


```
curl -k -X POST "http://localhost:$EASYPSI_PORT/api/v1alpha1/project/job/reject" \
--header "User-Token:$EASYPSI_TOKEN" \
--header 'Content-Type: application/json' \
-d '{
    "jobId":"fzkshvif",
    "rejectMsg": "取消操作"
}
```
### 响应格式

```
{
    "status": {
        "code": 0,
        "msg": "操作成功"
    },
    "data": {}
}
```
## 查看全部任务接口
### 请求参数
| **参数**           | **类型**                    | **描述**                                                                                                                                                                                |
|------------------|---------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **statusFilter** | **List<GraphJobStatus>�** | **状态("string (填写参数：PENDING_CERT &#124; PENDING_REVIEW &#124; RUNNING &#124; PAUSED &#124; TIMEOUT &#124; CANCELED &#124; REJECTED &#124; SUCCEEDED &#124; FAILED &#124; )")（选填：默认空）** |
| **search**       | **String**                | **任务名/节点id（选填：默认空）**                                                                                                                                                                  |
| **sortKey**      | **String**                | **排序值**                                                                                                                                                                               |
| **sortType**     | **String**                | **排序类型**                                                                                                                                                                              |
| **pageNum**      | **Integer**               | **页数**                                                                                                                                                                                |
| **pageSize**     | **Integer�**              | **页数大小**                                                                                                                                                                              |


```
curl -k -X POST "http://localhost:$EASYPSI_PORT/api/v1alpha1/project/job/list/black_screen" \
--header "User-Token:$EASYPSI_TOKEN" \
--header 'Content-Type: application/json' \
-d '{
 		"pageNum": 1,
		"pageSize": 10,
		"sortType": "DESC"
}'
```
### 响应格式

```
{
    "status": {
        "code": 0,
        "msg": "操作成功"
    },
    "data": {
        "list": [
            {
                "jobId": "任务id",
                "status": "任务状态",
                "errMsg": "错误信息",
                "gmtCreate": "创建时间",
                "gmtFinished": "完成时间",
                "name": "任务名称",
                "srcNodeId": "发起方节点id",
                "dstNodeId": "合作方节点id",
                "operation": [
                    "操作状态"
                ] 
          }
        ],
        "total": 1
    }
}
```
## 查看某个任务接口
### 请求参数
| **参数**    | **类型**     | **描述**   |
|-----------|------------|----------|
| **jobId** | **String** | **任务ID** |


```
curl -k -X POST "http://localhost:$EASYPSI_PORT/api/v1alpha1/project/job/get" \
--header "User-Token:$EASYPSI_TOKEN" \
--header 'Content-Type: application/json' \
-d '{
    "jobId":"ahmrnsbl"
}'
```
### 响应格式

```
{
    "status": {
        "code": 0,
        "msg": "操作成功"
    },
    "data": {
        "jobId": "任务id",
        "status": "任务状态",
        "errMsg": "错误信息",
        "gmtCreate": "创建时间",
        "gmtModified": "修改时间",
        "gmtFinished": "完成时间",
        "name": "名称",
        "description": "描述信息",
        "initiatorConfig": {
            "nodeId": "节点id",
            "protocolConfig": {
                "protocol": "协议",
                "role": "角色",
                "broadcastResult": "是否为结果方",
                "ecdhConfig": {
                    "curve": "协议配置"
                },
            },
            "inputConfig": {
                "type": "表类型",
                "path": "表名"
            },
            "outputConfig": {
                "type": "表类型",
                "path": "结果表"
            },
            "linkConfig": {
                "recvTimeoutMs": "超时时间",
                "httpTimeoutMs": "超时时间"
            },
            "keys": [
                "关联健"
            ],
            "skipDuplicatesCheck": "重复值检查",
            "disableAlignment": "重排序",
            "recoveryConfig": {
                "enabled": "断线续传",
                "folder": "快照路径"
            },
            "advancedJoinType": "求交类型",
            "leftSide": "求交左方"
        },
        "partnerConfig": {
            "nodeId": "节点id",
            "protocolConfig": {
                "protocol": "协议",
                "role": "角色",
                "broadcastResult": "是否为结果方",
                "ecdhConfig": {
                    "curve": "协议配置"
                },
            },
            "inputConfig": {
                "type": "表类型",
                "path": "表名"
            },
            "outputConfig": {
                "type": "表类型",
                "path": "结果表"
            },
            "linkConfig": {
                "recvTimeoutMs": "超时时间",
                "httpTimeoutMs": "超时时间"
            },
            "keys": [
                "关联健"
            ],
            "skipDuplicatesCheck": "重复值检查",
            "disableAlignment": "重排序",
            "recoveryConfig": {
                "enabled": "断线续传",
                "folder": "快照路径"
            },
            "advancedJoinType": "求交类型",
            "leftSide": "求交左方"
        },
        "startTime": "开始时间",
        "operation": [
            "操作按钮"
        ]
    }
}
```
## 取消任务接口
### 请求参数
| **参数**    | **类型**     | **描述**   |
|-----------|------------|----------|
| **jobId** | **String** | **任务ID** |


```
curl -k -X POST "http://localhost:$EASYPSI_PORT/api/v1alpha1/project/job/stop" \
--header "User-Token:$EASYPSI_TOKEN" \
--header 'Content-Type: application/json' \
-d '{
    "jobId":"fzkshvif"
}'
```
### 响应格式

```
{
    "status": {
        "code": 0,
        "msg": "操作成功"
    },
    "data": {}
}
```
## 暂停任务接口
### 请求参数
| **参数**    | **类型**     | **描述**   |
|-----------|------------|----------|
| **jobId** | **String** | **任务ID** |


```
curl -k -X POST "http://localhost:$EASYPSI_PORT/api/v1alpha1/project/job/pause" \
--header "User-Token:$EASYPSI_TOKEN" \
--header 'Content-Type: application/json' \
-d '{
    "jobId":"fzkshvif"
}'
```
**响应格式**

```
{
  "status": {
    "code": 0,
    "msg": "操作成功"
  },
  "data": null
}
```
## 删除任务接口
### 请求参数
| **参数**    | **类型**     | **描述**   |
|-----------|------------|----------|
| **jobId** | **String** | **任务ID** |


```
curl -k -X POST "http://localhost:$EASYPSI_PORT/api/v1alpha1/project/job/delete" \
--header "User-Token:$EASYPSI_TOKEN" \
--header 'Content-Type: application/json' \
-d '{
    "jobId":"fzkshvif"
}'
```
### 响应格式
```
{
    "status": {
        "code": 0,
        "msg": "操作成功"
    },
    "data": {}
}
```
## 继续任务接口
### 请求参数
| **参数**    | **类型**     | **描述**   |
|-----------|------------|----------|
| **jobId** | **String** | **任务ID** |


```
curl -k -X POST "http://localhost:$EASYPSI_PORT/api/v1alpha1/project/job/continue" \
--header "User-Token:$EASYPSI_TOKEN" \
--header 'Content-Type: application/json' \
-d '{
    "jobId":"fzkshvif"
}'
```
### 响应格式

```
{
    "status": {
        "code": 0,
        "msg": "操作成功"
    },
    "data": {}
}
```

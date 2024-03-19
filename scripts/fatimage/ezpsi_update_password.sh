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
    -o              [optional]  old password
    -n              [optional]  new password
    -p              [optional]  The port exposed by easypsi-edge, The port must NOT be occupied by other processes, default 8088
    "
}

old_password=
new_password=
easypsi_port=$EASYPSI_PORT

while getopts 'o:n:p:' option; do
  case "$option" in
  o)
    old_password=$OPTARG
    ;;
  n)
    new_password=$OPTARG
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

if [[ ${old_password} == "" ]]; then
    error "-o parameter missing，old password cannot be empty"
    return 1
fi

if [[ ${new_password} == "" ]]; then
    error "-n parameter missing，new password cannot be empty"
    return 1
fi

if [[ $EASYPSI_TOKEN == "" ]]; then
    error "token is empty, please log in first"
    return 1
fi

if [[ ${ip} == "" ]]; then
    ip="localhost"
    log "default ip address is localhost"
fi

if [[ ${easypsi_port} == "" ]]; then
    easypsi_port="8088"
    log "No port specified, using default port 8088"
fi

EASYPSI_PORT=$easypsi_port

function public_key_check() {
  file_path="$1"
  if [ -f "$file_path" ]; then
    log "Use $file_path file for password encryption"
  else
    error "$file_path does not exist."
    return 1
  fi
}

# Message code validation
function response_code_check() {
  response="$1"
  if [ -z "$response" ]; then
    error "response is null"
    return 1
  fi
  response_status=$(echo $response | grep -o '"code": *[0-9]*' | awk -F ': *' '{print $2}')
  log "Response code is $response_status"
  if [[ $response_status == '0' ]]; then
      log "Response success！"
  elif [[ $response_status == '404' ]]; then
      error "Port Error! Please reconfigure the port"
  else
      error "Update password request failed"
  fi
}

# check whether the public key exists
public_key_check 'public_key.pem'

# Password Encryption
function password_encryption() {
   password="$1"
   password_256=$(echo -n ${password} | openssl dgst -sha256 | awk '{print $2}')
   password_public_key=$(echo -n $password_256 | openssl pkeyutl  -encrypt -pubin -inkey public_key.pem | base64 | tr -d '\n')
   echo "$password_public_key"
}

log "old_password before encryption: ${old_password}"
OLD_PASSWORD=$(password_encryption ${old_password})
log "old_password after public key encryption: $OLD_PASSWORD"

log "new_password before encryption: ${new_password}"
NEW_PASSWORD=$(password_encryption ${new_password})
log "new_password after public key encryption: $NEW_PASSWORD"

CONFIRM_PASSWORD=$NEW_PASSWORD

update_pwd_response=$(curl -k -X POST "http://${ip}:${easypsi_port}/api/v1alpha1/user/updatePwd" \
--header 'Content-Type: application/json' \
--header "User-Token:$EASYPSI_TOKEN" \
-d '{
  "name": "admin",
  "oldPasswordHash":"'"$OLD_PASSWORD"'",
  "newPasswordHash":"'"$NEW_PASSWORD"'",
  "confirmPasswordHash":"'"$CONFIRM_PASSWORD"'",
  "publicKey":"'"$EASYPSI_PUBLIC_KEY"'"
}')

log "modify password response:"
echo $update_pwd_response
response_code_check $update_pwd_response

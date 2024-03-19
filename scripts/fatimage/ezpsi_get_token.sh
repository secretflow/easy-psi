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
ip=

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
    return 1
fi

if [[ ${easypsi_port} == "" ]]; then
    easypsi_port="8088"
    log "No port specified, using default port 8088"
fi

if [[ ${ip} == "" ]]; then
    ip="localhost"
    log "default ip address is localhost"
fi

export EASYPSI_PORT=$easypsi_port

function public_key_check() {
  file_path="$1"
  if [ -f "$file_path" ]; then
    log "$file_path already exists. Do you want to overwrite it? (y/n)"
    read input
    if [ "$input" = "y" ]; then
        log "Overwriting the file..."
    else
        log "Aborting the overwrite. "
    fi
  else
    echo "$file_path does not exist."
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
      return 1
  else
      error "Login feature request failed"
      return 1
  fi
}

# Get the RSA public key
encryption_response=$(curl -k -X GET "http://$ip:$EASYPSI_PORT/api/encryption/getRandomKey")

log "Public key request return value："
echo $encryption_response
response_code_check $encryption_response

EASYPSI_PUBLIC_KEY=$(echo $encryption_response | grep -o '"data": *"[^"]*' | awk -F '": *"' '{print $2}')

log "Intercepted public key data："
echo $EASYPSI_PUBLIC_KEY
export EASYPSI_PUBLIC_KEY=$EASYPSI_PUBLIC_KEY

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
login_response=$(curl -k -X POST "http://$ip:$EASYPSI_PORT/api/login" \
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

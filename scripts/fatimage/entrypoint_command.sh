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
NC='\033[0m'

function log() {
  local log_content=$1
  echo -e "${GREEN}${log_content}${NC}"
}

log "KUSCIA_PROTOCOL=${KUSCIA_PROTOCOL}"
KUSCIA_WORKDIR=/home/kuscia
PAD_WORKDIR=/app
CTR_CERT_ROOT=${KUSCIA_WORKDIR}/var/certs
EASYPSI_PASSWORD=


 # random uppercase letters
function get_uppercase_letter() {
    letters="ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    echo -n ${letters:$(( RANDOM % ${#letters} )):1}
}

 # random lowercase letters
function get_lowercase_letter() {
    letters="abcdefghijklmnopqrstuvwxyz"
    echo -n ${letters:$(( RANDOM % ${#letters} )):1}
}

# random numbers
function get_number() {
    numbers="0123456789"
    echo -n ${numbers:$(( RANDOM % ${#numbers} )):1}
}

# random special characters
get_special_character() {
    characters="!@#$%^&*()_+-=:;<>?,."
    echo -n ${characters:$(( RANDOM % ${#characters} )):1}
}

# random fixed length password
function generate_password() {
     length=$1
     random_password=""
    while [ ${#random_password} -lt $length ]; do
        random_password+="$(get_uppercase_letter)"
        random_password+="$(get_lowercase_letter)"
        random_password+="$(get_number)"
        random_password+="$(get_special_character)"
    done
    echo "${random_password:0:$length}"
}

EASYPSI_PASSWORD=$(generate_password 10)

function do_http_probe() {
  local endpoint=$1
  local max_retry=$2
  local retry=0
  while [ $retry -lt $max_retry ]; do
    local status_code
    # TODO support MTLS
    status_code=$(curl -k --write-out '%{http_code}' --silent --output /dev/null ${endpoint})
    if [[ $status_code -eq 200 || $status_code -eq 404 || $status_code -eq 401 ]]; then
      return 0
    fi
    retry=$((retry + 1))
    log "check kuscia status: ${status_code}. times: ${retry}"
    sleep 2
  done

  return 1
}

function probe_kuscia() {
  if ! do_http_probe "http://127.0.0.1:80" 60; then
    echo "[Error] Probe kuscia is not running. Please check the log" >&2
    exit 1
  fi
}


function start_kuscia() {
    pushd ${KUSCIA_WORKDIR}
    log "start kuscia..."
    bin/kuscia start -c etc/conf/kuscia.yaml &
    log "check kuscia status..."
    probe_kuscia
    popd
}


function create_secretflow_app_image() {
  local image_repo=$SECRETFLOW_IMAGE
  local image_tag=latest

  if [[ "${SECRETFLOW_IMAGE}" == *":"* ]]; then
    image_repo=${SECRETFLOW_IMAGE%%:*}
    image_tag=${SECRETFLOW_IMAGE##*:}
  fi

  app_type=$(echo "${image_repo}" | awk -F'/' '{print $NF}' | awk -F'-' '{print $1}')
  if [[ ${app_type} == "" ]]; then
    app_type="secretflow"
  fi

  scripts/deploy/create_sf_app_image.sh "${image_repo}" "${image_tag}" "${app_type}" "${SF_IMAGE_ID}"
  log "Create secretflow app image done"
}

function copy_kuscia_api_client_certs() {
  # notls
  mkdir -p ${PAD_WORKDIR}/config/certs
  if [[ ${KUSCIA_PROTOCOL}  != "notls" ]]; then
      cp ${CTR_CERT_ROOT}/ca.crt ${PAD_WORKDIR}/config/certs/ca.crt
        # mtls
        if [[ ${KUSCIA_PROTOCOL}   -eq "mtls" ]]; then
            cp ${CTR_CERT_ROOT}/kusciaapi-client.crt ${PAD_WORKDIR}/config/certs/client.crt
            cp ${CTR_CERT_ROOT}/kusciaapi-client.key ${PAD_WORKDIR}/config/certs/client.pem
        fi
  fi
  cp ${CTR_CERT_ROOT}/token ${PAD_WORKDIR}/config/certs/token
  log "copy kuscia api client certs to web server container done"
}

function pre_kuscia() {
    log "init kuscia.yaml"
    DOMAIN_KEY_DATA=$(openssl genrsa 2048 | base64 | tr -d "\n")
    CONFIG_DATA=$(sed -e "s!{{.DOMAIN_ID}}!${NODE_ID}!g;s!{{.DOMAIN_KEY_DATA}}!${DOMAIN_KEY_DATA}!g" <"/app/scripts/template/kuscia-autonomy-template.yaml")
    echo "${CONFIG_DATA}" > etc/conf/kuscia.yaml
}
function post_kuscia() {
    log "1. load secretflow image..."
    kuscia image load -i image_libs/secretflow.tar --store /home/kuscia/var/images
    log "2. create_secretflow_app_image..."
    create_secretflow_app_image
    log "3. gen kuscia api client certs..."
    sh scripts/deploy/init_kusciaapi_client_certs.sh
    log "4. creat secretpad svc ..."
    sh scripts/deploy/create_secretpad_svc.sh 127.0.0.1 ${NODE_ID}
}
function copy_easypsi_config() {
    cp -r /app/bak/config /app
    cp -r /app/bak/scripts /app
    cp -r /app/bak/data /app
    cp  /app/bak/scripts/fatimage/ezpsi_get_token.sh /app/tmp/scripts
    cp  /app/bak/scripts/fatimage/ezpsi_update_password.sh /app/tmp/scripts
}

function generate_easypsi_serverkey() {
  /app/scripts/gen_easypsi_serverkey.sh 'easypsi' /app/config

  log "generate webserver server key done"
}
function init_easypsi_db() {
  /app/scripts/update-sql.sh
  log "initialize  webserver database done"
}
function create_easypsi_user_password() {
  /app/scripts/register_account.sh -n 'admin' -p "${EASYPSI_PASSWORD}" -t P2P -o "${NODE_ID}"
  log "create webserver user and password done"
}

function prepare_pad() {
    pushd ${KUSCIA_WORKDIR}
    log "1. generate easypsi serverkey..."
    generate_easypsi_serverkey
    log "2. init easypsi db..."
    init_easypsi_db
    log "3. create easypsi user password..."
    create_easypsi_user_password
    log "4. copy kuscia api client certs..."
    copy_kuscia_api_client_certs

    popd
}

function start_pad() {
    pushd ${PAD_WORKDIR}
    java -jar -Dsun.net.http.allowRestrictedHeaders=true /app/easypsi.jar
    popd
}

log ">> copy easypsi config..."
copy_easypsi_config
log ">> pre kuscia..."
pre_kuscia
log ">> start kuscia..."
start_kuscia
log ">> post kuscia..."
post_kuscia
log ">> pre pad..."
prepare_pad
log ">> start pad..."
start_pad
log ">> finish start"
log "would use default password: ${EASYPSI_PASSWORD}"
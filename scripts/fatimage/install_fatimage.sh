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

p2p_default_dir="$HOME/kuscia/p2p"

set -e
GREEN='\033[0;32m'
NC='\033[0m'


usage() {
  echo "$(basename "$0") DEPLOY_MODE [OPTIONS]

  OPTIONS:
    -n              [mandatory] Domain id to be deployed.
    -h              [optional]  Show this help text.
    -p              [optional]  The port exposed by kuscia-lite-gateway, The port must NOT be occupied by other processes, default 8080
    -s              [optional]  The port exposed by easypsi-edge, The port must NOT be occupied by other processes, default 8088
    -d              [optional]  The install directory. Default is ${p2p_default_dir}.

example:
    install.sh -n alice -d /root/tm/t4 -p 7001 -s 7002'
    "
}

function log() {
  local log_content=$1
  echo -e "\033[36m[$(date +%y/%m/%d-%H:%M:%S)] \033[0m${GREEN}${log_content}${NC}"
}

while getopts ':n:d:p:s:t:' option; do
  case "$option" in
  n)
    NODE_ID=$OPTARG
    ;;
  d)
    ROOT_PATH=$OPTARG
    ;;
  p)
    KUSCIA_DOMAIN_PORT=$OPTARG
    ;;
  s)
    WEB_PORT=$OPTARG
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

for file in images/*; do
	if [ -f "$file" ]; then
		echo "$file"
		imageInfo="$(docker load <$file)"
		echo "echo ${imageInfo}"
		someimage=$(echo ${imageInfo} | sed "s/Loaded image: //")
		if [[ $someimage == *easypsi* ]]; then
			EASYPSI_FAT_IMAGE=$someimage
    fi
	fi
done

#### Fill default data ####
#if [ "${EASYPSI_FAT_IMAGE}" == "" ]; then
#  EASYPSI_FAT_IMAGE=${default_fat_image}
#fi

if [[ ${KUSCIA_DOMAIN_PORT} == "" ]]; then
  KUSCIA_DOMAIN_PORT="8080"
fi
if [[ ${WEB_PORT} == "" ]]; then
  WEB_PORT="8088"
fi
if [[ $USER == "" ]]; then
  USER=default
fi
if [[ ${KUSCIA_PROTOCOL} == "" ]]; then
  KUSCIA_PROTOCOL="mtls"
fi
if [[ $ROOT_PATH == "" ]]; then
  ROOT_PATH=${p2p_default_dir}
fi

#### Verify ####
if [[ ${EASYPSI_FAT_IMAGE} == "" ]]; then
  printf "empty EASYPSI_FAT_IMAGE\n" >&2
  exit 1
fi
if [[ ${NODE_ID} == "" ]]; then
  printf "empty node id\n" >&2
  exit 1
fi

#### prepare
mkdir -p $ROOT_PATH
CTR_PREFIX=${USER}-kuscia
CTR_CERT_ROOT=${ROOT_PATH}/var/certs
FORCE_START=false
# set by account_settings function
EASYPSI_USER_NAME=""
EASYPSI_PASSWORD=""
SPRING_PROFILES_ACTIVE="p2p"
LITE_MEMORY_LIMIT=4G
EASYPSI_IMAGE=${EASYPSI_FAT_IMAGE}
log "EASYPSI_FAT_IMAGE=${EASYPSI_FAT_IMAGE}"
log "easypsi root dir: $ROOT_PATH"


function need_start_docker_container() {
  ctr=$1

  if [[ ! "$(docker ps -a -q -f name=^/${ctr}$)" ]]; then
    # need start your container
    return 0
  fi

  if $FORCE_START; then
    log "Remove container '${ctr}' ..."
    docker rm -f $ctr >/dev/null 2>&1
    # need start your container
    return 0
  fi

  read -rp "$(echo -e ${GREEN}The container \'${ctr}\' already exists. Do you need to recreate it? [y/n]: ${NC})" yn
  case $yn in
  [Yy]*)
    echo -e "${GREEN}Remove container ${ctr} ...${NC}"
    docker rm -f $ctr
    # need start your container
    return 0
    ;;
  *)
    return 1
    ;;
  esac

  return 1
}

function copy_easypsi_file_to_volume() {
  local dst_path=$1
  mkdir -p ${dst_path}/easypsi
  mkdir -p ${dst_path}/data
  # copy config file
  docker run --rm --entrypoint /bin/bash -v ${dst_path}/easypsi:/tmp/easypsi $EASYPSI_IMAGE -c 'cp -R /app/config /tmp/easypsi/'
  # copy sqlite db file
  docker run --rm --entrypoint /bin/bash -v ${dst_path}/easypsi:/tmp/easypsi $EASYPSI_IMAGE -c 'cp -R /app/db /tmp/easypsi/'
  # copy demo data file
  docker run --rm --entrypoint /bin/bash -v ${dst_path}:/tmp/easypsi $EASYPSI_IMAGE -c 'cp -R /app/data /tmp/easypsi/'
  log "copy webserver config and database file done"
}

function generate_easypsi_serverkey() {
  local tmp_volume=$1
  local password=$2
  # generate server key in edge container
  docker run -it --rm --entrypoint /bin/bash --volume=${tmp_volume}/easypsi/config/:/tmp/temp ${EASYPSI_IMAGE} -c "/app/scripts/gen_easypsi_serverkey.sh ${password} /tmp/temp"
  rm -rf ${tmp_volume}/server.jks
  log "generate webserver server key done"
}

function init_easypsi_db() {
  local root_path=$1
  # generate server key in edge container
  docker run -it --rm --entrypoint /bin/bash --volume=${root_path}/easypsi/db:/app/db ${EASYPSI_IMAGE} -c "/app/scripts/update-sql.sh"
  log "initialize  webserver database done"
}

function create_easypsi_user_password() {
  local root_path=$1
  local user_name=$2
  local password=$3
  # generate server key in edge container
  docker run -it --rm --entrypoint /bin/bash --volume=${root_path}/easypsi/db:/app/db ${EASYPSI_IMAGE} -c "/app/scripts/register_account.sh -n '${user_name}' -p '${password}' -t P2P -o '${NODE_ID}'"

  log "create webserver user and password done"
}


function render_easypsi_config() {
  local root_path=$1
  local tmpl_path=${root_path}/easypsi/config/template/application.yaml.tmpl
  local store_key_password=$2
  # cp file to easypsi's config path
  docker run -d --rm --name ${CTR_PREFIX}-dummy --volume=${root_path}/easypsi/config:/tmp/temp $IMAGE tail -f /dev/null >/dev/null 2>&1
  docker cp ${root_path}/application.yaml ${CTR_PREFIX}-dummy:/tmp/temp/
  docker rm -f ${CTR_PREFIX}-dummy >/dev/null 2>&1
  # rm temp file
  rm -rf ${root_path}/application_01.yaml ${root_path}/application.yaml

  log "render webserver config done"
}

function do_http_probe() {
  local ctr=$1
  local endpoint=$2
  local max_retry=$3
  local retry=0
  while [ $retry -lt $max_retry ]; do
    local status_code
    # TODO support MTLS
    status_code=$(docker exec -it $ctr curl -k --write-out '%{http_code}' --silent --output /dev/null ${endpoint})
    if [[ $status_code -eq 200 || $status_code -eq 404 || $status_code -eq 401 ]]; then
      return 0
    fi
    sleep 2
    retry=$((retry + 1))
    log "check server status: ${status_code}. times: ${retry}"
  done

  return 1
}

function probe_easy_psi() {
  local easypsi_ctr=$1
  if ! do_http_probe $easypsi_ctr "http://127.0.0.1:8080" 60; then
    echo "[Error] Probe secret pad in container '$easypsi_ctr' failed. Please check the log" >&2
    exit 1
  fi
}

function check_user_name() {
  local user_name=$1
  strlen=$(echo "${user_name}" | grep -E --color '^(.{4,}).*$')
  if [ -n "${strlen}" ]; then
    return 0
  else
    log "The username requires a length greater than 4"
    return 1
  fi
}

function check_user_passwd() {
  local password=$1
  # length greater than 8
  str_len=$(echo "${password}" | grep -E --color '^(.{8,}).*$')
  # with lowercase letters
  str_low=$(echo "${password}" | grep -E --color '^(.*[a-z]+).*$')
  # with uppercase letters
  str_upp=$(echo "${password}" | grep -E --color '^(.*[A-Z]).*$')
  # with special characters
  str_ts=$(echo "${password}" | grep -E --color '^(.*\W).*$')
  # with numbers
  str_num=$(echo "${password}" | grep -E --color '^(.*[0-9]).*$')
  if [ -n "${str_len}" ] && [ -n "${str_low}" ] && [ -n "${str_upp}" ] && [ -n "${str_ts}" ] && [ -n "${str_num}" ]; then
    return 0
  else
    log "The password requires a length greater than 8, including uppercase and lowercase letters, numbers, and special characters."
    return 2
  fi
}

function account_settings() {
  local RET
  set +e
  log "Please set the username and the password used to login the KUSCIA-WEB.\n\
The username requires a length greater than 4, The password requires a length greater than 8,\n\
including uppercase and lowercase letters, numbers, and special characters."
  for ((i = 0; i < 1; i++)); do
    read -r -p "Enter username(admin):" EASYPSI_USER_NAME
    check_user_name "${EASYPSI_USER_NAME}"
    RET=$?
    if [ "${RET}" -eq 0 ]; then
      break
    elif [ "${RET}" -ne 0 ] && [ "${i}" == 0 ]; then
      log "would use default user: admin"
      EASYPSI_USER_NAME="admin"
    fi
  done
  stty -echo # disable display
  for ((i = 0; i < 3; i++)); do
    read -r -p "Enter password: " EASYPSI_PASSWORD
    echo ""
    check_user_passwd "${EASYPSI_PASSWORD}"
    RET=$?
    if [ "${RET}" -eq 0 ]; then
      local CONFIRM_PASSWD
      read -r -p "Confirm password again: " CONFIRM_PASSWD
      echo ""
      if [ "${CONFIRM_PASSWD}" == "${EASYPSI_PASSWORD}" ]; then
        break
      else
        log "Password not match! please reset"
      fi
    elif [ "${RET}" -ne 0 ] && [ "${i}" == 2 ]; then
      log "would use default password: 12#\$qwER"
      EASYPSI_PASSWORD="12#\$qwER"
    fi
  done
  set -e
  stty echo # enable display
  log "The user and password have been set up successfully."
}
function copy_kuscia_api_client_certs() {
  # notls
  if [[ ${KUSCIA_PROTOCOL}  != "notls" ]]; then
      cp ${CTR_CERT_ROOT}/ca.crt ${PAD_WORK_DIR}/config/certs/ca.crt
        # mtls
        if [[ ${KUSCIA_PROTOCOL}   -eq "mtls" ]]; then
            cp ${CTR_CERT_ROOT}/kusciaapi-client.crt ${PAD_WORK_DIR}/config/certs/client.crt
            cp ${CTR_CERT_ROOT}/kusciaapi-client.key ${PAD_WORK_DIR}/config/certs/client.pem
        fi
  fi
  cp ${CTR_CERT_ROOT}/token ${PAD_WORK_DIR}/config/certs/token
  log "copy kuscia api client certs to web server container done"
}
function prepare_pad_config() {
  # root_path
  # ├── data
  # │   ├── alice
  # │   │   └── alice.csv
  # │   └── bob
  # │       └── bob.csv
  # └── easypsi
  #     ├── config
  #     └── db
  #
  log "Prepare pad config..."
  account_settings
  local root_path=${ROOT_PATH}
  local kuscia_protocol=${KUSCIA_PROTOCOL}
  local user_name=$EASYPSI_USER_NAME
  local password=$EASYPSI_PASSWORD

  easypsi_key_pass="easypsi"
  # copy db,config,demodata from easypsi image
  log "copy db,config,demodata to '$root_path' ..."
  copy_easypsi_file_to_volume ${root_path}
  # generate server key
  log "generate server key '$root_path' ..."
  generate_easypsi_serverkey ${root_path} ${easypsi_key_pass}
  # initialize easypsi dbd
  init_easypsi_db ${root_path}
  # create easypsi user and password
  create_easypsi_user_password ${root_path} ${user_name} ${password}
  # TODO render easypsi config
  # render_easypsi_config ${root_path} ${easypsi_key_pass}
}
function start_fat_image(){
    local fat_ctr=$1 #  root-kuscia-easypsi-fat-alice
    local volume_data_path=$2 # ${root_path}/kuscia-autonomy-${NODE_ID}-data
    local volume_log_path=$3 # ${root_path}/kuscia-autonomy-${NODE_ID}-log
    local volume_pad_config_path=$4 # ${root_path}/easypsi/config
    local volume_pad_db_path=$5 # ${root_path}/easypsi/db

    echo ${fat_ctr} > ${ROOT_PATH}/continer_name
    docker run -itd --init --name=${fat_ctr} --restart=always -m $LITE_MEMORY_LIMIT \
      --volume=${volume_data_path}:/app/data \
      --volume=${volume_data_path}:/home/kuscia/var/storage/data \
      --volume=${volume_log_path}/pods:/home/kuscia/var/stdout/pods \
      --volume=${volume_log_path}/kuscia:/home/kuscia/var/logs \
      --volume=${volume_log_path}/easypsi:/app/log/easypsi \
      --volume=${volume_log_path}/pods:/app/log/pods \
      --volume=${volume_pad_config_path}:/app/config \
      --volume=${volume_pad_db_path}:/app/db \
      --workdir=/home/kuscia \
      -p $WEB_PORT:8080 \
      -p ${KUSCIA_DOMAIN_PORT}:1080 \
      -v ${ROOT_PATH}/kuscia.yaml:/home/kuscia/etc/conf/kuscia.yaml \
      -e SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE} \
      -e NODE_ID=${NODE_ID} \
      -e KUSCIA_API_ADDRESS=127.0.0.1:8083 \
      -e KUSCIA_GW_ADDRESS=127.0.0.1:80 \
      -e HOST_PATH=${volume_data_path} \
      -e KUSCIA_PROTOCOL=${KUSCIA_PROTOCOL} \
      ${EASYPSI_IMAGE}
    probe_easy_psi ${fat_ctr}
    log "web server started successfully"
    log "Please visit the website http://localhost:${port} (or http://{the IPAddress of this machine}:${port}) to experience the Kuscia web's functions ."
    log "The login name:'${EASYPSI_USER_NAME}' ,The login password:'${EASYPSI_PASSWORD}' ."
    log "The demo data would be stored in the path: ${volume_data_path} ."
    log "The kuscia tls mode is: ${KUSCIA_PROTOCOL} ."
    log "The EASYPSI_IMAGE is: ${EASYPSI_IMAGE} ."
}
function prepare_uninstall_script() {
    docker run --rm --entrypoint /bin/bash -v ${ROOT_PATH}:/tmp/scripts $EASYPSI_IMAGE -c 'cp -R /app/scripts/fatimage/uninstall_fatimage.sh /tmp/scripts/uninstall.sh && chmod +x /tmp/scripts/uninstall.sh'
    chmod +x ${ROOT_PATH}/uninstall.sh
}
function prepare_kuscia_config() {
    log "init kuscia.yaml"
    docker run --rm --entrypoint /bin/bash -v ${ROOT_PATH}:/tmp/scripts $EASYPSI_IMAGE -c 'cp -R /app/scripts/template/kuscia-autonomy-template.yaml /tmp/scripts/kuscia-autonomy-template.yaml'

    DOMAIN_KEY_DATA=$(docker run --rm --entrypoint /bin/bash $EASYPSI_IMAGE -c 'openssl genrsa 2048 | base64 | tr -d "\n"')
    CONFIG_DATA=$(sed -e "s!{{.DOMAIN_ID}}!${NODE_ID}!g;s!{{.DOMAIN_KEY_DATA}}!${DOMAIN_KEY_DATA}!g" <"${ROOT_PATH}/kuscia-autonomy-template.yaml")
    echo "${CONFIG_DATA}" > ${ROOT_PATH}/kuscia.yaml

}

# If the container exists.
fat_ctr=${CTR_PREFIX}-easypsi-fat-${NODE_ID}
if need_start_docker_container $fat_ctr; then
  log ">> Prepare uninstall script."
  prepare_uninstall_script
  log ">> Prepare kuscia config."
  prepare_kuscia_config
  log ">> Prepare pad config."
  prepare_pad_config
  log ">> Run image."
  start_fat_image $fat_ctr ${ROOT_PATH}/kuscia-autonomy-${NODE_ID}-data ${ROOT_PATH}/kuscia-autonomy-${NODE_ID}-log ${ROOT_PATH}/easypsi/config ${ROOT_PATH}/easypsi/db
  shift $((OPTIND - 1))
else
  log "The container is running. Ignore this action."
fi

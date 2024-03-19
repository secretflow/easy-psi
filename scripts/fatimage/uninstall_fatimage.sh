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
  echo -e "\033[36m[$(date +%y/%m/%d-%H:%M:%S)] \033[0m${GREEN}${log_content}${NC}"
}

CURRENT_DIR=$(cd $(dirname $0); pwd)
log "Uninstall dir: ${CURRENT_DIR}"

# Delete container
function delete_container() {
  if [ -f ${CURRENT_DIR}/continer_name ]; then
    continer_name=`cat ${CURRENT_DIR}/continer_name`
    if [[ "$(docker ps -a -q -f name=^/${continer_name}$)" ]]; then
        # need start your container
        log "Delete container ${continer_name} ..."
        docker rm -f ${continer_name} > /dev/null 2>&1
    fi
  else
    log "Container ${continer_name} does not exist."
  fi
}
# Delete data
function delete_data() {
  local RET
  log "Delete all data for path: ${CURRENT_DIR}"
  read -r -p $'\033[1;35mDelete all data? N/y: \033[0m' delete_flag
  if [[ "Y" == $delete_flag || "y" == $delete_flag  ]]; then
    cd $CURRENT_DIR
    log "Delete data ${CURRENT_DIR} ..."
    rm -rf `ls -a | grep -v uninstall | grep -v '^\.$' | grep -v '^\.\.$'`
  fi
}

delete_container
delete_data

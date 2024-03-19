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

# Load images
# Set image
# EASYPSI_FAT_IMAGE=""

GREEN='\033[0;32m'
NC='\033[0m'
function log() {
	local log_content=$1
	echo -e "${GREEN}${log_content}${NC}"
}

# Prepare
CURRENT_DIR=$(cd $(dirname $0); pwd)
PRJ_TARGET=${CURRENT_DIR}/../../target

# Rebuild pad image
read -rp "$(echo -e ${GREEN}Do you need to rebuild image? [N/y]: ${NC})" yn
case $yn in
[Yy]*)
  log "rebuild image"
  sh ${CURRENT_DIR}/../build_fatimage.sh
esac

#### Verify ####
if [[ ${EASYPSI_FAT_IMAGE} == "" ]]; then
  printf "empty EASYPSI_FAT_IMAGE\n" >&2
  exit 1
fi

# Package tar.gz
pushd ${PRJ_TARGET}
VERSION_TAG="$(git describe --tags)"
package_name=secretflow-easypsi-fat
result_tar_name=${package_name}-${VERSION_TAG}.tar.gz
mkdir -p ${package_name}/images
fatTag=${EASYPSI_FAT_IMAGE##*:}
log "fat tag: $fatTag"
docker save -o ${package_name}/images/easypsi-fat-${fatTag}.tar ${EASYPSI_FAT_IMAGE}
cp ${CURRENT_DIR}/install_fatimage.sh ${package_name}/install.sh
cp ${CURRENT_DIR}/ezpsi_get_token.sh ${package_name}/ezpsi_get_token.sh
cp ${CURRENT_DIR}/ezpsi_update_password.sh ${package_name}/ezpsi_update_password.sh
chmod +x ${package_name}/install.sh
tar --no-xattrs -zcvf ${result_tar_name} ./${package_name}
popd

log "package done. result: ${PRJ_TARGET}/${result_tar_name}"
open ${PRJ_TARGET}
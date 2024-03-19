#!/bin/bash
#
# Copyright 2023 Ant Group Co., Ltd.
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

# load images
KUSCIA_IMAGE=""
EASYPSI_IMAGE=""
SECRETFLOW_IMAGE=""

GREEN='\033[0;32m'
NC='\033[0m'
function log() {
	local log_content=$1
	echo -e "${GREEN}${log_content}${NC}"
}

package_name=secretflow-easy-psi-allinone-package
# create dir
echo "mkdir -p ${package_name}/images"
mkdir -p ${package_name}/images
# copy install.sh
path="$(cd "$(dirname $0)";pwd)"
echo "cp install.sh ${package_name}/"
cp "$path"/install.sh ${package_name}/
# copy uninstall.sh
echo "cp uninstall.sh ${package_name}/"
cp "$path"/uninstall.sh ${package_name}/
# remove temp data
echo "rm -rf ${package_name}/images/*"
rm -rf ${package_name}/images/*

if [ "${KUSCIA_IMAGE}" == "" ]; then
	KUSCIA_IMAGE=secretflow-registry.cn-hangzhou.cr.aliyuncs.com/secretflow/kuscia:latest
fi

if [ "${EASYPSI_IMAGE}" == "" ]; then
	EASYPSI_IMAGE=secretflow-registry.cn-hangzhou.cr.aliyuncs.com/secretflow/easypsi:latest
fi

if [ "${SECRETFLOW_IMAGE}" == "" ]; then
	SECRETFLOW_IMAGE=secretflow-registry.cn-hangzhou.cr.aliyuncs.com/secretflow/secretflow-lite-anolis8:latest
fi

echo "kuscia image: $KUSCIA_IMAGE"
echo "easypsi image: $EASYPSI_IMAGE"
echo "secretflow image: $SECRETFLOW_IMAGE"

set -e
echo "docker pull ${KUSCIA_IMAGE}"
docker pull ${KUSCIA_IMAGE}
log "docker pull ${KUSCIA_IMAGE} done"
echo "docker pull ${EASYPSI_IMAGE}"
docker pull ${EASYPSI_IMAGE}
log "docker pull ${EASYPSI_IMAGE} done"
echo "docker pull ${SECRETFLOW_IMAGE}"
docker pull ${SECRETFLOW_IMAGE}
log "docker pull ${SECRETFLOW_IMAGE} done"

kusciaTag=${KUSCIA_IMAGE##*:}
echo "kuscia tag: $kusciaTag"
easypsiTag=${EASYPSI_IMAGE##*:}
echo "easypsi tag: $easypsiTag"
secretflowTag=${SECRETFLOW_IMAGE##*:}
echo "secretflow tag: $secretflowTag"
VERSION_TAG="$(git describe --tags)"
echo "${package_name} tag: $VERSION_TAG"

echo "docker save -o ./${package_name}/images/kuscia-${kusciaTag}.tar ${KUSCIA_IMAGE} "
docker save -o ./${package_name}/images/kuscia-${kusciaTag}.tar ${KUSCIA_IMAGE}

echo "docker save -o ./${package_name}/images/easypsi-${easypsiTag}.tar ${EASYPSI_IMAGE} "
docker save -o ./${package_name}/images/easypsi-${easypsiTag}.tar ${EASYPSI_IMAGE}

echo "docker save -o ./${package_name}/images/secretflow-${secretflowTag}.tar ${SECRETFLOW_IMAGE} "
docker save -o ./${package_name}/images/secretflow-${secretflowTag}.tar ${SECRETFLOW_IMAGE}

echo "tar --no-xattrs -zcvf ${package_name}-${VERSION_TAG}.tar.gz ./${package_name}"
tar --no-xattrs -zcvf ${package_name}-${VERSION_TAG}.tar.gz ./${package_name}
echo "package done"

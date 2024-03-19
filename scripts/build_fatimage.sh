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

export SECRETFLOW_IMAGE=secretflow-registry.cn-hangzhou.cr.aliyuncs.com/secretflow/psi-anolis8:0.3.0beta
export KUSCIA_IMAGE=secretflow/kuscia:0.6.0b0

# Verify
if [[ ${SECRETFLOW_IMAGE} == "" ]]; then
  printf "empty SECRETFLOW_IMAGE\n" >&2
  exit 1
fi

if [[ ${KUSCIA_IMAGE} == "" ]]; then
  printf "empty KUSCIA_IMAGE\n" >&2
  exit 1
fi


CURRENT_DIR=$(cd $(dirname $0); pwd)
PRJ_ROOT_DIR=${CURRENT_DIR}/..
PRJ_TARGET_DIR=${PRJ_ROOT_DIR}/target
mkdir -p $PRJ_TARGET_DIR

cd $PRJ_ROOT_DIR
./scripts/build.sh false

# gen image name
DATETIME=$(date +"%Y%m%d%H%M%S")
git fetch --tags
VERSION_TAG="$(git describe --tags)"
commit_id=$(git log -n 1  --pretty=oneline | awk '{print $1}' | cut -b 1-6)
echo "$commit_id"
tag=${VERSION_TAG}-${DATETIME}-"${commit_id}"
local_image=easypsi:$tag
echo "local_image: ${local_image}"

# gen sf tar
docker pull ${SECRETFLOW_IMAGE}
docker save ${SECRETFLOW_IMAGE} -o ${PRJ_TARGET_DIR}/secretflow.tar

# build pad image
docker build --build-arg KUSCIA_IMAGE_NAME=${KUSCIA_IMAGE} --build-arg EASYPSI_IMAGE_NAME=${local_image} --build-arg SECRETFLOW_IMAGE_NAME=${SECRETFLOW_IMAGE} -f ./build/Dockerfiles/fatimage.Dockerfile --platform linux/amd64 -t "$local_image" .
# push image


export EASYPSI_FAT_IMAGE=${local_image}

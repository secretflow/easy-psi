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
set -e

WITH_FRONTEND_FLAG=$1
FRONTEND_TAG=$2

if [[ $WITH_FRONTEND_FLAG == "" ]]; then
  WITH_FRONTEND_FLAG=false
fi

if [[ $WITH_FRONTEND_FLAG == true ]]; then
  if [ "${FRONTEND_TAG}" == "" ]; then
	FRONTEND_TAG=$(git ls-remote --sort='version:refname' --tags https://github.com/secretflow/easy-psi-frontend.git | tail -n1 | sed 's/.*\///')
  fi
  ROOT=$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd -P)
  WORK_DIR="./tmp/frontend"
  mkdir -p $WORK_DIR
  wget -O $WORK_DIR/frontend.tar https://github.com/secretflow/easypsi-frontend/releases/download/"${FRONTEND_TAG}"/"${FRONTEND_TAG}".tar
  tar -xvf  $WORK_DIR/frontend.tar -C ${WORK_DIR} --strip-components=1
  DIST_DIR="$WORK_DIR/apps/platform/dist"
  TARGET_DIR="${ROOT}/easypsi-web/src/main/resources/static"
  mkdir -p "${TARGET_DIR}"
  cp -rpf $DIST_DIR/* "${TARGET_DIR}"
  rm -rf "$WORK_DIR"
fi

mvn clean package -DskipTests

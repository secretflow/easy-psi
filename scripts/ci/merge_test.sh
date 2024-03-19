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

EASYPSI_ROOT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )/../.." && pwd )"

echo "root path : ${EASYPSI_ROOT_DIR}"

rm -rf   "${EASYPSI_ROOT_DIR}"/surefire-report
mkdir -p "${EASYPSI_ROOT_DIR}"/surefire-report

#cp "${EASYPSI_ROOT_DIR}"/easypsi-api/client-java-kusciaapi/target/surefire-reports/*.xml "${EASYPSI_ROOT_DIR}"/surefire-report/>/dev/null || :
cp "${EASYPSI_ROOT_DIR}"/easypsi-api/client-java-easypsi/target/surefire-reports/*.xml "${EASYPSI_ROOT_DIR}"/surefire-report/>/dev/null || :
cp "${EASYPSI_ROOT_DIR}"/easypsi-common/target/surefire-reports/*.xml "${EASYPSI_ROOT_DIR}"/surefire-report/>/dev/null || :
cp "${EASYPSI_ROOT_DIR}"/easypsi-manager/target/surefire-reports/*.xml "${EASYPSI_ROOT_DIR}"/surefire-report/>/dev/null || :
cp "${EASYPSI_ROOT_DIR}"/easypsi-persistence/target/surefire-reports/*.xml "${EASYPSI_ROOT_DIR}"/surefire-report/>/dev/null || :
cp "${EASYPSI_ROOT_DIR}"/easypsi-service/target/surefire-reports/*.xml "${EASYPSI_ROOT_DIR}"/surefire-report/>/dev/null || :
cp "${EASYPSI_ROOT_DIR}"/easypsi-web/target/surefire-reports/*.xml "${EASYPSI_ROOT_DIR}"/surefire-report/>/dev/null || :

touch "${EASYPSI_ROOT_DIR}"/easypsi-test/TEST-all.xml
echo '<?xml version="1.0" encoding="UTF-8"?><testsuites>' > "${EASYPSI_ROOT_DIR}"/easypsi-test/TEST-all.xml

for file in "${EASYPSI_ROOT_DIR}"/surefire-report/*
do
    if test -f $file
    then
        echo `tail -n +2 $file` >> ${EASYPSI_ROOT_DIR}/easypsi-test/TEST-all.xml
    fi
done
echo '</testsuites>' >> "${EASYPSI_ROOT_DIR}"/easypsi-test/TEST-all.xml
rm -rf   "${EASYPSI_ROOT_DIR}"/surefire-report
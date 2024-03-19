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

FROM openanolis/anolisos:8.4-x86_64

## install openjdk 17
ARG JDK_VERSION=17.0.6+10
ARG JDK_DIR=/usr/local/openjdk-17
RUN mkdir -p ${JDK_DIR} && \
    curl -o openjdk.tar.gz https://builds.openlogic.com/downloadJDK/openlogic-openjdk/${JDK_VERSION}/openlogic-openjdk-${JDK_VERSION}-linux-x64.tar.gz && \
    tar -xvf openjdk.tar.gz -C ${JDK_DIR} --strip-components=1 && \
    rm -rf openjdk.tar.gz
ENV JAVA_HOME=${JDK_DIR}
ENV PATH=${JAVA_HOME}/bin:${PATH}

## set timezone and charset
ENV TZ=Asia/Shanghai
ENV LANG=C.UTF-8

WORKDIR /home/admin/dev

CMD ["sh"]
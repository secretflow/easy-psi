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

ARG KUSCIA_IMAGE_NAME
FROM secretflow-registry.cn-hangzhou.cr.aliyuncs.com/secretflow/secretpad-base-lite:0.2 as base_pad
FROM ${KUSCIA_IMAGE_NAME}
ARG KUSCIA_IMAGE_NAME
ARG EASYPSI_IMAGE_NAME
ARG SECRETFLOW_IMAGE_NAME
RUN echo "arg kuscia=${KUSCIA_IMAGE_NAME}"
RUN echo "arg easypsi=${EASYPSI_IMAGE_NAME}"
RUN echo "arg sf=${SECRETFLOW_IMAGE_NAME}"

## install openjdk 17
ARG JDK_VERSION=17.0.6+10
ARG JDK_DIR=/usr/local/openjdk-17
RUN mkdir -p ${JDK_DIR}
COPY --from=base_pad ${JDK_DIR} ${JDK_DIR}
ENV JAVA_HOME=${JDK_DIR}
ENV PATH=${JAVA_HOME}/bin:${PATH}

## set timezone and charset
ENV TZ=Asia/Shanghai
ENV LANG=C.UTF-8

# set image version
ENV KUSCIA_IMAGE=${KUSCIA_IMAGE_NAME}
ENV EASYPSI_IMAGE=${EASYPSI_IMAGE_NAME}
ENV SECRETFLOW_IMAGE=${SECRETFLOW_IMAGE_NAME}
WORKDIR /app

RUN #mkdir -p /var/log/easypsi && mkdir -p /app/db && mkdir -p /app/config/certs  && mkdir -p /app/tmp/scripts
RUN yum install -y sqlite && yum clean all
COPY config /app/bak/config
COPY scripts /app/bak/scripts
COPY demo/data /app/bak/data
COPY target/*.jar easypsi.jar
COPY scripts/fatimage/entrypoint_command.sh /app/entrypoint_command.sh
RUN mkdir -p /home/kuscia/image_libs
COPY target/secretflow.tar /home/kuscia/image_libs/secretflow.tar

WORKDIR /home/kuscia
RUN echo "env kuscia=${KUSCIA_IMAGE}"
RUN echo "env easypsi=${EASYPSI_IMAGE}"
RUN echo "env sf=${SECRETFLOW_IMAGE}"

# Other defatul env
ENV KUSCIA_API_ADDRESS=127.0.0.1:8083
ENV KUSCIA_GW_ADDRESS=127.0.0.1:80
ENV KUSCIA_PROTOCOL="mtls"
ENV SPRING_PROFILES_ACTIVE="p2p"


CMD ["bash",  "/app/entrypoint_command.sh"]

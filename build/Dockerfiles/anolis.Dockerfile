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
FROM secretflow-registry.cn-hangzhou.cr.aliyuncs.com/secretflow/secretpad-base-lite:0.2

ENV LANG=C.UTF-8
WORKDIR /app

RUN mkdir -p /var/log/easypsi && mkdir -p /app/db && mkdir -p /app/config/certs && yum install -y sqlite

COPY config /app/config
COPY scripts /app/scripts
COPY demo/data /app/data
COPY target/*.jar easypsi.jar

EXPOSE 80
EXPOSE 8080
EXPOSE 9001
ENTRYPOINT ["java","-jar", "-Dsun.net.http.allowRestrictedHeaders=true", "easypsi.jar"]
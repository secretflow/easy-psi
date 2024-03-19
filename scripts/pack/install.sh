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

P2P_DEFAULT_DIR="$HOME/kuscia/p2p"
set -e

usage() {
  echo "$(basename "$0") DEPLOY_MODE [OPTIONS]

p2p OPTIONS:
    -n              [mandatory] Domain id to be deployed.
    -c              [optional]  The host directory used to store domain certificates, default is 'kuscia-{{DEPLOY_MODE}}-{{DOMAIN_ID}}-certs'. It will be mounted into the domain container.
    -h              [optional]  Show this help text.
    -p              [optional]  The port exposed by kuscia-lite-gateway, The port must NOT be occupied by other processes, default 8080
    -s              [optional]  The port exposed by easypsi-edge, The port must NOT be occupied by other processes, default 8088
    -k              [optional]  The port exposed by kuscia-lite-api-http, The port must NOT be occupied by other processes, default 8081
    -g              [optional]  The port exposed by kuscia-lite-api-grpc, The port must NOT be occupied by other processes, default 8082
    -m              [optional]  The kuscia endpoint.
    -d              [optional]  The install directory. Default is ${P2P_DEFAULT_DIR}.

example:
    install.sh -n alice-domain-id'
    "
}

domain_id=
domain_host_port=
domain_api_http_port=
domain_api_grpc_port=
easypsi_edge_port=
domain_certs_dir=
master_endpoint=
token=
masterca=
volume_path=$(pwd)
install_dir=
kuscia_protocol="mtls"

while getopts 'c:d:i:n:p:s:t:m:k:g:h:' option; do
  case "$option" in
  c)
    domain_certs_dir=$OPTARG
    ;;
  d)
    install_dir=$OPTARG
    ;;
  n)
    domain_id=$OPTARG
    ;;
  p)
    domain_host_port=$OPTARG
    ;;
  s)
    easypsi_edge_port=$OPTARG
    ;;
  k)
    domain_api_http_port=$OPTARG
    ;;
  g)
    domain_api_grpc_port=$OPTARG
    ;;
  t)
    token=$OPTARG
    ;;
  m)
    master_endpoint=$OPTARG
    ;;
  v)
    masterca=$OPTARG
    ;;
  h)
    usage
    exit
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
shift $((OPTIND - 1))

for file in images/*; do
	if [ -f "$file" ]; then
		echo "$file"
		imageInfo="$(docker load <$file)"
		echo "echo ${imageInfo}"
		someimage=$(echo ${imageInfo} | sed "s/Loaded image: //")
		if [[ $someimage == *kuscia* ]]; then
			KUSCIA_IMAGE=$someimage
		elif [[ $someimage == *easypsi* ]]; then
			EASYPSI_IMAGE=$someimage
#		elif [[ $someimage == *secretflow-lite* ]]; then
#			SECRETFLOW_IMAGE=$someimage
#		elif [[ $someimage == *sf-dev-anolis8* ]]; then
#    	SECRETFLOW_IMAGE=$someimage
    elif [[ $someimage == *psi* ]]; then
      SECRETFLOW_IMAGE=$someimage
    fi
	fi
done
export KUSCIA_IMAGE=$KUSCIA_IMAGE
export EASYPSI_IMAGE=$EASYPSI_IMAGE
export SECRETFLOW_IMAGE=$SECRETFLOW_IMAGE

# deploy p2p
if [[ ${domain_id} == "" ]]; then
  printf "empty domain id\n" >&2
  exit 1
fi

if [[ ${easypsi_edge_port} == "" ]]; then
  easypsi_edge_port="8088"
fi

if [[ ${domain_host_port} == "" ]]; then
  domain_host_port="8080"
fi

if [[ ${domain_api_http_port} == "" ]]; then
  domain_api_http_port="8081"
fi

if [[ ${domain_api_grpc_port} == "" ]]; then
  domain_api_grpc_port="8082"
fi

if [[ ${install_dir} == "" ]]; then
  install_dir=${P2P_DEFAULT_DIR}
fi

# set intall dir of the deploy.sh
# the datapath is ${ROOT}/kuscia-${deploy_mode}-${DOMAIN_ID}-data
# the certpath is ${ROOT}/kuscia-${deploy_mode}-${DOMAIN_ID}-certs
# the kuscia-configpath is ${ROOT}
export ROOT=${install_dir}

mkdir -p ${ROOT}
kuscia_config=${ROOT}/kuscia.yaml
# generate kuscia configuration file
docker run --rm $KUSCIA_IMAGE bin/kuscia init --mode autonomy --domain $domain_id  --protocol $kuscia_protocol > $kuscia_config

cmd_opt="-n ${domain_id} -p ${domain_host_port} -k ${domain_api_http_port} -g ${domain_api_grpc_port} \
-d ${install_dir}/kuscia-autonomy-${domain_id}-data -l ${install_dir}/kuscia-autonomy-${domain_id}-log \
-c ${kuscia_config}"
if [[ ${domain_certs_dir} != "" ]]; then
    domain_certs_dir=${ROOT}/kuscia-autonomy-${domain_id}-certs
fi

# copy deploy.sh from kuscia image
docker run --rm $KUSCIA_IMAGE cat /home/kuscia/scripts/deploy/deploy.sh > deploy.sh && chmod u+x deploy.sh
# execute deploy lite shell
# ./deploy.sh autonomy -n alice -i 127.0.0.1 -p 48080 -k 48081 -g 48082
echo "bash $(pwd)/deploy.sh autonomy ${cmd_opt}"
bash $(pwd)/deploy.sh autonomy ${cmd_opt}

# add external name svc
docker exec -it  ${USER}-kuscia-autonomy-${domain_id} scripts/deploy/create_secretpad_svc.sh ${USER}-kuscia-easypsi-p2p-${domain_id} ${domain_id}

# initialize start_p2p.sh
#edge_opt="-n ${node_id} -s 8088 -m root-kuscia-autonomy-alice:8083"
KUSCIA_NAME="${USER}-kuscia-autonomy-${domain_id}"
edge_opt="-n ${domain_id} -s ${easypsi_edge_port} -m ${KUSCIA_NAME}:8083 -t ${kuscia_protocol}"

export EASYPSI_PORT=${easypsi_edge_port}
# initialize start_p2p.sh
docker run --rm --entrypoint /bin/bash -v $(pwd):/tmp/easypsi $EASYPSI_IMAGE -c 'cp -R /app/scripts/start_p2p.sh /tmp/easypsi/'
echo "bash $(pwd)/start_p2p.sh ${edge_opt}"
bash $(pwd)/start_p2p.sh ${edge_opt}

#!/bin/bash

#docker load < tmm-22.1.1.tgz
##/api/v1/lra-coordinator
#docker run -p 9000:9000 \
#-e NARAYANA_LRA_COMPATIBILITY_MODE='true' \
#-e storage_type='memory' \
#tmm:22.1.1

#-e authorization_enabled=false \
#-e xa_coordinator_enabled=false \
#-e XA_COORDINATOR_ENABLED=false \
#-e AUTHORIZATION_ENABLED=false \
#-e LISTEN_ADDR='0.0.0.0:9001' \
#-e EXTERNAL_ADDR='http://localhost:9002' \
#-e LOGGING_LEVEL='debug' \
#-e HTTP_TRACE_ENABLED=true \

#export CONFIG_FILE=tcs.yaml && \
#./tcs
#
#./tcs -config-file=./config.properties
#
#docker run -it -v `pwd`:/app/config -w /app/config  --env CONFIG_FILE=tcs.yaml --name tmm tmm:22.1.1

# https://artifactory.oci.oraclecorp.com/webapp/#/artifacts/browse/tree/General/blockchain-generic-local/mtrm/release/release-22.1.1/otmm-22.1.1-216-20220209_102243_UTC.zip
wget https://artifactory.oci.oraclecorp.com/blockchain-generic-local/mtrm/release/release-22.1.1/otmm-22.1.1-216-20220209_102243_UTC.zip
unzip -p otmm-*.zip otmm-22.1.1/otmm/image/tmm-22.1.1.tgz > tmm-22.1.1.tgz
docker load < tmm-22.1.1.tgz
cat <<EOF >> tcs-docker.yaml
name: tmm
listenAddr: 0.0.0.0:9000
internalAddr: http://127.0.0.1:9000
externalAddr: http://127.0.0.1:9000
xaCoordinator:
  enabled: true
lraCoordinator:
  enabled: true
tccCoordinator:
  enabled: true
maxRetryCount: 10
minRetryInterval: 1000
maxRetryInterval: 10000
httpTraceEnabled: true
storage:
  type: memory
logging:
  level: debug
  devMode: true
  tokenPropagationEnabled: false
narayanaLraCompatibilityMode:
  enabled: true
EOF
docker run --network="host" -it -v `pwd`:/app/config -w /app/config --env CONFIG_FILE=tcs-docker.yaml tmm:22.1.1

#docker run -p 9000:9000 -it -v `pwd`:/app/config -w /app/config  --env CONFIG_FILE=tcs-docker.yaml tmm:22.1.1

./tcs -storage-type=memory -listen-addr="localhost:8080" -internal-addr="http://localhost:8080" -external-addr="http://localhost:8080"
#export CONFIG_FILE=tcs.yaml && ./tcs
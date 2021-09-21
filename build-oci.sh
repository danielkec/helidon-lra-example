#!/bin/bash
VERSION=1.0
NS=cinema-reservation
DIR=$(pwd)
[ -n "$1" ] && OCI_REGISTRY="$1/"
[ -n "$2" ] && TENANCY_NS="$2/"

for service in 'payment-service' 'seat-booking-service';
  do
    cd "${DIR}/${service}" || exit
    tag="${OCI_REGISTRY}${TENANCY_NS}${NS}/${service}:${VERSION}"
    executed_cmd="docker build -t ${tag} ."
    echo "${executed_cmd}" && eval "${executed_cmd}"
    executed_cmd="docker push ${tag}"
    echo "${executed_cmd}" && eval "${executed_cmd}"
  done
#!/bin/bash
VERSION=1.0
NS=cinema-reservation
DIR=$(pwd)

eval $(minikube docker-env)

docker image load -i ./tmm-22.1.1.tgz

for service in 'payment-service' 'seat-booking-service';
  do
    cd "${DIR}/${service}" || exit
    tag="${NS}/${service}:${VERSION}"
    executed_cmd="docker build -t ${tag} ."
    echo "${executed_cmd}" && eval "${executed_cmd}"
  done
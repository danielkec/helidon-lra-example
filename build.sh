#!/bin/bash
VERSION=1.0
NS=cinema-reservation
DIR=$(pwd)

# Fail fast in case docker hub login is needed
docker pull mysql:8
docker pull jbosstm/lra-coordinator:5.12.1.Final

for service in 'payment-service' 'seat-booking-service';
  do
    cd "${DIR}/${service}" || exit
    tag="${NS}/${service}:${VERSION}"
    executed_cmd="docker build -t ${tag} ."
    echo "${executed_cmd}" && eval "${executed_cmd}"
  done
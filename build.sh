#!/bin/bash
VERSION=1.0
REGISTRY=""
DIR=$(pwd)

eval $(minikube docker-env)

# Fail fast in case docker hub login is needed
docker pull docker.io/mysql:8
docker pull docker.io/jbosstm/lra-coordinator:5.12.1.Final

cd "${DIR}/payment-service" || exit
docker build -t "${REGISTRY}cinema-reservation/payment-service:${VERSION}" .

cd "${DIR}/seat-booking-service" || exit
docker build -t "${REGISTRY}cinema-reservation/seat-booking-service:${VERSION}" .

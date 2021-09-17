#!/bin/bash

NAMESPACE=cinema-reservation
MAIN_SERVICE=seat-booking-service
DB_SERVICE=booking-db

kubectl delete namespace ${NAMESPACE}
kubectl create namespace ${NAMESPACE}
kubectl apply -k . --namespace ${NAMESPACE}
kubectl config set-context --current --namespace=${NAMESPACE}

# Expose on Minikube
kubectl expose deployment ${MAIN_SERVICE} --type=NodePort --port=8080 --name=${NAMESPACE}
echo "Application ${NAMESPACE} will be available at $(minikube service ${MAIN_SERVICE} -n ${NAMESPACE} --url)"

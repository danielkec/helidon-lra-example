#!/bin/bash

NAMESPACE=cinema-reservation
MAIN_SERVICE=seat-booking-service

kubectl delete namespace ${NAMESPACE}
kubectl create namespace ${NAMESPACE}
kubectl config set-context --current --namespace=${NAMESPACE}

cat <<EOF >./kustomization.yaml
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization

resources:
  - booking-db/app.yaml
  - payment-service/app.yaml
  - seat-booking-service/app.yaml
  - lra-coordinator-service/app.yaml
EOF

kubectl apply -k . --namespace ${NAMESPACE}

# Expose on Minikube
kubectl expose deployment ${MAIN_SERVICE} --type=NodePort --port=8080 --name=${NAMESPACE}
echo "Application ${NAMESPACE} will be available at $(minikube service ${MAIN_SERVICE} -n ${NAMESPACE} --url)"

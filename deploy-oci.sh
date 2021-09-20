#!/bin/bash

NAMESPACE=cinema-reservation
SEAT_SERVICE=seat-booking-service
OCI_IMAGE_REGISTRY=fra.ocir.io/fr8yxyel2vcv

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
- oci-lb.yaml

images:
- name: cinema-reservation/payment-service
  newName: fra.ocir.io/fr8yxyel2vcv/repokec/cinema-reservation/payment-service
  newTag: "1.0"
- name: cinema-reservation/seat-booking-service
  newName: fra.ocir.io/fr8yxyel2vcv/repokec/cinema-reservation/seat-booking-service
  newTag: "1.0"
EOF

kubectl apply -k .

# Expose on OCI
kubectl expose deployment ${SEAT_SERVICE} --port=8080 --type=LoadBalancer --name=${NAMESPACE}
echo "Application ${NAMESPACE} will be available at ???"
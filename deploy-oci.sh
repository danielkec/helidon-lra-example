#!/bin/bash
##
# The script is expected to run in the OCI Cloud console
#

NAMESPACE=cinema-reservation

kubectl delete namespace ${NAMESPACE}
kubectl create namespace ${NAMESPACE}
kubectl config set-context --current --namespace=${NAMESPACE}

TENANCY_NAMESPACE=$(oci os ns get --query "data" --raw-output)
REGION_KEY=${OCI_CONFIG_PROFILE}
OCI_IMAGE_REGISTRY=${REGION_KEY}.ocir.io/${TENANCY_NAMESPACE}

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
    newName: ${OCI_IMAGE_REGISTRY}/cinema-reservation/payment-service
    newTag: "1.0"
  - name: cinema-reservation/seat-booking-service
    newName: ${OCI_IMAGE_REGISTRY}/cinema-reservation/seat-booking-service
    newTag: "1.0"
EOF

kubectl apply -k . --namespace ${NAMESPACE}
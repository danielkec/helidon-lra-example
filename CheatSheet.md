## Prepare minikube
```shell
minikube start
# Use minikube's docker registry
eval $(minikube docker-env)
```

## Investigate
```shell
# Set current namespace
kubectl config set-context --current --namespace=cinema-reservation
# List all pods
kubectl get pods
# List all services
kubectl get svc

# Inspect seat booking service
kubectl logs --tail 100 -l app=seat-booking-service
kubectl describe pod -l app=seat-booking-service

# Inspect payment service
kubectl logs --tail 100 -l app=payment-service
kubectl describe pod -l app=payment-service

# Inspect coordinator service
kubectl logs --tail 100 -l app=lra-coordinator
kubectl describe pod -l app=lra-coordinator
```
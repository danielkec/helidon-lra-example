## Prepare minikube
```shell
minikube start --driver=virtualbox
# Use minikube's docker registry
eval $(minikube docker-env) 
```

## Cleanup
```shell
# delete all pods and services
kubectl delete deployment lra-coordinator
kubectl delete pods,services -l name=lra-coordinator
```

## Investigate
```shell    
# List all pods
kubectl get pods -o wide
```
# K8s Docker hub login
```shell
#danielkec/...
docker login
# Standard
#cat ~/.docker/config.json
# Snap installed docker
cat ~/snap/docker/current/.docker/config.json
# Create K8s secret
kubectl create secret generic dockercred \
    --from-file=.dockerconfigjson=/home/kec/snap/docker/current/.docker/config.json \
    --type=kubernetes.io/dockerconfigjson
    
# Inspect created secret
kubectl get secret dockercred --output=yaml
```

Use secret for pulling the image
```yaml
apiVersion: v1
kind: Pod
metadata:
  name: private-reg
spec:
  containers:
  - name: private-reg-container
    image: <your-private-image>
  imagePullSecrets:
  - name: dockercred
```


```shell
docker run --name booking-db \
      -e MYSQL_ROOT_PASSWORD=toor \
      -e MYSQL_DATABASE=booking-db \
      -e MYSQL_USER=user \
      -e MYSQL_PASSWORD=pass \
      --network host \
      mysql:8
```
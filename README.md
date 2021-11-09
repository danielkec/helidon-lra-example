# Helidon MicroProfile LRA example
Set of services demonstrating usage of the 
[MicroProfile Long Running Actions](https://download.eclipse.org/microprofile/microprofile-lra-1.0/microprofile-lra-spec-1.0.html)
in Helidon MP. This example is accompanying 
[article](https://danielkec.github.io/blog/helidon/lra/saga/2021/10/12/helidon-lra.html) 
describing usage of [LRA in 
Helidon MP](https://helidon.io/docs/v2/#/mp/lra/01_introduction).

The article:

https://danielkec.github.io/blog/helidon/lra/saga/2021/10/12/helidon-lra.html

## Online cinema booking system
Our hypothetical cinema needs an online reservation system. 
We will split it in the two scalable services:
* [seat-booking-service](/seat-booking-service), 
* [payment-service](/payment-service)

Our services will be completely separated, integrated only through the REST API calls.

Additional services are needed in order to coordinate
LRA transactions and persist booking data: 
* [lra-coordinator-service](/lra-coordinator-service) 
* [booking-db](/booking-db) 

### Deploy to minikube
Prerequisites:
* Installed and started minikube
* Environment with
  [minikube docker daemon](https://minikube.sigs.k8s.io/docs/handbook/pushing/#1-pushing-directly-to-the-in-cluster-docker-daemon-docker-env) - `eval $(minikube docker-env)`

#### Build images
As we work directly with
[minikube docker daemon](https://minikube.sigs.k8s.io/docs/handbook/pushing/#1-pushing-directly-to-the-in-cluster-docker-daemon-docker-env)
all we need to do is build the docker images.
```shell
bash build.sh;
```
Note that the first build can take few minutes for all the artifacts to download.
Subsequent builds are going to be much faster as the layer with dependencies gets cached.

### Deploy to minikube
Prerequisites:
* Installed and started minikube
* Environment with
  [minikube docker daemon](https://minikube.sigs.k8s.io/docs/handbook/pushing/#1-pushing-directly-to-the-in-cluster-docker-daemon-docker-env) - `eval $(minikube docker-env)`

#### Build images
As we work directly with
[minikube docker daemon](https://minikube.sigs.k8s.io/docs/handbook/pushing/#1-pushing-directly-to-the-in-cluster-docker-daemon-docker-env)
all we need to do is build the docker images.
```shell
bash build.sh;
```
Note that the first build can take few minutes for all of the artifacts to download.
Subsequent builds are going to be much faster as the layer with dependencies is cached.

#### Deploy to minikube
```shell
bash deploy-minikube.sh
```
This script recreates the whole namespace, any previous state of the `cinema-reservation` is obliterated.
Deployment is exposed via the NodePort and the URL with port is printed at the end of the output:
```shell
namespace "cinema-reservation" deleted
namespace/cinema-reservation created
Context "minikube" modified.
service/booking-db created
service/lra-coordinator created
service/payment-service created
service/seat-booking-service created
deployment.apps/booking-db created
deployment.apps/lra-coordinator created
deployment.apps/payment-service created
deployment.apps/seat-booking-service created
service/cinema-reservation exposed
Application cinema-reservation will be available at http://192.0.2.254:31584
```

### Deploy to OCI OKE cluster
Prerequisites:
* [OKE K8s cluster](https://docs.oracle.com/en/learn/container_engine_kubernetes)
* OCI Cloud Shell with git, docker and kubectl configured to access the Oracle Container Engine for Kubernetes (OKE) cluster

#### Pushing images to your OCI Container registry
The first thing you will need is a place to push your docker images to so that OKE K8s have a location to pull from.
[Container registry](https://docs.oracle.com/en-us/iaas/Content/Registry/Concepts/registryprerequisites.htm#Availab)
is part of your OCI tenancy, so to be able to push to it you need to log in:
`docker login <REGION_KEY>.ocir.io`
Username of the registry is `<TENANCY_NAMESPACE>/joe@example.com`
where `joe@example.com` is your OCI user.
Password will be [auth token](https://docs.oracle.com/en-us/iaas/Content/Registry/Tasks/registrygettingauthtoken.htm)
of your `joe@example.com`
To get your region key and tenancy namespace, execute the following command in your OCI Cloud Shell:

```shell
# Get tenancy namespace and container registry
echo "" && \
echo "Container registry: ${OCI_CONFIG_PROFILE}.ocir.io" && \
echo "Tenancy namespace: $(oci os ns get --query "data" --raw-output)" && \
echo "" && \
echo "docker login ${OCI_CONFIG_PROFILE}.ocir.io" && \
echo "Username: $(oci os ns get --query "data" --raw-output)/joe@example.com" && \
echo "Password: --- Auth token for user joe@example.com" && \
echo ""
```
Example output:
```shell
Container registry: eu-frankfurt-1.ocir.io
Tenancy namespace: fr8yxyel2vcv

docker login eu-frankfurt-1.ocir.io
Username: fr8yxyel2vcv/joe@example.com
Password: --- Auth token for user joe@example.com
```
Save your container registry, tenancy namespace, and auth token for later.

When your local docker is logged in to OCI Container Registry, you can execute `build-oci.sh`
with the container registry and tenancy namespace as the parameters.

Example:
```shell
bash build-oci.sh eu-frankfurt-1.ocir.io fr8yxyel2vcv
```
Example output:
```shell
docker build -t eu-frankfurt-1.ocir.io/fr8yxyel2vcv/cinema-reservation/payment-service:1.0 .
...
docker push eu-frankfurt-1.ocir.io/fr8yxyel2vcv/cinema-reservation/seat-booking-service:1.0
...
docker build -t eu-frankfurt-1.ocir.io/fr8yxyel2vcv/cinema-reservation/seat-booking-service:1.0 .
...
docker push eu-frankfurt-1.ocir.io/fr8yxyel2vcv/cinema-reservation/payment-service:1.0
...
```
The script will print out docker build commands before executing them.
Note that the first build can take few minutes for all the artifacts to download.
Subsequent builds are going to be much faster as the layer with dependencies is cached.

To make your pushed images publicly available, open your OCI console and set both repositories to **Public**:
**Developer Tools**>**Containers & Artifacts**>
[**Container Registry**](https://cloud.oracle.com/registry/containers/repos)


![https://cloud.oracle.com/registry/containers/repos](images/public-registry.png)

#### Deploy to OKE
You can use the cloned helidon-lra-example repository in the OCI Cloud shell with your K8s descriptors.
Your changes are built to the images you pushed in the previous step.

In the OCI Cloud shell:
```shell
git clone https://github.com/danielkec/helidon-lra-example.git
cd helidon-lra-example
bash deploy-oci.sh

kubectl get services
```
Example output:
```shell
NAME                         TYPE           CLUSTER-IP      EXTERNAL-IP   PORT(S)          AGE
booking-db                   ClusterIP       192.0.2.254    <none>        3306/TCP         34s
lra-coordinator              NodePort        192.0.2.253    <none>        8070:32434/TCP   33s
oci-load-balancing-service   LoadBalancer    192.0.2.252    <pending>     80:31192/TCP     33s
payment-service              NodePort        192.0.2.251    <none>        8080:30842/TCP   32s
seat-booking-service         NodePort        192.0.2.250    <none>        8080:32327/TCP   32s
```

You can see that right after the deployment, the EXTERNAL-IP of the external LoadBalancer reads as `<pending>`
because OCI is provisioning it for you. You can invoke `kubectl get services` a little later
and see that it now gives you an external IP address with Helidon Cinema example exposed on port 80.

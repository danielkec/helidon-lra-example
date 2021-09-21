# helidon-lra-example
Online Cinema Booking


## Deploy to OCI OKE cluster

### Pushing images to your OCI Container registry
First thing you need is a place to push you docker images to, so 
OKE k8s can pull them from such place. 
[Container registry](https://docs.oracle.com/en-us/iaas/Content/Registry/Concepts/registryprerequisites.htm#Availab) 
is part of your OCI tenancy, to be able to push in it, you just need to 
`docker login <REGION_KEY>.ocir.io` in it.
Username of the registry is `<TENANCY_NAMESPACE>/joe@acme.com` 
where `joe@acme.com` is your OCI user. 
Password will be [auth token](https://docs.oracle.com/en-us/iaas/Content/Registry/Tasks/registrygettingauthtoken.htm) 
of your `joe@acme.com`
For getting region key and tenancy namespace just execute following cmd in your OCI Cloud Shell: 
```shell
# Get tenancy namespace and container registry
echo "" && \
echo "Container registry: ${OCI_CONFIG_PROFILE}.ocir.io" && \
echo "Tenancy namespace: $(oci os ns get --query "data" --raw-output)" && \
echo "" && \
echo "docker login ${OCI_CONFIG_PROFILE}.ocir.io" && \
echo "Username: $(oci os ns get --query "data" --raw-output)/joe@acme.com" && \
echo "Password: --- Auth token for user joe@acme.com" && \
echo ""
# Example output
>Container registry: eu-frankfurt-1.ocir.io
>Tenancy namespace: fr8yxyel2vcv
>
>docker login eu-frankfurt-1.ocir.io
>Username: fr8yxyel2vcv/joe@acme.com
>Password: --- Auth token for user joe@acme.com
```
Save your container registry, tenancy namespace and auth token for later.

When your docker is logged in to OCI Container Registry, you can execute `build-oci.sh`
with container registry and tenancy namespace as the parameters.

Example:
```shell
./build-oci.sh eu-frankfurt-1.ocir.io fr8yxyel2vcv
# Example output
>docker build -t eu-frankfurt-1.ocir.io/fr8yxyel2vcv/cinema-reservation/payment-service:1.0 .
...
>docker push eu-frankfurt-1.ocir.io/fr8yxyel2vcv/cinema-reservation/seat-booking-service:1.0
...
>docker build -t eu-frankfurt-1.ocir.io/fr8yxyel2vcv/cinema-reservation/seat-booking-service:1.0 .
...
>docker push eu-frankfurt-1.ocir.io/fr8yxyel2vcv/cinema-reservation/payment-service:1.0
...
```
The script will print out docker build commands before executing them. 
First build can take few minutes for all the artefacts to download, 
subsequent build are going to be much faster as the layer with dependencies gets cached. 


### Deploy to OKE
Prerequisites:
* [OKE k8s cluster](https://www.oracle.com/webfolder/technetwork/tutorials/obe/oci/oke-full/index.html) 
* OCI Cloud Shell with git, docker and kubectl configured for access OKE cluster 

In the OCI Cloud shell: 
```shell
git clone https://github.com/danielkec/helidon-lra-example.git
cd helidon-lra-example
bash deploy-oci.sh
```
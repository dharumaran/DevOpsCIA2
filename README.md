# CI/CD Demo – Java app on AKS

Spring Boot app built with Maven, containerised with Docker, pushed to Azure Container Registry and
deployed to Azure Kubernetes Service by a GitHub Actions pipeline (deployment step automated with Ansible).

## Architecture
Developer -> git push -> GitHub Actions -> Maven build + tests -> Docker image -> ACR -> AKS (Deployment + LoadBalancer Service) -> browser

## Run locally
```bash
mvn clean package
java -jar target/app.jar          # http://localhost:8080
docker build -t cicd-demo .
docker run -p 8080:8080 cicd-demo
```

## One-time Azure setup
```bash
# names: ACR name must be globally unique, lowercase letters/digits only
RG=cicd-rg; LOC=centralindia; ACR=cicddemo<yourinitials>; AKS=cicd-aks

az login
az group create -n $RG -l $LOC
az acr create -g $RG -n $ACR --sku Basic
az aks create -g $RG -n $AKS --tier free --node-count 2 --node-vm-size Standard_B2s \
  --attach-acr $ACR --generate-ssh-keys

# service principal for GitHub (copy the whole JSON output)
SUB=$(az account show --query id -o tsv)
az ad sp create-for-rbac --name cicd-github --role contributor \
  --scopes /subscriptions/$SUB/resourceGroups/$RG --sdk-auth
```
If your region or VM size isn't available, pick another (`az vm list-skus -l $LOC -o table`).

## GitHub configuration (repo > Settings > Secrets and variables > Actions)
| Type     | Name                | Value                          |
|----------|---------------------|--------------------------------|
| Secret   | `AZURE_CREDENTIALS` | JSON from the service principal |
| Variable | `ACR_NAME`          | your ACR name (no `.azurecr.io`) |
| Variable | `AKS_RESOURCE_GROUP`| `cicd-rg`                      |
| Variable | `AKS_CLUSTER`       | `cicd-aks`                     |

## Pipeline stages (`.github/workflows/ci-cd.yml`)
1. Checkout  2. Maven compile + package  3. Unit tests (reports uploaded)  4. Docker build, tagged with commit SHA
5. Push to ACR  6. Deploy to AKS (Ansible: apply manifests, set image)  7. Verify rollout and HTTP health

Pull requests run only the build/test job; pushes to `main` run the full pipeline.

## Redeployment demo
1. Edit `MESSAGE` in `HelloController.java` (e.g. "Version 2 - deployed by the pipeline").
2. `git add . && git commit -m "Update message" && git push`
3. Watch the Actions run, then refresh the service IP in the browser.

Find the endpoint: `kubectl get service cicd-demo` (EXTERNAL-IP).

## Clean up (stops all charges)
```bash
az group delete -n cicd-rg --yes --no-wait
```

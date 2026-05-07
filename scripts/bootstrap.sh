#!/usr/bin/env bash
# shellcheck source=scripts/_lib.sh
. "$(dirname "$0")/_lib.sh"

: "${GHCR_USER:?GHCR_USER env var required}"

log "running precheck..."
GHCR_USER="$GHCR_USER" "$(dirname "$0")/precheck.sh"
SHA="$(gh api "repos/{owner}/{repo}/commits/master" --jq .sha)"

log "terraform apply (VPC, EKS, ECR, Secrets Manager, IRSA)..."
(
  cd "$REPO_ROOT/infra/terraform"
  [ -f terraform.tfvars ] || fail "infra/terraform/terraform.tfvars missing — copy terraform.tfvars.example"
  terraform init -upgrade
  terraform apply -auto-approve
)

REGION="$(tfout region)"
CLUSTER="$(tfout cluster_name)"
ECR_REGISTRY="$(tfout ecr_registry)"
ECR_BACKEND="$(tfout ecr_backend_url)"
ECR_FRONTEND="$(tfout ecr_frontend_url)"
ESO_ROLE_ARN="$(tfout eso_role_arn)"
LETSENCRYPT_EMAIL="$(grep -E '^letsencrypt_email' "$REPO_ROOT/infra/terraform/terraform.tfvars" | awk -F'"' '{print $2}')"

log "updating kubeconfig..."
aws eks update-kubeconfig --region "$REGION" --name "$CLUSTER" >/dev/null

log "writing generated secrets to AWS Secrets Manager..."
DB_PASSWORD="$(openssl rand -base64 32 | tr -d '/+=\n\r')"
JWT_SECRET="$(openssl rand -base64 64 | tr -d '/+=\n\r')"
GRAFANA_PASSWORD="$(openssl rand -base64 24 | tr -d '/+=\n\r')"

SECRET_DB="$(tfout secret_arn_db)"
SECRET_JWT="$(tfout secret_arn_jwt)"
SECRET_SMTP="$(tfout secret_arn_smtp)"
SECRET_GRAFANA="$(tfout secret_arn_grafana)"

# Strip potential \r from Terraform outputs
SECRET_DB="${SECRET_DB%$'\r'}"
SECRET_JWT="${SECRET_JWT%$'\r'}"
SECRET_SMTP="${SECRET_SMTP%$'\r'}"
SECRET_GRAFANA="${SECRET_GRAFANA%$'\r'}"

aws secretsmanager put-secret-value --secret-id "$SECRET_DB" --secret-string "$(jq -n --arg u postgres --arg p "$DB_PASSWORD" '{username:$u,password:$p}')" >/dev/null
aws secretsmanager put-secret-value --secret-id "$SECRET_JWT" --secret-string "$(jq -n --arg s "$JWT_SECRET" '{secret:$s}')" >/dev/null
# SMTP: leave as placeholder so MAIL_HOST is empty (backend already guards on empty).
aws secretsmanager put-secret-value --secret-id "$SECRET_SMTP" --secret-string '{"host":"","port":"587","username":"","password":""}' >/dev/null
aws secretsmanager put-secret-value --secret-id "$SECRET_GRAFANA" --secret-string "$(jq -n --arg u admin --arg p "$GRAFANA_PASSWORD" '{username:$u,password:$p}')" >/dev/null

log "helm dependency build (platform)..."
(cd "$REPO_ROOT/infra/helm/platform" && helm dependency build)

log "adding Helm repos for standalone operator installs..."
helm repo add jetstack https://charts.jetstack.io >/dev/null 2>&1 || helm repo add jetstack https://charts.jetstack.io
helm repo add external-secrets https://charts.external-secrets.io >/dev/null 2>&1 || helm repo add external-secrets https://charts.external-secrets.io
helm repo update >/dev/null

log "disabling cert-manager and external-secrets subcharts in platform release..."
helm upgrade --install platform "$REPO_ROOT/infra/helm/platform" \
  --namespace platform \
  --create-namespace \
  --dependency-update \
  --wait --timeout 10m \
  --set bootstrap.createCustomResources=false \
  --set cert-manager.enabled=false \
  --set external-secrets.enabled=false \
  --set kube-prometheus-stack.enabled=false \
  --set region="$REGION" \
  --set letsencryptEmail="$LETSENCRYPT_EMAIL" \
  --set "external-secrets.serviceAccount.annotations.eks\.amazonaws\.com/role-arn=$ESO_ROLE_ARN"

log "installing cert-manager as a standalone release..."
helm upgrade --install platform-cert-manager jetstack/cert-manager \
  --namespace platform \
  --create-namespace \
  --wait --timeout 10m \
  --set crds.enabled=true \
  --set prometheus.enabled=false

log "installing external-secrets as a standalone release..."
helm upgrade --install platform-external-secrets external-secrets/external-secrets \
  --namespace platform \
  --create-namespace \
  --wait --timeout 10m \
  --set installCRDs=true \
  --set serviceAccount.name=external-secrets \
  --set "serviceAccount.annotations.eks\.amazonaws\.com/role-arn=$ESO_ROLE_ARN"

log "waiting for cert-manager and external-secrets CRDs..."
wait_for "cert-manager ClusterIssuer CRD" 180 \
  kubectl get crd clusterissuers.cert-manager.io
wait_for "external-secrets CRDs" 180 \
  bash -c 'kubectl get crd externalsecrets.external-secrets.io clustersecretstores.external-secrets.io >/dev/null'

log "waiting for cert-manager and external-secrets controllers..."
kubectl -n platform wait --for=condition=Available deployment -l app.kubernetes.io/name=cert-manager --timeout=5m
kubectl -n platform wait --for=condition=Available deployment -l app.kubernetes.io/name=external-secrets --timeout=5m

log "rebuilding platform chart dependencies before custom resources upgrade..."
(cd "$REPO_ROOT/infra/helm/platform" && helm dependency build)

log "helm upgrade platform custom resources..."
helm upgrade platform "$REPO_ROOT/infra/helm/platform" \
  --namespace platform \
  --dependency-update \
  --wait --timeout 10m \
  --set bootstrap.createCustomResources=true \
  --set cert-manager.enabled=false \
  --set external-secrets.enabled=false \
  --set kube-prometheus-stack.enabled=false \
  --set region="$REGION" \
  --set letsencryptEmail="$LETSENCRYPT_EMAIL" \
  --set "external-secrets.serviceAccount.annotations.eks\.amazonaws\.com/role-arn=$ESO_ROLE_ARN"

log "waiting for Grafana admin secret from External Secrets..."
wait_for "grafana-admin secret" 180 \
  kubectl -n platform get secret grafana-admin

log "installing kube-prometheus-stack as a standalone release..."
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts >/dev/null 2>&1 || helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update >/dev/null
helm upgrade --install platform-kube-prometheus-s prometheus-community/kube-prometheus-stack \
  --namespace platform \
  --create-namespace \
  --wait --timeout 10m \
  --set alertmanager.enabled=true \
  --set "alertmanager.alertmanagerSpec.storage.volumeClaimTemplate.spec.storageClassName=gp2" \
  --set "alertmanager.alertmanagerSpec.storage.volumeClaimTemplate.spec.accessModes[0]=ReadWriteOnce" \
  --set "alertmanager.alertmanagerSpec.storage.volumeClaimTemplate.spec.resources.requests.storage=1Gi" \
  --set prometheus.prometheusSpec.retention=6h \
  --set prometheus.prometheusSpec.serviceMonitorSelectorNilUsesHelmValues=false \
  --set grafana.admin.existingSecret=grafana-admin \
  --set grafana.admin.userKey=admin-user \
  --set grafana.admin.passwordKey=admin-password \
  --set grafana.sidecar.dashboards.enabled=true \
  --set grafana.sidecar.dashboards.label=grafana_dashboard \
  --set grafana.service.type=ClusterIP

log "waiting for NLB hostname..."
wait_for "ingress-nginx NLB hostname" 180 \
  bash -c 'kubectl -n platform get svc platform-ingress-nginx-controller \
    -o jsonpath="{.status.loadBalancer.ingress[0].hostname}" | grep -q .'

NLB_HOST="$(kubectl -n platform get svc platform-ingress-nginx-controller \
  -o jsonpath='{.status.loadBalancer.ingress[0].hostname}')"
log "NLB hostname: $NLB_HOST"

log "resolving NLB to IPv4..."
NLB_IP=""
for _ in {1..30}; do
  NLB_IP="$(dig +short "$NLB_HOST" | grep -Eo '^[0-9]+\.[0-9]+\.[0-9]+\.[0-9]+$' | head -n1 || true)"
  [ -n "$NLB_IP" ] && break
  sleep 5
done
[ -n "$NLB_IP" ] || fail "failed to resolve $NLB_HOST to IPv4 in 150s"

HOST="futsal-${NLB_IP//./-}.nip.io"
log "public hostname: $HOST"

log "mirroring images GHCR -> ECR via skopeo..."
PASSWORD="$(aws ecr get-login-password --region "$REGION")"

skopeo copy --all --dest-creds "AWS:$PASSWORD" \
  "docker://ghcr.io/${GHCR_USER}/futsal-backend:${SHA}" \
  "docker://${ECR_BACKEND}:${SHA}"
skopeo copy --all --dest-creds "AWS:$PASSWORD" \
  "docker://ghcr.io/${GHCR_USER}/futsal-frontend:${SHA}" \
  "docker://${ECR_FRONTEND}:${SHA}"

log "helm install futsal (app)..."
helm upgrade --install futsal "$REPO_ROOT/infra/helm/futsal" \
  --namespace futsal \
  --wait --timeout 5m \
  --set host="$HOST" \
  --set corsAllowedOrigins="https://$HOST" \
  --set backend.image.repository="$ECR_BACKEND" \
  --set backend.image.tag="$SHA" \
  --set frontend.image.repository="$ECR_FRONTEND" \
  --set frontend.image.tag="$SHA"

log "waiting for Let's Encrypt certificate..."
wait_for "certificate ready" 300 \
  bash -c 'kubectl -n futsal get certificate futsal-tls -o jsonpath="{.status.conditions[?(@.type==\"Ready\")].status}" | grep -q True'

log "waiting for backend readiness..."
wait_for "backend deployment ready" 300 \
  kubectl -n futsal rollout status deploy/backend --timeout=5m

log "bootstrap done. URL: https://$HOST"
log "next: GHCR_USER=$GHCR_USER HOST=$HOST ./scripts/verify.sh"

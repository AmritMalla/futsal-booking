#!/usr/bin/env bash
# shellcheck source=scripts/_lib.sh
. "$(dirname "$0")/_lib.sh"

: "${GHCR_USER:?GHCR_USER env var required}"

log "running precheck..."
GHCR_USER="$GHCR_USER" "$(dirname "$0")/precheck.sh"
SHA="$(gh api "repos/${GHCR_USER}/$(basename "$REPO_ROOT")/commits/main" --jq .sha)"

log "terraform apply (VPC, EKS, ECR, Secrets Manager, IRSA)..."
(
  cd "$REPO_ROOT/deploy/terraform"
  [ -f terraform.tfvars ] || fail "deploy/terraform/terraform.tfvars missing — copy terraform.tfvars.example"
  terraform init -upgrade
  terraform apply -auto-approve
)

REGION="$(tfout region)"
CLUSTER="$(tfout cluster_name)"
ECR_REGISTRY="$(tfout ecr_registry)"
ECR_BACKEND="$(tfout ecr_backend_url)"
ECR_FRONTEND="$(tfout ecr_frontend_url)"
ESO_ROLE_ARN="$(tfout eso_role_arn)"
LETSENCRYPT_EMAIL="$(grep -E '^letsencrypt_email' "$REPO_ROOT/deploy/terraform/terraform.tfvars" | awk -F'"' '{print $2}')"

log "updating kubeconfig..."
aws eks update-kubeconfig --region "$REGION" --name "$CLUSTER" >/dev/null

log "writing generated secrets to AWS Secrets Manager..."
DB_PASSWORD="$(openssl rand -base64 32 | tr -d '/+=\n')"
JWT_SECRET="$(openssl rand -base64 64 | tr -d '/+=\n')"
GRAFANA_PASSWORD="$(openssl rand -base64 24 | tr -d '/+=\n')"

aws secretsmanager put-secret-value --secret-id /futsal/sandbox/db \
  --secret-string "$(jq -n --arg u postgres --arg p "$DB_PASSWORD" '{username:$u,password:$p}')" >/dev/null
aws secretsmanager put-secret-value --secret-id /futsal/sandbox/jwt \
  --secret-string "$(jq -n --arg s "$JWT_SECRET" '{secret:$s}')" >/dev/null
# SMTP: leave as placeholder so MAIL_HOST is empty (backend already guards on empty).
aws secretsmanager put-secret-value --secret-id /futsal/sandbox/smtp \
  --secret-string '{"host":"","port":"587","username":"","password":""}' >/dev/null
aws secretsmanager put-secret-value --secret-id /futsal/sandbox/grafana \
  --secret-string "$(jq -n --arg u admin --arg p "$GRAFANA_PASSWORD" '{username:$u,password:$p}')" >/dev/null

log "helm dependency update (platform)..."
(cd "$REPO_ROOT/deploy/helm/platform" && helm dependency update)

log "helm install platform..."
helm upgrade --install platform "$REPO_ROOT/deploy/helm/platform" \
  --namespace platform \
  --create-namespace \
  --wait --timeout 10m \
  --set region="$REGION" \
  --set letsencryptEmail="$LETSENCRYPT_EMAIL" \
  --set "external-secrets.serviceAccount.annotations.eks\.amazonaws\.com/role-arn=$ESO_ROLE_ARN"

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
aws ecr get-login-password --region "$REGION" \
  | skopeo login --username AWS --password-stdin "$ECR_REGISTRY"

skopeo copy --all \
  "docker://ghcr.io/${GHCR_USER}/futsal-backend:${SHA}" \
  "docker://${ECR_BACKEND}:${SHA}"
skopeo copy --all \
  "docker://ghcr.io/${GHCR_USER}/futsal-frontend:${SHA}" \
  "docker://${ECR_FRONTEND}:${SHA}"

log "helm install futsal (app)..."
helm upgrade --install futsal "$REPO_ROOT/deploy/helm/futsal" \
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

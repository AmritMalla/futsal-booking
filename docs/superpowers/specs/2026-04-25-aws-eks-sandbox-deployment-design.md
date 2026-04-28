# AWS EKS Sandbox Deployment — Design

**Status:** Approved for implementation planning
**Date:** 2026-04-25
**Owner:** Amrit Malla
**Scope:** Deploy the futsal booking project to AWS EKS in a Pluralsight 4-hour sandbox via scripts, using production-standard patterns.

---

## 1. Goals, Non-Goals, Success Criteria

### Goals

- One-command bootstrap of a production-shaped stack on a fresh Pluralsight AWS sandbox in **≤ 30 min wall-clock**, leaving ≥ 3h 30m for demo, recording, and iteration.
- Every architectural choice either *is* a production best practice or has an explicit one-line "swap to X for real prod" note.
- Portfolio-legible: a reviewer reading `deploy/` + the README's deployment section can describe the production architecture without running anything.

### Non-Goals

- A permanently live demo URL (impossible given a 4-hour sandbox).
- Multi-environment promotion (dev → staging → prod); there is only "sandbox," with production equivalents documented.
- Cost optimization — sandbox is credit-backed, so clarity is prioritized over spend.
- Rewriting application code for "cloud-native" beyond what the deployment requires. Local filesystem uploads via PVC are kept; S3 is a documented future step.

### Success Criteria

1. `scripts/bootstrap.sh` on a fresh sandbox produces a working public HTTPS URL serving the frontend, with backend API reachable at `/api`.
2. `scripts/verify.sh` runs the existing integration test suite against the deployed URL and passes.
3. The architecture section of the README includes a diagram and a **"Sandbox vs Production" mapping table** that names every compromise.
4. All IaC, manifests, and scripts live under `deploy/` and `scripts/`. No ad-hoc config lives only on the developer machine.

---

## 2. Architecture Overview

```
Browser
  │ HTTPS (Let's Encrypt via nip.io)
  ▼
┌─────────────────────────────────────────────────────────────┐
│  AWS Sandbox (us-east-1, one VPC, 2 public + 2 private AZs)│
│                                                              │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  EKS Cluster (1.30)                                  │    │
│  │                                                       │    │
│  │  ingress-nginx (LoadBalancer → NLB in public subnet) │    │
│  │     │                                                 │    │
│  │     ├── "/"       → frontend Deployment (nginx)      │    │
│  │     └── "/api/*"  → backend Deployment (Spring Boot) │    │
│  │                                                       │    │
│  │  postgres (Bitnami StatefulSet, PVC on gp3)          │    │
│  │  cert-manager (Let's Encrypt HTTP-01 via nip.io)     │    │
│  │  external-secrets (ESO) ──► AWS Secrets Manager      │    │
│  │  kube-prometheus-stack + Loki (ops namespace)        │    │
│  │                                                       │    │
│  │  Node group: 2× t3.large on-demand, private subnets  │    │
│  └─────────────────────────────────────────────────────┘    │
│                                                              │
│  ECR repos: futsal-backend, futsal-frontend                 │
│  Secrets Manager: /futsal/sandbox/{db,jwt,smtp}             │
└─────────────────────────────────────────────────────────────┘

        ▲                                ▲
        │ skopeo mirror (~90s)           │ terraform + helm
        │                                │
┌───────┴──────────┐              ┌──────┴───────┐
│ GHCR (CI pushes  │              │ Developer    │
│ tagged images    │              │ laptop with  │
│ on push to main) │              │ sandbox creds│
└──────────────────┘              └──────────────┘
```

### Key flows

- **Build (continuous):** GitHub Actions on push to `main` builds multi-arch images for backend and frontend, tags with git SHA + `latest`, pushes to GHCR. Runs regardless of whether a sandbox is active, so images are always ready.
- **Bootstrap (per sandbox session):** `bootstrap.sh` runs `terraform apply` (VPC, EKS, ECR, Secrets Manager entries, IRSA roles), then `skopeo copy` mirrors GHCR → ECR, then `helm upgrade --install` deploys the umbrella platform chart and the `futsal` app chart.
- **Request:** Browser → NLB → ingress-nginx → frontend or backend pod; backend → in-cluster Postgres Service; backend uploads → PVC-backed volume; backend secrets → ESO-synced K8s Secret sourced from AWS Secrets Manager.

### Why this shape

- Two subnets per AZ (public + private): public holds the NLB, private holds nodes and Postgres — standard VPC layout.
- Ingress-NGINX instead of ALB Controller: allows cert-manager to terminate TLS in-cluster with Let's Encrypt (the ALB path cannot do Let's Encrypt cleanly without Route53, which is out of scope).
- In-cluster Postgres is the one major sandbox accommodation; every other piece mirrors production patterns.

---

## 3. Infrastructure (Terraform)

### Layout

```
deploy/terraform/
├── main.tf              # provider, backend (local state)
├── versions.tf          # terraform + provider version pins
├── variables.tf         # region, cluster_name, node_instance_type, tags
├── vpc.tf               # terraform-aws-modules/vpc/aws (2 AZs, public + private)
├── eks.tf               # terraform-aws-modules/eks/aws (managed node group)
├── ecr.tf               # aws_ecr_repository × 2 (backend, frontend)
├── secrets.tf           # aws_secretsmanager_secret × 3 (db, jwt, smtp)
├── iam.tf               # IRSA role for External Secrets Operator
├── addons.tf            # EKS addons: vpc-cni, coredns, kube-proxy, ebs-csi
├── outputs.tf           # cluster_name, ecr_urls, secret_arns, eso_role_arn
└── terraform.tfvars.example
```

### Key decisions

- **State:** local `terraform.tfstate`. Sandbox is ephemeral and gets destroyed; remote state would outlive it and accumulate garbage. Documented trade-off: "prod uses S3 + DynamoDB lock."
- **Modules:** `terraform-aws-modules/vpc/aws` and `terraform-aws-modules/eks/aws`. Community-standard, well-audited.
- **EKS version:** 1.30 (supported through mid-2026).
- **Node group:** 1 managed node group, `t3.large`, desired=2, private subnets, on-demand. Spot is too risky for mid-demo eviction.
- **EBS CSI driver:** installed as EKS addon via IRSA so the Postgres PVC can provision gp3 volumes.
- **IRSA** only for ESO at the Terraform layer. cert-manager, ingress-nginx, and Prometheus do not need IAM.
- **No Route53, no ACM, no ALB Controller** — explicitly out of scope (sandbox cannot own a domain).

### Pluralsight sandbox accommodations

- Default region: `us-east-1` (confirmed usable on Pluralsight). Configurable via variable.
- `precheck.sh` verifies `aws sts get-caller-identity` works and warns if not in the expected region.
- Resource names are short and prefixed with `futsal-sandbox-` for identification in the shared console.

### Estimated apply time

~20 min: VPC ~2m, EKS control plane ~12m, node group ~4m, addons + ECR + SM ~2m.

---

## 4. Kubernetes Workloads (Helm)

### Layout

```
deploy/helm/
├── platform/                  # umbrella chart for cluster-wide add-ons
│   ├── Chart.yaml             # dependencies block lists community charts
│   ├── values.yaml
│   └── templates/
│       ├── namespaces.yaml
│       └── cluster-issuer.yaml   # Let's Encrypt ClusterIssuer (staging + prod)
│
└── futsal/                    # app chart
    ├── Chart.yaml
    ├── values.yaml
    └── templates/
        ├── backend-deployment.yaml
        ├── backend-service.yaml
        ├── backend-hpa.yaml
        ├── backend-pdb.yaml
        ├── frontend-deployment.yaml
        ├── frontend-service.yaml
        ├── ingress.yaml
        ├── uploads-pvc.yaml
        ├── backend-externalsecret.yaml
        └── _helpers.tpl
```

### Platform umbrella chart dependencies

Pinned versions, installed in one `helm upgrade --install platform`:

- `ingress-nginx` → NLB via `service.beta.kubernetes.io/aws-load-balancer-type: nlb`
- `cert-manager` + a `ClusterIssuer` using Let's Encrypt HTTP-01
- `external-secrets` with a `ClusterSecretStore` pointing at AWS Secrets Manager in the sandbox region, using the IRSA role from Terraform
- `bitnami/postgresql` — single replica, PVC on gp3, password pulled from a pre-created K8s secret (ESO-synced)
- `kube-prometheus-stack` (Prometheus + Grafana + Alertmanager) in the `ops` namespace
- `grafana/loki-stack` for logs

### `futsal` app chart — production-standard Deployment patterns

Backend and frontend Deployments both include:

- `resources.requests` + `resources.limits` (backend: 500m/1Gi request, 1000m/2Gi limit).
- `livenessProbe` + `readinessProbe` (backend: `/actuator/health/liveness` and `/readiness`; frontend: `/`).
- `securityContext`: non-root user, read-only root FS where possible, `allowPrivilegeEscalation: false`, `seccompProfile: RuntimeDefault`, drop ALL capabilities.
- `topologySpreadConstraints` to spread replicas across nodes.
- `PodDisruptionBudget` (min 1 available).
- `HorizontalPodAutoscaler` on CPU (backend only, 2–4 replicas).
- Labels following the `app.kubernetes.io/*` recommended set.

### Ingress

Single Ingress object, host `futsal-<nlb-ipv4-dashed>.nip.io`, TLS section annotated with `cert-manager.io/cluster-issuer: letsencrypt-prod`. Path rules: `/api/*` → backend, `/` → frontend. `bootstrap.sh` reads the NLB's assigned IP after ingress-nginx is up and renders the host into the Helm release via `--set`.

### Application-side changes required

1. Enable Spring Boot Actuator `health` endpoint with liveness/readiness groups in a new `application-kubernetes.yml` profile.
2. Confirm frontend API calls use relative `/api` paths so one image works across environments. If absolute URLs exist, switch them.
3. `uploads/` directory mounts a PVC; confirm the code already writes to a configurable path and parameterize via env var if not.
4. Add `SPRING_PROFILES_ACTIVE=kubernetes` env var in the backend Deployment.
5. Add `micrometer-registry-prometheus` dependency to `pom.xml` for `/actuator/prometheus`.

### Estimated Helm install time

~7 min: platform chart ~5m (cert-manager CRDs + LB provisioning), app chart ~1m, Let's Encrypt cert issuance ~1m.

---

## 5. Image Pipeline (CI + sandbox mirror)

### CI side — `.github/workflows/image-build.yml`

Triggers: push to `main`, manual dispatch, pull requests (PR builds don't push).

Jobs:

1. **`test`** — runs existing Maven + frontend lint/test suites. Blocks image build.
2. **`build-backend`** — buildx multi-stage using the existing `Dockerfile`. Tags: `ghcr.io/<user>/futsal-backend:${{ github.sha }}` + `:latest`. Pushes to GHCR using the built-in `GITHUB_TOKEN`.
3. **`build-frontend`** — new `frontend/Dockerfile` (two-stage: `node:20-alpine` for `npm run build`, `nginx:1.27-alpine` for serve). Same tagging scheme. Pushes to `ghcr.io/<user>/futsal-frontend`.
4. **`sbom-scan`** — `anchore/scan-action` runs Trivy on both images and uploads SARIF to GitHub Security tab.

Images are public on GHCR so the sandbox does not need a pull secret for mirroring.

### Sandbox mirror (inside `bootstrap.sh`)

After Terraform creates the two ECR repos:

```bash
SHA=$(gh api repos/{owner}/{repo}/commits/master --jq .sha)

aws ecr get-login-password --region "$REGION" \
  | skopeo login --username AWS --password-stdin "$ECR_REGISTRY"

skopeo copy --all \
  docker://ghcr.io/<user>/futsal-backend:${SHA} \
  docker://${ECR_BACKEND}:${SHA}

skopeo copy --all \
  docker://ghcr.io/<user>/futsal-frontend:${SHA} \
  docker://${ECR_FRONTEND}:${SHA}

helm upgrade --install futsal deploy/helm/futsal \
  --set backend.image.repository=${ECR_BACKEND} \
  --set backend.image.tag=${SHA} \
  --set frontend.image.repository=${ECR_FRONTEND} \
  --set frontend.image.tag=${SHA}
```

### Why skopeo over docker pull/push

skopeo copies layers registry-to-registry without pulling them through the local machine. ~90 seconds for both images vs ~4–5 minutes for docker-based transfer.

### Why mirror to ECR at all

- Nodes authenticate to ECR via their node IAM role — no image-pull secret.
- Demonstrates ECR + IAM-for-image-pulls, the interview-relevant AWS pattern.
- Documented as: "in real prod, CI would push directly to ECR via OIDC; we mirror because sandbox credentials rotate every 4h."

### Frontend API URL strategy

Frontend calls relative `/api/*` paths; the Ingress handles routing. One image works across environments — no build-time API URL baked in.

### Tag strategy

Always deploy by git SHA, never `:latest`. The `latest` tag exists for humans browsing GHCR.

---

## 6. Secrets Management

### Sources of truth

Three AWS Secrets Manager entries, containers created by Terraform, values populated at bootstrap time:

| Secret name | Keys | Populated by |
|---|---|---|
| `/futsal/sandbox/db` | `username`, `password` | `bootstrap.sh` (generates random password) |
| `/futsal/sandbox/jwt` | `secret` | `bootstrap.sh` (generates 64-byte base64) |
| `/futsal/sandbox/smtp` | `host`, `port`, `username`, `password` | Developer fills `terraform.tfvars` or skips (mail disabled in sandbox by default) |

Terraform creates containers only. Values are written by the bootstrap script with `aws secretsmanager put-secret-value`, keeping secrets out of `terraform.tfstate`.

### Sync into the cluster

External Secrets Operator runs in the `external-secrets` namespace. A single `ClusterSecretStore` points at AWS Secrets Manager in the sandbox region using the IRSA role Terraform provisioned.

Two `ExternalSecret` objects in the `futsal` namespace:

- `backend-db` → K8s Secret `futsal-backend-db` with keys `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`.
- `backend-app` → K8s Secret `futsal-backend-app` with `JWT_SECRET`, `SPRING_MAIL_HOST`, `SPRING_MAIL_PORT`, etc.

Backend Deployment loads both via `envFrom.secretRef`. Refresh interval: 1 hour.

### Postgres credentials flow

- `bootstrap.sh` generates the DB password and writes it to `/futsal/sandbox/db`.
- ESO syncs it into the `futsal` namespace as `futsal-backend-db`.
- The Bitnami Postgres chart is installed with `auth.existingSecret: futsal-backend-db` and `auth.secretKeys.adminPasswordKey: SPRING_DATASOURCE_PASSWORD`, so server and client read the same secret. No password hits the filesystem or terraform state.

### What does not go through Secrets Manager

- Postgres service DNS name, DB name, JDBC URL template — configuration, not secrets. Live in `values.yaml` as plain env vars.
- CORS allowed origins — configuration. Set via env var at deploy time.

### Production equivalence

- Sandbox: secrets populated by an interactive bootstrap script.
- Production: secrets populated by a separate platform process (e.g., Terraform with `-target` excluding secrets, a one-time operator runbook, or CI with sealed approval). Application-side pattern — ESO → K8s Secret → `envFrom` — is identical.

---

## 7. Observability

Installed as part of the `platform` umbrella chart, in its own `ops` namespace.

### What gets installed

| Component | Source | Purpose |
|---|---|---|
| Prometheus | `kube-prometheus-stack` | scrapes cluster + workload metrics |
| Grafana | `kube-prometheus-stack` | dashboards |
| Alertmanager | `kube-prometheus-stack` | wired in but no receivers configured |
| node-exporter, kube-state-metrics | `kube-prometheus-stack` | node + K8s object metrics |
| Loki + Promtail | `grafana/loki-stack` | log aggregation |

### Sandbox tuning

- Prometheus retention: `6h`.
- Prometheus storage: `emptyDir`, not PVC.
- Grafana: `adminPassword` pulled from Secrets Manager via ESO.
- Grafana exposed via `kubectl port-forward`, not a public Ingress. Prevents an open Grafana on the internet for 4 hours.
- Alertmanager receivers: empty (documented as "configure Slack/PagerDuty webhook in prod").

### Backend instrumentation

- Spring Boot Actuator exposes `/actuator/prometheus` (requires `micrometer-registry-prometheus` dependency).
- A `ServiceMonitor` in the `futsal` chart tells Prometheus to scrape the backend Service at `/actuator/prometheus`.
- One pre-provisioned Grafana dashboard: "Futsal Backend Overview" (request rate, p95 latency, JVM heap, DB pool active). Committed as JSON under `deploy/helm/platform/dashboards/` and loaded via the Grafana dashboard sidecar.

### Logs

- stdout from every pod flows through Promtail → Loki.
- One Grafana Explore-ready LogQL query saved: `{namespace="futsal",app="backend"} |= "ERROR"`.

### Out of scope

- Distributed tracing (OTel / Jaeger / Tempo). README notes "production adds OTel + Tempo."
- Custom SLO/SLI definitions.
- CloudWatch Container Insights (duplicate of in-cluster stack).

---

## 8. Scripts & Developer UX

### Script surface

```
scripts/
├── _lib.sh            # logging helpers, tool-presence checks
├── precheck.sh        # verifies sandbox is usable before burning time
├── bootstrap.sh       # full provision + deploy
├── verify.sh          # smoke tests + integration suite against deployed URL
└── teardown.sh        # manual early teardown (optional; sandbox auto-cleans)
```

Each script ≤ 200 lines, uses `set -euo pipefail`, sources the shared library.

### `precheck.sh` (~5 sec)

- `aws sts get-caller-identity` succeeds.
- Region is expected (`us-east-1` or configured).
- Required CLI tools present: `terraform`, `kubectl`, `helm`, `skopeo`, `jq`, `gh`, `aws`. Fails loudly with install hints.
- GHCR images for target SHA exist (`docker manifest inspect`).

### `bootstrap.sh` (~30 min)

Prints a labeled, timed progress log:

```
[00:00] precheck... ok
[00:05] terraform apply (VPC, EKS, ECR, SM, IRSA)... running
[20:12] terraform apply... done
[20:12] aws eks update-kubeconfig... done
[20:15] generating secrets + writing to Secrets Manager... done
[20:20] helm install platform... running
[25:30] waiting for NLB hostname... done
[25:35] resolving NLB to IP for nip.io hostname...
[25:40] mirroring images GHCR -> ECR via skopeo... done
[27:10] helm install futsal... done
[28:00] waiting for Let's Encrypt cert... issued
[29:00] waiting for backend readiness... ready
[29:15] bootstrap done. URL: https://futsal-<ip>.nip.io
```

Key properties:

- **Idempotent** — re-running after partial failure picks up where Terraform/Helm left off.
- **Fail-fast with actionable errors** — each step's failure message says what to do next.
- **NLB-IP-to-nip.io resolution** — ingress-nginx exposes a Service of type LoadBalancer, NLB gets an IP after ~30s, script polls `nslookup`, then renders the final hostname into the Ingress via `helm upgrade --set`.

### `verify.sh` (~3 min)

- `curl` health endpoints: `/api/actuator/health/liveness`, `/api/actuator/health/readiness`, `/`.
- TLS cert is valid (chain resolves, not self-signed).
- Runs the existing Maven integration test suite with `-Dtest.base-url=https://<host>` against the deployed stack.
- Exits non-zero on any failure.

### `teardown.sh` (~5 min, optional)

- `helm uninstall futsal platform`.
- `terraform destroy`.
- Exists for the portfolio story ("graceful drain in real prod"). Sandbox auto-cleanup otherwise rips everything down at the 4h mark.

### README one-liner

```
./scripts/precheck.sh && ./scripts/bootstrap.sh && ./scripts/verify.sh
```

---

## 9. Repository Structure

Only new/changed paths are shown:

```
deploy/
├── terraform/
│   ├── main.tf  versions.tf  variables.tf  outputs.tf
│   ├── vpc.tf  eks.tf  ecr.tf  secrets.tf  iam.tf  addons.tf
│   └── terraform.tfvars.example
├── helm/
│   ├── platform/           # ingress-nginx, cert-manager, ESO, postgres, observability
│   │   ├── Chart.yaml  values.yaml
│   │   ├── templates/      # namespaces, ClusterIssuer, ClusterSecretStore
│   │   └── dashboards/     # Grafana JSON
│   └── futsal/             # backend + frontend + ingress + PVC + ExternalSecret
│       ├── Chart.yaml  values.yaml
│       └── templates/
└── README.md               # deploy-specific runbook

frontend/
└── Dockerfile              # NEW: two-stage node:20 build + nginx:1.27 serve

src/main/resources/
└── application-kubernetes.yml  # NEW: profile with actuator groups, prod logging

scripts/
├── _lib.sh
├── precheck.sh  bootstrap.sh  verify.sh  teardown.sh

.github/workflows/
└── image-build.yml         # NEW: test + build + push to GHCR + Trivy scan

docs/
├── aws_eks_deployment_plan.md     # superseded; redirect banner at top
└── superpowers/specs/
    └── 2026-04-25-aws-eks-sandbox-deployment-design.md  # this spec

README.md                    # new Deployment section + arch diagram + mapping table
```

---

## 10. Sandbox-vs-Production Mapping

The credibility artifact. Goes into the root README verbatim.

| Concern | Sandbox (this repo) | Real production | Why the compromise |
|---|---|---|---|
| Terraform state | Local `terraform.tfstate` | S3 backend + DynamoDB lock | Sandbox is ephemeral; no multi-engineer concurrency |
| Database | Bitnami Postgres in cluster, PVC on gp3 | RDS Multi-AZ, automated snapshots | Saves ~10 min of a 4h window; Postgres-in-K8s not recommended for prod |
| TLS | Let's Encrypt HTTP-01 via nip.io hostname | ACM cert + Route53 for owned domain | Sandbox cannot own a domain |
| Ingress | NGINX Ingress Controller + NLB | AWS Load Balancer Controller + ALB (or same NGINX) | Cert-manager path needed; NLB works fine |
| Image push | GitHub Actions → GHCR, bootstrap mirrors to ECR | GitHub Actions OIDC → ECR directly | Pluralsight sandbox creds rotate every 4h; cannot wire OIDC to a short-lived account |
| Secrets population | Bootstrap script generates + writes | Separate platform process / sealed CI approval | Interactive origin fine for sandbox; application-side flow identical |
| File uploads | PVC-backed volume | S3 bucket + presigned URLs | Out of scope; documented follow-up |
| Node group | 2× t3.large on-demand | Spot + on-demand mix, multi-AZ, cluster autoscaler | Sandbox: simplicity > optimization |
| Observability | Prometheus/Loki emptyDir, 6h retention | Persistent volumes or managed (AMP/AMG), long retention, paging receivers | Sandbox has no long-lived data to keep |
| Grafana access | `kubectl port-forward` | SSO-gated Ingress | Don't expose an open Grafana on the internet |
| Tracing | Not installed | OTel SDK + Tempo/X-Ray | Time budget |

---

## 11. Time Budget

Estimated total wall-clock for a fresh sandbox:

```
precheck                 0:05
terraform apply         20:00
kubeconfig + pre-install 0:20
helm install platform    5:00
NLB provision + nip.io   0:30
skopeo mirror            1:30
helm install futsal      1:00
Let's Encrypt cert       1:00
backend readiness        1:15
──────────────────────
total                   30:40
```

Leaves ~3h 29m for demo, recording, iteration, or intentional teardown.

---

## 12. Risks & Mitigations

| Risk | Mitigation |
|---|---|
| EKS control plane creation is slow (~12m) and occasionally flakes | `bootstrap.sh` retries the wait loop; total budget assumes one clean run, partial failures are idempotent |
| Let's Encrypt rate-limits for nip.io (shared domain) | Use the `letsencrypt-staging` issuer during testing; switch to `letsencrypt-prod` once confident. Issuer is configurable via values |
| Pluralsight sandbox blocks an unexpected IAM action | `precheck.sh` attempts a cheap test-create on each service and exits early with a clear error |
| NLB takes longer than 30s to get an IP | Script polls with backoff up to 3 min before failing |
| GHCR images for target SHA don't exist yet (CI still running) | `precheck.sh` verifies the image exists via `docker manifest inspect` and waits/exits clearly |
| Postgres PVC fails to bind (EBS CSI not ready) | EBS CSI is installed as an EKS addon in the same `terraform apply`; `helm install platform` waits on CSI driver rollout before the Postgres sub-chart runs |
| In-cluster Postgres data loss on pod restart | Acceptable — sandbox is ephemeral by design. Documented in the mapping table |

---

## 13. Follow-ups (Explicitly Out of Scope)

- Upload storage migration to S3 + presigned URLs.
- Domain purchase + Route53 + ACM path (would replace nip.io for a permanent demo).
- GitHub Actions → OIDC → AWS deploy pipeline (only meaningful in a non-ephemeral account).
- Distributed tracing with OTel.
- Backend readiness probe depth (DB connectivity check, not just process alive).
- Cluster autoscaler + spot mixing.
- Multi-environment (dev/staging/prod) overlays.

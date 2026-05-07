# AWS EKS Sandbox Deployment

Script-driven deployment of the futsal arena app to AWS EKS, tuned for a 4-hour Pluralsight sandbox. See the full design in [`docs/superpowers/specs/2026-04-25-aws-eks-sandbox-deployment-design.md`](../docs/superpowers/specs/2026-04-25-aws-eks-sandbox-deployment-design.md).

## Prerequisites

- Active Pluralsight AWS sandbox (4-hour lifetime).
- Local tools: `aws`, `terraform` >= 1.6, `kubectl`, `helm` 3, `skopeo`, `jq`, `gh`, `openssl`, `dig`, `shellcheck`.
- GitHub Actions has already built images to GHCR (see `.github/workflows/image-build.yml`).
- GHCR packages are set to **public** (one-time manual step).

## Configuration

```bash
cp infra/terraform/terraform.tfvars.example infra/terraform/terraform.tfvars
$EDITOR infra/terraform/terraform.tfvars   # set letsencrypt_email at minimum
```

## Running

```bash
export GHCR_USER=<your-github-username-or-org>
aws sso login   # or: export AWS credentials from the Pluralsight sandbox page

./scripts/precheck.sh
./scripts/bootstrap.sh   # ~30 min
HOST="$(helm -n futsal get values futsal -o json | jq -r .host)" ./scripts/verify.sh
```

The bootstrap prints the public URL at the end.

## Teardown (optional)

```bash
./scripts/teardown.sh
```

The sandbox auto-destroys everything at the 4-hour mark; `teardown.sh` is only useful for graceful early shutdown.

## What gets installed

- **AWS:** VPC (2 AZs, public + private), EKS 1.30, 2× `t3.large` nodes, ECR (backend + frontend), Secrets Manager (db, jwt, smtp, grafana), IRSA role for ESO.
- **In-cluster:** ingress-nginx (NLB), cert-manager (Let's Encrypt), External Secrets Operator, Bitnami Postgres (PVC on gp3), kube-prometheus-stack, Loki.
- **App:** backend (Spring Boot, 2× replicas, HPA 2–4), frontend (unprivileged nginx, 2× replicas), shared Ingress with TLS.

See the root `README.md` for the Sandbox-vs-Production mapping table.

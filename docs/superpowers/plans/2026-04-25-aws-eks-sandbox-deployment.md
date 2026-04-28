# AWS EKS Sandbox Deployment Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Stand up the futsal booking app on AWS EKS in a Pluralsight 4-hour sandbox via `./scripts/bootstrap.sh`, using production-standard patterns (Terraform, Helm, ESO, cert-manager, ingress-nginx, observability, CI image build → ECR mirror).

**Architecture:** Terraform provisions VPC + EKS + ECR + Secrets Manager + IRSA in ~20 min. An umbrella `platform` Helm chart installs ingress-nginx, cert-manager, External Secrets Operator, Bitnami Postgres, and kube-prometheus-stack + Loki. The `futsal` Helm chart deploys backend + frontend + Ingress + PVC + ExternalSecrets. GitHub Actions builds images and pushes to GHCR; `bootstrap.sh` mirrors GHCR → ECR with skopeo (~90 s) and installs both charts. TLS is Let's Encrypt via a `nip.io` hostname derived from the NLB's IPv4. No RDS, no Route53, no ACM — documented sandbox compromises with a production-equivalence table.

**Tech Stack:** Spring Boot 3.3 / Java 17 / React 18 / TypeScript / Terraform 1.6+ / AWS provider 5.x / Helm 3 / Kubernetes 1.30 / EKS / ingress-nginx / cert-manager / External Secrets Operator / kube-prometheus-stack / Loki / skopeo / bash.

**Spec reference:** [docs/superpowers/specs/2026-04-25-aws-eks-sandbox-deployment-design.md](../specs/2026-04-25-aws-eks-sandbox-deployment-design.md)

**Validation conventions in this plan:**
- Java code: JUnit (TDD pattern).
- Terraform: `terraform fmt -check` + `terraform validate` + `terraform plan` (cannot apply without sandbox).
- Helm: `helm lint` + `helm template ... | kubectl apply --dry-run=client -f -`.
- Shell scripts: `shellcheck`.
- End-to-end: only runnable against a live sandbox via `scripts/verify.sh`.

**Placeholder convention:** `<GHCR_USER>` = GitHub username/org of this repo (resolved at deploy time, configured in `deploy/terraform/terraform.tfvars`). `<REGION>` defaults to `us-east-1` but is a variable.

---

## Phase 1 — Application-side Prep

Small, focused code changes so the existing backend is Kubernetes-ready: a `kubernetes` Spring profile, Prometheus metrics, and ensuring the frontend can be served at a relative path.

### Task 1.1: Add `micrometer-registry-prometheus` dependency

**Files:**
- Modify: `pom.xml`

- [ ] **Step 1: Add the dependency**

Add this inside `<dependencies>` in `pom.xml` (after the `spring-boot-starter-actuator` block, around line 42):

```xml
        <!-- Prometheus metrics for Spring Boot Actuator -->
        <dependency>
            <groupId>io.micrometer</groupId>
            <artifactId>micrometer-registry-prometheus</artifactId>
        </dependency>
```

- [ ] **Step 2: Verify the build still compiles**

Run: `./mvnw -q -DskipTests package`
Expected: `BUILD SUCCESS`

- [ ] **Step 3: Verify the actuator exposes `/actuator/prometheus` at runtime**

Create a throwaway test to confirm the registry is wired:

Run: `./mvnw -q -Dtest='FustsalApiApplicationTests' test`
Expected: PASS (baseline smoke test still passes).

- [ ] **Step 4: Commit**

```bash
git add pom.xml
git commit -m "feat(backend): add micrometer prometheus registry for /actuator/prometheus"
```

### Task 1.2: Create `application-kubernetes.properties` profile

**Files:**
- Create: `src/main/resources/application-kubernetes.properties`

- [ ] **Step 1: Write the failing test**

Create: `src/test/java/com/amrit/futsal/config/KubernetesProfileTest.java`

```java
package com.amrit.futsal.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("kubernetes")
class KubernetesProfileTest {

    @LocalServerPort int port;
    @Autowired TestRestTemplate rest;

    @Test
    void livenessEndpointIsExposed() {
        ResponseEntity<String> r = rest.getForEntity("http://localhost:" + port + "/actuator/health/liveness", String.class);
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void readinessEndpointIsExposed() {
        ResponseEntity<String> r = rest.getForEntity("http://localhost:" + port + "/actuator/health/readiness", String.class);
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void prometheusEndpointIsExposed() {
        ResponseEntity<String> r = rest.getForEntity("http://localhost:" + port + "/actuator/prometheus", String.class);
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody()).contains("jvm_memory_used_bytes");
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./mvnw -q -Dtest='KubernetesProfileTest' test`
Expected: FAIL — endpoints not exposed (404) because the `kubernetes` profile doesn't exist yet.

- [ ] **Step 3: Create the profile properties file**

Create `src/main/resources/application-kubernetes.properties`:

```properties
# Kubernetes profile — intended for in-cluster deployment
# Inherits everything from application.properties; overrides below.

server.port=${SERVER_PORT:8080}

# Actuator: probes + prometheus only
management.endpoints.web.exposure.include=health,info,prometheus
management.endpoint.health.probes.enabled=true
management.endpoint.health.show-details=never
management.health.livenessstate.enabled=true
management.health.readinessstate.enabled=true

# JPA: sandbox uses `update`; real prod would use `validate` + Flyway
spring.jpa.hibernate.ddl-auto=${JPA_DDL_AUTO:update}
spring.jpa.show-sql=false

# Logging: JSON-friendly single-line pattern for Loki ingestion
logging.pattern.console=%d{yyyy-MM-dd'T'HH:mm:ss.SSSZ} %-5level [%X{traceId:-},%X{spanId:-}] %logger{36} - %msg%n
logging.level.org.springframework.web=info
logging.level.org.hibernate.SQL=warn
logging.level.com.amrit.futsal=info

# File upload directory defaults for PVC mount
file.upload-dir=${FILE_UPLOAD_DIR:/var/app/uploads}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./mvnw -q -Dtest='KubernetesProfileTest' test`
Expected: PASS — all three endpoints return 200.

- [ ] **Step 5: Commit**

```bash
git add src/main/resources/application-kubernetes.properties src/test/java/com/amrit/futsal/config/KubernetesProfileTest.java
git commit -m "feat(backend): add kubernetes spring profile with actuator probes + prometheus"
```

### Task 1.3: Expose `file.upload-dir` writability via the kubernetes profile

The existing `FileStorageService` already uses `@Value("${file.upload-dir:uploads}")`. The K8s profile sets it to `/var/app/uploads`, which will be a PVC mount. No code changes needed — just confirmation via test.

**Files:**
- Test only.

- [ ] **Step 1: Write the test**

Create: `src/test/java/com/amrit/futsal/service/FileStorageServicePathTest.java`

```java
package com.amrit.futsal.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("kubernetes")
@TestPropertySource(properties = "file.upload-dir=/tmp/futsal-test-uploads")
class FileStorageServicePathTest {

    @Value("${file.upload-dir}")
    String configuredUploadDir;

    @Autowired
    FileStorageService fileStorageService;

    @Test
    void uploadDirIsConfigurable() {
        assertThat(configuredUploadDir).isEqualTo("/tmp/futsal-test-uploads");
        assertThat(fileStorageService).isNotNull();
    }
}
```

- [ ] **Step 2: Run test**

Run: `./mvnw -q -Dtest='FileStorageServicePathTest' test`
Expected: PASS.

- [ ] **Step 3: Commit**

```bash
git add src/test/java/com/amrit/futsal/service/FileStorageServicePathTest.java
git commit -m "test(backend): verify file.upload-dir is env-driven under kubernetes profile"
```

### Task 1.4: Frontend image-safe API base URL

`frontend/src/services/api.ts` currently falls back to `http://localhost:8090/api/v1`. In the container, we'll set `REACT_APP_API_BASE_URL=/api/v1` at build time (Task 2.1). We also need `fileService.ts` to compose URLs correctly from a relative base.

**Files:**
- Modify: `frontend/src/services/fileService.ts`

- [ ] **Step 1: Read current file**

Check line 17 of `frontend/src/services/fileService.ts`:

Current:
```ts
return `${process.env.REACT_APP_API_BASE_URL}/files/${fileName}`;
```

- [ ] **Step 2: Make the URL construction work with a relative base**

Edit `frontend/src/services/fileService.ts`: replace the `${process.env.REACT_APP_API_BASE_URL}/files/${fileName}` expression with a helper that handles both absolute and relative bases by prefixing `window.location.origin` when the base is relative.

Replace the current URL-building line with:

```ts
const base = process.env.REACT_APP_API_BASE_URL || '';
const absoluteBase = base.startsWith('http') ? base : `${window.location.origin}${base}`;
return `${absoluteBase}/files/${fileName}`;
```

- [ ] **Step 3: Run frontend build locally**

```bash
cd frontend
REACT_APP_API_BASE_URL=/api/v1 npm run build
```

Expected: `Compiled successfully.` and no warnings referencing `REACT_APP_API_BASE_URL`.

- [ ] **Step 4: Commit**

```bash
git add frontend/src/services/fileService.ts
git commit -m "fix(frontend): build file URLs correctly from a relative API base"
```

### Task 1.5: Backend Dockerfile — update port and add non-root user

**Files:**
- Modify: `Dockerfile`

- [ ] **Step 1: Replace `Dockerfile`**

Replace the current contents with:

```dockerfile
# syntax=docker/dockerfile:1.7

# Stage 1: build the JAR
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -B -q -DskipTests package

# Stage 2: runtime
FROM eclipse-temurin:17-jre-alpine AS runtime
RUN addgroup -S app && adduser -S -G app app
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
RUN chown -R app:app /app && mkdir -p /var/app/uploads && chown -R app:app /var/app/uploads
USER app
EXPOSE 8080
ENV SERVER_PORT=8080
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
```

- [ ] **Step 2: Build locally to verify**

```bash
docker build -t futsal-backend:local .
```

Expected: `Successfully tagged futsal-backend:local`.

- [ ] **Step 3: Smoke-run locally (optional if Docker Desktop is up)**

```bash
docker run --rm -p 8080:8080 -e SPRING_PROFILES_ACTIVE=kubernetes -e DATABASE_URL=jdbc:postgresql://host.docker.internal:5432/futsal_booking futsal-backend:local &
sleep 20
curl -fsS http://localhost:8080/actuator/health/liveness
```

Expected: `{"status":"UP"}`. Stop the container with `docker stop $(docker ps -q --filter ancestor=futsal-backend:local)`.

Skip this step if local Postgres is not available; the image-build.yml CI job catches build failures anyway.

- [ ] **Step 4: Commit**

```bash
git add Dockerfile
git commit -m "feat(docker): backend Dockerfile with non-root user, 8080 port, heap percentage"
```

---

## Phase 2 — Frontend Container Image

### Task 2.1: Create `frontend/Dockerfile` (multi-stage build + nginx serve)

**Files:**
- Create: `frontend/Dockerfile`
- Create: `frontend/nginx.conf`
- Create: `frontend/.dockerignore`

- [ ] **Step 1: Create `frontend/.dockerignore`**

```
node_modules
build
.git
.env
.env.local
.env.development
.env.test
coverage
.DS_Store
*.log
```

- [ ] **Step 2: Create `frontend/nginx.conf`**

```nginx
server {
  listen 8080;
  server_name _;
  root /usr/share/nginx/html;
  index index.html;

  # SPA: fall through to index.html for client-side routing
  location / {
    try_files $uri /index.html;
  }

  # Cache hashed static assets aggressively
  location /static/ {
    expires 1y;
    add_header Cache-Control "public, immutable";
  }

  # Do not cache the HTML entrypoint
  location = /index.html {
    add_header Cache-Control "no-store";
  }
}
```

- [ ] **Step 3: Create `frontend/Dockerfile`**

```dockerfile
# syntax=docker/dockerfile:1.7

# Stage 1: build the React production bundle
FROM node:20-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci --no-audit --no-fund
COPY . .
ARG REACT_APP_API_BASE_URL=/api/v1
ENV REACT_APP_API_BASE_URL=${REACT_APP_API_BASE_URL}
RUN npm run build

# Stage 2: serve static assets with nginx
FROM nginxinc/nginx-unprivileged:1.27-alpine AS runtime
COPY --from=build /app/build /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 8080
```

- [ ] **Step 4: Build + smoke-run**

```bash
cd frontend
docker build -t futsal-frontend:local .
docker run --rm -d -p 3000:8080 --name futsal-front futsal-frontend:local
sleep 2
curl -fsS http://localhost:3000/ | head -n 5
docker stop futsal-front
```

Expected: curl prints HTML starting with `<!doctype html>`.

- [ ] **Step 5: Commit**

```bash
git add frontend/Dockerfile frontend/nginx.conf frontend/.dockerignore
git commit -m "feat(frontend): multi-stage Dockerfile with unprivileged nginx + SPA routing"
```

---

## Phase 3 — CI Image Build Pipeline

### Task 3.1: Add `.github/workflows/image-build.yml`

**Files:**
- Create: `.github/workflows/image-build.yml`

- [ ] **Step 1: Create the workflow**

```yaml
name: Image Build

on:
  push:
    branches: [ "main", "master" ]
  pull_request:
    branches: [ "main", "master" ]
  workflow_dispatch: {}

permissions:
  contents: read
  packages: write
  security-events: write

env:
  REGISTRY: ghcr.io
  BACKEND_IMAGE: ghcr.io/${{ github.repository_owner }}/futsal-backend
  FRONTEND_IMAGE: ghcr.io/${{ github.repository_owner }}/futsal-frontend

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: maven

      - name: Backend tests
        run: ./mvnw -B test

      - name: Set up Node.js 20
        uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: npm
          cache-dependency-path: frontend/package-lock.json

      - name: Install + build frontend
        working-directory: frontend
        run: |
          npm ci
          npm run build

  build-backend:
    runs-on: ubuntu-latest
    needs: test
    if: github.event_name != 'pull_request'
    steps:
      - uses: actions/checkout@v4

      - name: Set up Docker Buildx
        uses: docker/setup-buildx-action@v3

      - name: Log in to GHCR
        uses: docker/login-action@v3
        with:
          registry: ${{ env.REGISTRY }}
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}

      - name: Build and push backend
        uses: docker/build-push-action@v5
        with:
          context: .
          file: Dockerfile
          platforms: linux/amd64
          push: true
          tags: |
            ${{ env.BACKEND_IMAGE }}:${{ github.sha }}
            ${{ env.BACKEND_IMAGE }}:latest
          cache-from: type=gha
          cache-to: type=gha,mode=max

      - name: Trivy scan
        uses: aquasecurity/trivy-action@0.24.0
        with:
          image-ref: ${{ env.BACKEND_IMAGE }}:${{ github.sha }}
          format: sarif
          output: trivy-backend.sarif
          exit-code: '0'
          severity: CRITICAL,HIGH

      - name: Upload Trivy SARIF
        uses: github/codeql-action/upload-sarif@v3
        with:
          sarif_file: trivy-backend.sarif
          category: trivy-backend

  build-frontend:
    runs-on: ubuntu-latest
    needs: test
    if: github.event_name != 'pull_request'
    steps:
      - uses: actions/checkout@v4

      - name: Set up Docker Buildx
        uses: docker/setup-buildx-action@v3

      - name: Log in to GHCR
        uses: docker/login-action@v3
        with:
          registry: ${{ env.REGISTRY }}
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}

      - name: Build and push frontend
        uses: docker/build-push-action@v5
        with:
          context: frontend
          file: frontend/Dockerfile
          platforms: linux/amd64
          push: true
          tags: |
            ${{ env.FRONTEND_IMAGE }}:${{ github.sha }}
            ${{ env.FRONTEND_IMAGE }}:latest
          build-args: |
            REACT_APP_API_BASE_URL=/api/v1
          cache-from: type=gha
          cache-to: type=gha,mode=max

      - name: Trivy scan
        uses: aquasecurity/trivy-action@0.24.0
        with:
          image-ref: ${{ env.FRONTEND_IMAGE }}:${{ github.sha }}
          format: sarif
          output: trivy-frontend.sarif
          exit-code: '0'
          severity: CRITICAL,HIGH

      - name: Upload Trivy SARIF
        uses: github/codeql-action/upload-sarif@v3
        with:
          sarif_file: trivy-frontend.sarif
          category: trivy-frontend
```

- [ ] **Step 2: Validate the workflow syntax**

Run: `gh workflow view "Image Build" --repo <GHCR_USER>/<repo> 2>/dev/null || true`

(This only works after the push; skip on local validation.) Instead, locally run `yq` or `actionlint` if installed:

```bash
command -v actionlint && actionlint .github/workflows/image-build.yml || echo "actionlint not installed; rely on push to validate"
```

- [ ] **Step 3: Commit**

```bash
git add .github/workflows/image-build.yml
git commit -m "ci: build + push backend and frontend images to GHCR with Trivy scan"
```

- [ ] **Step 4: Push and observe the first run**

Push the branch. Navigate to the Actions tab. Expected: all three jobs (`test`, `build-backend`, `build-frontend`) succeed. GHCR shows two new packages.

If build-frontend fails with "npm ci" unable to find a lockfile, verify `frontend/package-lock.json` is committed (it is, per `Glob` earlier).

- [ ] **Step 5: Make the GHCR packages public**

After first successful push:
- Open https://github.com/users/<GHCR_USER>/packages/container/futsal-backend/settings — set visibility to Public.
- Same for `futsal-frontend`.

This removes the need for an image-pull secret at skopeo time.

---

## Phase 4 — Terraform Infrastructure

All files live under `deploy/terraform/`. Resource names are prefixed `futsal-sandbox-` for identification in a shared sandbox.

### Task 4.1: Terraform skeleton (providers, versions, variables)

**Files:**
- Create: `deploy/terraform/versions.tf`
- Create: `deploy/terraform/main.tf`
- Create: `deploy/terraform/variables.tf`
- Create: `deploy/terraform/terraform.tfvars.example`
- Create: `deploy/terraform/.gitignore`

- [ ] **Step 1: Create `deploy/terraform/versions.tf`**

```hcl
terraform {
  required_version = ">= 1.6.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.60"
    }
    random = {
      source  = "hashicorp/random"
      version = "~> 3.6"
    }
  }
}
```

- [ ] **Step 2: Create `deploy/terraform/main.tf`**

```hcl
provider "aws" {
  region = var.region

  default_tags {
    tags = {
      Project     = "futsal-booking"
      Environment = "sandbox"
      ManagedBy   = "terraform"
      Owner       = var.owner_tag
    }
  }
}

data "aws_availability_zones" "available" {
  state = "available"
}

locals {
  name    = "futsal-sandbox"
  azs     = slice(data.aws_availability_zones.available.names, 0, 2)
}
```

- [ ] **Step 3: Create `deploy/terraform/variables.tf`**

```hcl
variable "region" {
  description = "AWS region for the sandbox deployment."
  type        = string
  default     = "us-east-1"
}

variable "cluster_version" {
  description = "EKS control plane version."
  type        = string
  default     = "1.30"
}

variable "node_instance_type" {
  description = "EC2 instance type for the managed node group."
  type        = string
  default     = "t3.large"
}

variable "node_desired_size" {
  description = "Desired number of worker nodes."
  type        = number
  default     = 2
}

variable "owner_tag" {
  description = "Human-readable owner tag for created resources."
  type        = string
  default     = "amrit"
}

variable "letsencrypt_email" {
  description = "Email address used for Let's Encrypt account registration. Read by bootstrap.sh, not by Terraform directly."
  type        = string
}
```

- [ ] **Step 4: Create `deploy/terraform/terraform.tfvars.example`**

```hcl
region             = "us-east-1"
cluster_version    = "1.30"
node_instance_type = "t3.large"
node_desired_size  = 2
owner_tag          = "your-name"
letsencrypt_email  = "you@example.com"
```

- [ ] **Step 5: Create `deploy/terraform/.gitignore`**

```
*.tfstate
*.tfstate.*
.terraform/
.terraform.lock.hcl
terraform.tfvars
crash.log
```

- [ ] **Step 6: Validate**

```bash
cd deploy/terraform
terraform fmt -check -recursive
terraform init -backend=false
terraform validate
```

Expected: all three commands exit 0. `terraform validate` prints `Success! The configuration is valid.`

- [ ] **Step 7: Commit**

```bash
git add deploy/terraform/versions.tf deploy/terraform/main.tf deploy/terraform/variables.tf deploy/terraform/terraform.tfvars.example deploy/terraform/.gitignore
git commit -m "feat(infra): terraform skeleton (providers, versions, variables)"
```

### Task 4.2: VPC module

**Files:**
- Create: `deploy/terraform/vpc.tf`

- [ ] **Step 1: Write `deploy/terraform/vpc.tf`**

```hcl
module "vpc" {
  source  = "terraform-aws-modules/vpc/aws"
  version = "~> 5.13"

  name = "${local.name}-vpc"
  cidr = "10.42.0.0/16"

  azs             = local.azs
  public_subnets  = ["10.42.0.0/20", "10.42.16.0/20"]
  private_subnets = ["10.42.32.0/20", "10.42.48.0/20"]

  enable_nat_gateway   = true
  single_nat_gateway   = true
  enable_dns_hostnames = true

  public_subnet_tags = {
    "kubernetes.io/role/elb" = "1"
  }

  private_subnet_tags = {
    "kubernetes.io/role/internal-elb" = "1"
  }
}
```

- [ ] **Step 2: Validate**

```bash
cd deploy/terraform
terraform init -backend=false -upgrade
terraform validate
terraform fmt -check
```

Expected: `Success!`

- [ ] **Step 3: Commit**

```bash
git add deploy/terraform/vpc.tf
git commit -m "feat(infra): VPC with public + private subnets across 2 AZs (single NAT)"
```

### Task 4.3: EKS cluster + managed node group

**Files:**
- Create: `deploy/terraform/eks.tf`

- [ ] **Step 1: Write `deploy/terraform/eks.tf`**

```hcl
module "eks" {
  source  = "terraform-aws-modules/eks/aws"
  version = "~> 20.24"

  cluster_name    = local.name
  cluster_version = var.cluster_version

  cluster_endpoint_public_access       = true
  cluster_endpoint_public_access_cidrs = ["0.0.0.0/0"] # sandbox-only

  enable_cluster_creator_admin_permissions = true

  vpc_id     = module.vpc.vpc_id
  subnet_ids = module.vpc.private_subnets

  cluster_addons = {
    coredns                = { most_recent = true }
    kube-proxy             = { most_recent = true }
    vpc-cni                = { most_recent = true }
    aws-ebs-csi-driver     = { most_recent = true }
  }

  eks_managed_node_groups = {
    default = {
      instance_types = [var.node_instance_type]
      ami_type       = "AL2_x86_64"

      min_size     = var.node_desired_size
      desired_size = var.node_desired_size
      max_size     = var.node_desired_size + 1

      iam_role_additional_policies = {
        # Nodes need ECR pull; the managed policy covers it.
        AmazonEC2ContainerRegistryReadOnly = "arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryReadOnly"
      }
    }
  }
}
```

- [ ] **Step 2: Validate**

```bash
terraform init -backend=false -upgrade
terraform validate
terraform fmt -check
```

Expected: `Success!`

- [ ] **Step 3: Commit**

```bash
git add deploy/terraform/eks.tf
git commit -m "feat(infra): EKS cluster + managed node group (2x t3.large, EBS CSI addon)"
```

### Task 4.4: ECR repositories

**Files:**
- Create: `deploy/terraform/ecr.tf`

- [ ] **Step 1: Write `deploy/terraform/ecr.tf`**

```hcl
locals {
  ecr_repos = ["futsal-backend", "futsal-frontend"]
}

resource "aws_ecr_repository" "this" {
  for_each = toset(local.ecr_repos)

  name                 = each.key
  image_tag_mutability = "IMMUTABLE"
  force_delete         = true

  image_scanning_configuration {
    scan_on_push = true
  }

  encryption_configuration {
    encryption_type = "AES256"
  }
}

resource "aws_ecr_lifecycle_policy" "this" {
  for_each = aws_ecr_repository.this

  repository = each.value.name
  policy = jsonencode({
    rules = [{
      rulePriority = 1
      description  = "Keep only the 10 most recent images"
      selection = {
        tagStatus   = "any"
        countType   = "imageCountMoreThan"
        countNumber = 10
      }
      action = { type = "expire" }
    }]
  })
}
```

- [ ] **Step 2: Validate**

```bash
terraform validate
terraform fmt -check
```

- [ ] **Step 3: Commit**

```bash
git add deploy/terraform/ecr.tf
git commit -m "feat(infra): ECR repos for backend + frontend (immutable tags, scan-on-push)"
```

### Task 4.5: Secrets Manager containers

**Files:**
- Create: `deploy/terraform/secrets.tf`

- [ ] **Step 1: Write `deploy/terraform/secrets.tf`**

Container-only — values are populated by `bootstrap.sh`. `recovery_window_in_days = 0` lets re-runs destroy-and-recreate instantly (sandbox is ephemeral; recovery windows are pointless).

```hcl
resource "aws_secretsmanager_secret" "db" {
  name                    = "/futsal/sandbox/db"
  description             = "PostgreSQL credentials for the sandbox cluster"
  recovery_window_in_days = 0
}

resource "aws_secretsmanager_secret" "jwt" {
  name                    = "/futsal/sandbox/jwt"
  description             = "JWT signing secret for the sandbox backend"
  recovery_window_in_days = 0
}

resource "aws_secretsmanager_secret" "smtp" {
  name                    = "/futsal/sandbox/smtp"
  description             = "Optional SMTP credentials for the sandbox backend"
  recovery_window_in_days = 0
}

resource "aws_secretsmanager_secret" "grafana" {
  name                    = "/futsal/sandbox/grafana"
  description             = "Grafana admin credentials"
  recovery_window_in_days = 0
}
```

- [ ] **Step 2: Validate**

```bash
terraform validate
terraform fmt -check
```

- [ ] **Step 3: Commit**

```bash
git add deploy/terraform/secrets.tf
git commit -m "feat(infra): Secrets Manager containers for db, jwt, smtp, grafana"
```

### Task 4.6: IRSA role for External Secrets Operator

**Files:**
- Create: `deploy/terraform/iam.tf`

- [ ] **Step 1: Write `deploy/terraform/iam.tf`**

```hcl
data "aws_caller_identity" "current" {}

data "aws_iam_policy_document" "eso_trust" {
  statement {
    actions = ["sts:AssumeRoleWithWebIdentity"]
    effect  = "Allow"

    principals {
      type        = "Federated"
      identifiers = [module.eks.oidc_provider_arn]
    }

    condition {
      test     = "StringEquals"
      variable = "${module.eks.oidc_provider}:sub"
      values   = ["system:serviceaccount:platform:external-secrets"]
    }

    condition {
      test     = "StringEquals"
      variable = "${module.eks.oidc_provider}:aud"
      values   = ["sts.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "eso" {
  name               = "${local.name}-eso"
  assume_role_policy = data.aws_iam_policy_document.eso_trust.json
}

data "aws_iam_policy_document" "eso_secrets_read" {
  statement {
    effect = "Allow"
    actions = [
      "secretsmanager:GetSecretValue",
      "secretsmanager:DescribeSecret",
      "secretsmanager:ListSecretVersionIds"
    ]
    resources = [
      aws_secretsmanager_secret.db.arn,
      aws_secretsmanager_secret.jwt.arn,
      aws_secretsmanager_secret.smtp.arn,
      aws_secretsmanager_secret.grafana.arn,
    ]
  }
}

resource "aws_iam_role_policy" "eso_secrets_read" {
  name   = "${local.name}-eso-read"
  role   = aws_iam_role.eso.id
  policy = data.aws_iam_policy_document.eso_secrets_read.json
}
```

- [ ] **Step 2: Validate**

```bash
terraform validate
terraform fmt -check
```

- [ ] **Step 3: Commit**

```bash
git add deploy/terraform/iam.tf
git commit -m "feat(infra): IRSA role for External Secrets Operator with scoped SM read policy"
```

### Task 4.7: Outputs

**Files:**
- Create: `deploy/terraform/outputs.tf`

- [ ] **Step 1: Write `deploy/terraform/outputs.tf`**

```hcl
output "region" {
  value = var.region
}

output "cluster_name" {
  value = module.eks.cluster_name
}

output "cluster_endpoint" {
  value = module.eks.cluster_endpoint
}

output "oidc_provider" {
  value = module.eks.oidc_provider
}

output "oidc_provider_arn" {
  value = module.eks.oidc_provider_arn
}

output "ecr_registry" {
  value = "${data.aws_caller_identity.current.account_id}.dkr.ecr.${var.region}.amazonaws.com"
}

output "ecr_backend_url" {
  value = aws_ecr_repository.this["futsal-backend"].repository_url
}

output "ecr_frontend_url" {
  value = aws_ecr_repository.this["futsal-frontend"].repository_url
}

output "secret_arn_db" {
  value = aws_secretsmanager_secret.db.arn
}

output "secret_arn_jwt" {
  value = aws_secretsmanager_secret.jwt.arn
}

output "secret_arn_smtp" {
  value = aws_secretsmanager_secret.smtp.arn
}

output "secret_arn_grafana" {
  value = aws_secretsmanager_secret.grafana.arn
}

output "secret_name_db" { value = aws_secretsmanager_secret.db.name }
output "secret_name_jwt" { value = aws_secretsmanager_secret.jwt.name }
output "secret_name_smtp" { value = aws_secretsmanager_secret.smtp.name }
output "secret_name_grafana" { value = aws_secretsmanager_secret.grafana.name }

output "eso_role_arn" {
  value = aws_iam_role.eso.arn
}
```

- [ ] **Step 2: Validate with `terraform plan` (offline-friendly)**

```bash
terraform init -backend=false -upgrade
terraform validate
terraform fmt -check
```

Expected: `Success!` (cannot `plan` without real AWS creds; validation is the guard here.)

- [ ] **Step 3: Commit**

```bash
git add deploy/terraform/outputs.tf
git commit -m "feat(infra): terraform outputs (cluster, ecr, secrets, eso role)"
```

---

## Phase 5 — Platform Helm Chart

### Task 5.1: Platform chart skeleton with dependencies

**Files:**
- Create: `deploy/helm/platform/Chart.yaml`
- Create: `deploy/helm/platform/values.yaml`
- Create: `deploy/helm/platform/.helmignore`

- [ ] **Step 1: Create `deploy/helm/platform/.helmignore`**

```
.DS_Store
.git/
.gitignore
*.swp
```

- [ ] **Step 2: Create `deploy/helm/platform/Chart.yaml`**

```yaml
apiVersion: v2
name: platform
description: Cluster add-ons for the futsal sandbox (ingress, cert-manager, ESO, postgres, observability).
type: application
version: 0.1.0
appVersion: "1.0.0"

dependencies:
  - name: ingress-nginx
    version: 4.11.3
    repository: https://kubernetes.github.io/ingress-nginx
  - name: cert-manager
    version: v1.15.3
    repository: https://charts.jetstack.io
  - name: external-secrets
    version: 0.10.4
    repository: https://charts.external-secrets.io
  - name: postgresql
    version: 15.5.38
    repository: https://charts.bitnami.com/bitnami
  - name: kube-prometheus-stack
    version: 65.1.0
    repository: https://prometheus-community.github.io/helm-charts
  - name: loki-stack
    version: 2.10.2
    repository: https://grafana.github.io/helm-charts
```

- [ ] **Step 3: Create `deploy/helm/platform/values.yaml`**

```yaml
# Values passed from bootstrap.sh via --set or --set-file:
#   esoRoleArn, lettsencryptEmail, region, grafanaAdminSecretExists (bool).

ingress-nginx:
  controller:
    service:
      annotations:
        service.beta.kubernetes.io/aws-load-balancer-type: nlb
        service.beta.kubernetes.io/aws-load-balancer-scheme: internet-facing
        service.beta.kubernetes.io/aws-load-balancer-cross-zone-load-balancing-enabled: "true"
    admissionWebhooks:
      enabled: true

cert-manager:
  installCRDs: true
  prometheus:
    enabled: false

external-secrets:
  installCRDs: true
  # ESO's SA lands in the release namespace (platform). IRSA trust in Terraform
  # is pinned to `system:serviceaccount:platform:external-secrets`.
  serviceAccount:
    name: external-secrets
    annotations:
      # Overridden at install time via --set, kept here for documentation only.
      eks.amazonaws.com/role-arn: OVERRIDE_AT_INSTALL

postgresql:
  architecture: standalone
  auth:
    existingSecret: futsal-backend-db
    secretKeys:
      adminPasswordKey: SPRING_DATASOURCE_PASSWORD
      userPasswordKey:  SPRING_DATASOURCE_PASSWORD
    username: postgres
    database: futsal_booking
  primary:
    persistence:
      enabled: true
      size: 8Gi
      storageClass: gp3
    resources:
      requests: { cpu: 250m, memory: 512Mi }
      limits:   { cpu: 1000m, memory: 1Gi }

kube-prometheus-stack:
  alertmanager:
    enabled: true
    alertmanagerSpec:
      storage:
        volumeClaimTemplate:
          spec:
            storageClassName: gp3
            accessModes: ["ReadWriteOnce"]
            resources: { requests: { storage: 1Gi } }
  prometheus:
    prometheusSpec:
      retention: 6h
      storageSpec: {}   # emptyDir
      serviceMonitorSelectorNilUsesHelmValues: false
  grafana:
    admin:
      existingSecret: grafana-admin
      userKey: admin-user
      passwordKey: admin-password
    sidecar:
      dashboards:
        enabled: true
        label: grafana_dashboard
    service:
      type: ClusterIP

loki-stack:
  loki:
    enabled: true
    persistence:
      enabled: false
  promtail:
    enabled: true
```

- [ ] **Step 4: Pull chart dependencies and lint**

```bash
cd deploy/helm/platform
helm dependency update
helm lint .
```

Expected: `1 chart(s) linted, 0 chart(s) failed`.

- [ ] **Step 5: Commit**

```bash
# Note: charts/ directory is pulled, add to .gitignore
cd ../../../
cat >> deploy/helm/platform/.helmignore <<EOF
charts/
Chart.lock
EOF
echo "deploy/helm/platform/charts/" >> .gitignore
echo "deploy/helm/platform/Chart.lock" >> .gitignore

git add deploy/helm/platform/Chart.yaml deploy/helm/platform/values.yaml deploy/helm/platform/.helmignore .gitignore
git commit -m "feat(helm): platform chart skeleton with pinned upstream dependencies"
```

### Task 5.2: Platform templates — namespaces, ClusterIssuer, ClusterSecretStore, Grafana admin

**Files:**
- Create: `deploy/helm/platform/templates/namespaces.yaml`
- Create: `deploy/helm/platform/templates/cluster-issuer.yaml`
- Create: `deploy/helm/platform/templates/cluster-secret-store.yaml`
- Create: `deploy/helm/platform/templates/grafana-admin-externalsecret.yaml`
- Create: `deploy/helm/platform/templates/postgres-db-externalsecret.yaml`
- Create: `deploy/helm/platform/templates/NOTES.txt`

- [ ] **Step 1: Create `deploy/helm/platform/templates/namespaces.yaml`**

```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: futsal
---
apiVersion: v1
kind: Namespace
metadata:
  name: ops
```

- [ ] **Step 2: Create `deploy/helm/platform/templates/cluster-issuer.yaml`**

```yaml
apiVersion: cert-manager.io/v1
kind: ClusterIssuer
metadata:
  name: letsencrypt-staging
spec:
  acme:
    server: https://acme-staging-v02.api.letsencrypt.org/directory
    email: {{ required "letsencryptEmail is required" .Values.letsencryptEmail | quote }}
    privateKeySecretRef:
      name: letsencrypt-staging-account-key
    solvers:
      - http01:
          ingress:
            class: nginx
---
apiVersion: cert-manager.io/v1
kind: ClusterIssuer
metadata:
  name: letsencrypt-prod
spec:
  acme:
    server: https://acme-v02.api.letsencrypt.org/directory
    email: {{ required "letsencryptEmail is required" .Values.letsencryptEmail | quote }}
    privateKeySecretRef:
      name: letsencrypt-prod-account-key
    solvers:
      - http01:
          ingress:
            class: nginx
```

- [ ] **Step 3: Create `deploy/helm/platform/templates/cluster-secret-store.yaml`**

```yaml
apiVersion: external-secrets.io/v1beta1
kind: ClusterSecretStore
metadata:
  name: aws-secrets-manager
spec:
  provider:
    aws:
      service: SecretsManager
      region: {{ required "region is required" .Values.region | quote }}
      auth:
        jwt:
          serviceAccountRef:
            name: external-secrets
            namespace: platform
```

- [ ] **Step 4: Create the Postgres DB ExternalSecret**

`deploy/helm/platform/templates/postgres-db-externalsecret.yaml`:

```yaml
apiVersion: external-secrets.io/v1beta1
kind: ExternalSecret
metadata:
  name: futsal-backend-db
  namespace: futsal
spec:
  refreshInterval: 1h
  secretStoreRef:
    name: aws-secrets-manager
    kind: ClusterSecretStore
  target:
    name: futsal-backend-db
    creationPolicy: Owner
  data:
    - secretKey: SPRING_DATASOURCE_USERNAME
      remoteRef:
        key: /futsal/sandbox/db
        property: username
    - secretKey: SPRING_DATASOURCE_PASSWORD
      remoteRef:
        key: /futsal/sandbox/db
        property: password
```

- [ ] **Step 5: Create the Grafana admin ExternalSecret**

`deploy/helm/platform/templates/grafana-admin-externalsecret.yaml`:

```yaml
apiVersion: external-secrets.io/v1beta1
kind: ExternalSecret
metadata:
  name: grafana-admin
  namespace: ops
spec:
  refreshInterval: 1h
  secretStoreRef:
    name: aws-secrets-manager
    kind: ClusterSecretStore
  target:
    name: grafana-admin
    creationPolicy: Owner
  data:
    - secretKey: admin-user
      remoteRef:
        key: /futsal/sandbox/grafana
        property: username
    - secretKey: admin-password
      remoteRef:
        key: /futsal/sandbox/grafana
        property: password
```

- [ ] **Step 6: Create `deploy/helm/platform/templates/NOTES.txt`**

```
Platform chart installed. Next steps:
  1. Verify ingress-nginx NLB is ready:   kubectl -n platform get svc
  2. Verify cert-manager is ready:        kubectl -n platform get pods -l app.kubernetes.io/name=cert-manager
  3. Verify ESO is ready:                 kubectl -n platform get pods -l app.kubernetes.io/name=external-secrets
  4. Install the futsal app chart:        helm upgrade --install futsal ../futsal -n futsal
```

- [ ] **Step 7: Lint and template-render**

```bash
cd deploy/helm/platform
helm lint . \
  --set region=us-east-1 \
  --set letsencryptEmail=test@example.com \
  --set esoRoleArn=arn:aws:iam::123456789012:role/fake \
  --set 'external-secrets.serviceAccount.annotations.eks\.amazonaws\.com/role-arn=arn:aws:iam::123456789012:role/fake'

helm template platform . \
  --set region=us-east-1 \
  --set letsencryptEmail=test@example.com \
  --set 'external-secrets.serviceAccount.annotations.eks\.amazonaws\.com/role-arn=arn:aws:iam::123456789012:role/fake' \
  > /tmp/platform-render.yaml

wc -l /tmp/platform-render.yaml
```

Expected: lint passes, render produces thousands of lines. (Full `kubectl --dry-run=client` validation is skipped because cert-manager / ESO CRDs aren't installed locally; that's checked at real `helm install` time.)

- [ ] **Step 8: Commit**

```bash
git add deploy/helm/platform/templates/
git commit -m "feat(helm): platform templates (namespaces, ClusterIssuer, ClusterSecretStore, ExternalSecrets)"
```

### Task 5.3: Backend Grafana dashboard (JSON)

**Files:**
- Create: `deploy/helm/platform/dashboards/futsal-backend-overview.json`
- Create: `deploy/helm/platform/templates/dashboard-configmap.yaml`

- [ ] **Step 1: Save the dashboard JSON**

Create `deploy/helm/platform/dashboards/futsal-backend-overview.json`:

```json
{
  "annotations": {"list": []},
  "editable": true,
  "panels": [
    {
      "title": "Request rate",
      "type": "timeseries",
      "targets": [
        {"expr": "sum by (uri) (rate(http_server_requests_seconds_count{namespace=\"futsal\",app=\"backend\"}[1m]))"}
      ],
      "gridPos": {"x": 0, "y": 0, "w": 12, "h": 8}
    },
    {
      "title": "p95 latency",
      "type": "timeseries",
      "targets": [
        {"expr": "histogram_quantile(0.95, sum by (le, uri) (rate(http_server_requests_seconds_bucket{namespace=\"futsal\",app=\"backend\"}[5m])))"}
      ],
      "gridPos": {"x": 12, "y": 0, "w": 12, "h": 8}
    },
    {
      "title": "JVM heap used",
      "type": "timeseries",
      "targets": [
        {"expr": "sum by (pod) (jvm_memory_used_bytes{namespace=\"futsal\",app=\"backend\",area=\"heap\"})"}
      ],
      "gridPos": {"x": 0, "y": 8, "w": 12, "h": 8}
    },
    {
      "title": "DB pool active connections",
      "type": "timeseries",
      "targets": [
        {"expr": "hikaricp_connections_active{namespace=\"futsal\",app=\"backend\"}"}
      ],
      "gridPos": {"x": 12, "y": 8, "w": 12, "h": 8}
    }
  ],
  "schemaVersion": 38,
  "title": "Futsal Backend Overview",
  "uid": "futsal-backend",
  "version": 1
}
```

- [ ] **Step 2: Wrap dashboard in a ConfigMap that the Grafana sidecar picks up**

`deploy/helm/platform/templates/dashboard-configmap.yaml`:

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: futsal-backend-dashboard
  namespace: ops
  labels:
    grafana_dashboard: "1"
data:
  futsal-backend-overview.json: |-
{{ .Files.Get "dashboards/futsal-backend-overview.json" | indent 4 }}
```

- [ ] **Step 3: Lint and render**

```bash
cd deploy/helm/platform
helm lint . \
  --set region=us-east-1 --set letsencryptEmail=test@example.com \
  --set 'external-secrets.serviceAccount.annotations.eks\.amazonaws\.com/role-arn=arn:aws:iam::123456789012:role/fake'
```

Expected: lint passes.

- [ ] **Step 4: Commit**

```bash
git add deploy/helm/platform/dashboards/futsal-backend-overview.json deploy/helm/platform/templates/dashboard-configmap.yaml
git commit -m "feat(helm): Grafana dashboard for futsal backend (request rate, p95, heap, db pool)"
```

---

## Phase 6 — Futsal App Helm Chart

All templates apply labels per `app.kubernetes.io/*` convention via a `_helpers.tpl`. The chart exposes backend + frontend behind a single Ingress.

### Task 6.1: App chart skeleton + helpers

**Files:**
- Create: `deploy/helm/futsal/Chart.yaml`
- Create: `deploy/helm/futsal/values.yaml`
- Create: `deploy/helm/futsal/templates/_helpers.tpl`
- Create: `deploy/helm/futsal/.helmignore`

- [ ] **Step 1: Create `deploy/helm/futsal/Chart.yaml`**

```yaml
apiVersion: v2
name: futsal
description: Futsal booking backend + frontend deployment for sandbox EKS.
type: application
version: 0.1.0
appVersion: "0.0.1-SNAPSHOT"
```

- [ ] **Step 2: Create `deploy/helm/futsal/.helmignore`**

```
.DS_Store
.git/
```

- [ ] **Step 3: Create `deploy/helm/futsal/values.yaml`**

```yaml
# Overridden at install time by bootstrap.sh:
#   host, backend.image.repository, backend.image.tag,
#   frontend.image.repository, frontend.image.tag,
#   clusterIssuer, corsAllowedOrigins.

host: "OVERRIDE-AT-INSTALL.nip.io"
clusterIssuer: "letsencrypt-prod"
corsAllowedOrigins: "https://OVERRIDE-AT-INSTALL.nip.io"

backend:
  replicaCount: 2
  image:
    repository: OVERRIDE
    tag: OVERRIDE
    pullPolicy: IfNotPresent
  service:
    port: 8080
  resources:
    requests: { cpu: 500m, memory: 1Gi }
    limits:   { cpu: 1000m, memory: 2Gi }
  env:
    SPRING_PROFILES_ACTIVE: kubernetes
    SPRING_DATASOURCE_URL: "jdbc:postgresql://platform-postgresql.platform.svc.cluster.local:5432/futsal_booking"
    JPA_DDL_AUTO: update
    FILE_UPLOAD_DIR: /var/app/uploads
  hpa:
    enabled: true
    minReplicas: 2
    maxReplicas: 4
    targetCPUUtilizationPercentage: 70

frontend:
  replicaCount: 2
  image:
    repository: OVERRIDE
    tag: OVERRIDE
    pullPolicy: IfNotPresent
  service:
    port: 8080
  resources:
    requests: { cpu: 50m, memory: 64Mi }
    limits:   { cpu: 200m, memory: 128Mi }

uploads:
  size: 2Gi
  storageClass: gp3

serviceMonitor:
  enabled: true
```

- [ ] **Step 4: Create `deploy/helm/futsal/templates/_helpers.tpl`**

```tpl
{{- define "futsal.labels" -}}
app.kubernetes.io/name: futsal
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
app.kubernetes.io/part-of: futsal
{{- end -}}

{{- define "futsal.backend.labels" -}}
{{ include "futsal.labels" . }}
app.kubernetes.io/component: backend
app: backend
{{- end -}}

{{- define "futsal.frontend.labels" -}}
{{ include "futsal.labels" . }}
app.kubernetes.io/component: frontend
app: frontend
{{- end -}}

{{- define "futsal.backend.selectorLabels" -}}
app.kubernetes.io/name: futsal
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/component: backend
{{- end -}}

{{- define "futsal.frontend.selectorLabels" -}}
app.kubernetes.io/name: futsal
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/component: frontend
{{- end -}}
```

- [ ] **Step 5: Lint**

```bash
cd deploy/helm/futsal
helm lint . --set backend.image.repository=example --set backend.image.tag=abc --set frontend.image.repository=example --set frontend.image.tag=abc --set host=test.nip.io --set corsAllowedOrigins=https://test.nip.io
```

Expected: lint passes (warnings about OVERRIDE values are fine; overrides happen at install).

- [ ] **Step 6: Commit**

```bash
git add deploy/helm/futsal/Chart.yaml deploy/helm/futsal/values.yaml deploy/helm/futsal/templates/_helpers.tpl deploy/helm/futsal/.helmignore
git commit -m "feat(helm): futsal app chart skeleton with helpers"
```

### Task 6.2: Backend ExternalSecret (JWT + SMTP)

**Files:**
- Create: `deploy/helm/futsal/templates/backend-externalsecret.yaml`

- [ ] **Step 1: Write the template**

```yaml
apiVersion: external-secrets.io/v1beta1
kind: ExternalSecret
metadata:
  name: futsal-backend-app
  labels:
    {{- include "futsal.backend.labels" . | nindent 4 }}
spec:
  refreshInterval: 1h
  secretStoreRef:
    name: aws-secrets-manager
    kind: ClusterSecretStore
  target:
    name: futsal-backend-app
    creationPolicy: Owner
  data:
    - secretKey: JWT_SECRET
      remoteRef: { key: /futsal/sandbox/jwt, property: secret }
    - secretKey: MAIL_HOST
      remoteRef: { key: /futsal/sandbox/smtp, property: host }
    - secretKey: MAIL_PORT
      remoteRef: { key: /futsal/sandbox/smtp, property: port }
    - secretKey: MAIL_USERNAME
      remoteRef: { key: /futsal/sandbox/smtp, property: username }
    - secretKey: MAIL_PASSWORD
      remoteRef: { key: /futsal/sandbox/smtp, property: password }
```

- [ ] **Step 2: Lint**

```bash
cd deploy/helm/futsal
helm lint . --set backend.image.repository=example --set backend.image.tag=abc --set frontend.image.repository=example --set frontend.image.tag=abc --set host=test.nip.io --set corsAllowedOrigins=https://test.nip.io
```

Expected: pass.

- [ ] **Step 3: Commit**

```bash
git add deploy/helm/futsal/templates/backend-externalsecret.yaml
git commit -m "feat(helm): futsal backend ExternalSecret (jwt + smtp)"
```

### Task 6.3: Uploads PVC

**Files:**
- Create: `deploy/helm/futsal/templates/uploads-pvc.yaml`

- [ ] **Step 1: Write the template**

```yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: futsal-uploads
  labels:
    {{- include "futsal.backend.labels" . | nindent 4 }}
spec:
  accessModes: ["ReadWriteOnce"]
  storageClassName: {{ .Values.uploads.storageClass | quote }}
  resources:
    requests:
      storage: {{ .Values.uploads.size | quote }}
```

- [ ] **Step 2: Lint**

```bash
helm lint . --set backend.image.repository=example --set backend.image.tag=abc --set frontend.image.repository=example --set frontend.image.tag=abc --set host=test.nip.io --set corsAllowedOrigins=https://test.nip.io
```

- [ ] **Step 3: Commit**

```bash
git add deploy/helm/futsal/templates/uploads-pvc.yaml
git commit -m "feat(helm): PVC for futsal uploads directory"
```

### Task 6.4: Backend Deployment + Service + HPA + PDB + ServiceMonitor

**Files:**
- Create: `deploy/helm/futsal/templates/backend-deployment.yaml`
- Create: `deploy/helm/futsal/templates/backend-service.yaml`
- Create: `deploy/helm/futsal/templates/backend-hpa.yaml`
- Create: `deploy/helm/futsal/templates/backend-pdb.yaml`
- Create: `deploy/helm/futsal/templates/backend-servicemonitor.yaml`

- [ ] **Step 1: `backend-deployment.yaml`**

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: backend
  labels:
    {{- include "futsal.backend.labels" . | nindent 4 }}
spec:
  replicas: {{ .Values.backend.replicaCount }}
  selector:
    matchLabels:
      {{- include "futsal.backend.selectorLabels" . | nindent 6 }}
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 0
  template:
    metadata:
      labels:
        {{- include "futsal.backend.labels" . | nindent 8 }}
    spec:
      securityContext:
        runAsNonRoot: true
        runAsUser: 1000
        fsGroup: 1000
        seccompProfile:
          type: RuntimeDefault
      topologySpreadConstraints:
        - maxSkew: 1
          topologyKey: kubernetes.io/hostname
          whenUnsatisfiable: ScheduleAnyway
          labelSelector:
            matchLabels:
              {{- include "futsal.backend.selectorLabels" . | nindent 14 }}
      containers:
        - name: backend
          image: "{{ .Values.backend.image.repository }}:{{ .Values.backend.image.tag }}"
          imagePullPolicy: {{ .Values.backend.image.pullPolicy }}
          ports:
            - name: http
              containerPort: 8080
          env:
            {{- range $k, $v := .Values.backend.env }}
            - name: {{ $k }}
              value: {{ $v | quote }}
            {{- end }}
            - name: CORS_ALLOWED_ORIGINS
              value: {{ .Values.corsAllowedOrigins | quote }}
          envFrom:
            - secretRef:
                name: futsal-backend-db
            - secretRef:
                name: futsal-backend-app
          volumeMounts:
            - name: uploads
              mountPath: /var/app/uploads
          livenessProbe:
            httpGet:
              path: /actuator/health/liveness
              port: http
            initialDelaySeconds: 40
            periodSeconds: 10
            failureThreshold: 6
          readinessProbe:
            httpGet:
              path: /actuator/health/readiness
              port: http
            initialDelaySeconds: 20
            periodSeconds: 5
            failureThreshold: 6
          resources:
            {{- toYaml .Values.backend.resources | nindent 12 }}
          securityContext:
            allowPrivilegeEscalation: false
            readOnlyRootFilesystem: false
            capabilities:
              drop: ["ALL"]
      volumes:
        - name: uploads
          persistentVolumeClaim:
            claimName: futsal-uploads
```

- [ ] **Step 2: `backend-service.yaml`**

```yaml
apiVersion: v1
kind: Service
metadata:
  name: backend
  labels:
    {{- include "futsal.backend.labels" . | nindent 4 }}
spec:
  type: ClusterIP
  selector:
    {{- include "futsal.backend.selectorLabels" . | nindent 4 }}
  ports:
    - name: http
      port: {{ .Values.backend.service.port }}
      targetPort: http
```

- [ ] **Step 3: `backend-hpa.yaml`**

```yaml
{{- if .Values.backend.hpa.enabled }}
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: backend
  labels:
    {{- include "futsal.backend.labels" . | nindent 4 }}
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: backend
  minReplicas: {{ .Values.backend.hpa.minReplicas }}
  maxReplicas: {{ .Values.backend.hpa.maxReplicas }}
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: {{ .Values.backend.hpa.targetCPUUtilizationPercentage }}
{{- end }}
```

- [ ] **Step 4: `backend-pdb.yaml`**

```yaml
apiVersion: policy/v1
kind: PodDisruptionBudget
metadata:
  name: backend
  labels:
    {{- include "futsal.backend.labels" . | nindent 4 }}
spec:
  minAvailable: 1
  selector:
    matchLabels:
      {{- include "futsal.backend.selectorLabels" . | nindent 6 }}
```

- [ ] **Step 5: `backend-servicemonitor.yaml`**

```yaml
{{- if .Values.serviceMonitor.enabled }}
apiVersion: monitoring.coreos.com/v1
kind: ServiceMonitor
metadata:
  name: backend
  labels:
    {{- include "futsal.backend.labels" . | nindent 4 }}
spec:
  selector:
    matchLabels:
      {{- include "futsal.backend.selectorLabels" . | nindent 6 }}
  namespaceSelector:
    matchNames: [{{ .Release.Namespace }}]
  endpoints:
    - port: http
      path: /actuator/prometheus
      interval: 30s
{{- end }}
```

- [ ] **Step 6: Lint and render**

```bash
cd deploy/helm/futsal
helm lint . --set backend.image.repository=example --set backend.image.tag=abc --set frontend.image.repository=example --set frontend.image.tag=abc --set host=test.nip.io --set corsAllowedOrigins=https://test.nip.io

helm template futsal . -n futsal \
  --set backend.image.repository=example \
  --set backend.image.tag=abc \
  --set frontend.image.repository=example \
  --set frontend.image.tag=abc \
  --set host=test.nip.io \
  --set corsAllowedOrigins=https://test.nip.io \
  | head -n 100
```

Expected: lint passes; render shows Deployment/Service/HPA/PDB/ServiceMonitor with `example:abc` image.

- [ ] **Step 7: Commit**

```bash
git add deploy/helm/futsal/templates/backend-deployment.yaml deploy/helm/futsal/templates/backend-service.yaml deploy/helm/futsal/templates/backend-hpa.yaml deploy/helm/futsal/templates/backend-pdb.yaml deploy/helm/futsal/templates/backend-servicemonitor.yaml
git commit -m "feat(helm): backend Deployment, Service, HPA, PDB, ServiceMonitor"
```

### Task 6.5: Frontend Deployment + Service

**Files:**
- Create: `deploy/helm/futsal/templates/frontend-deployment.yaml`
- Create: `deploy/helm/futsal/templates/frontend-service.yaml`

- [ ] **Step 1: `frontend-deployment.yaml`**

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: frontend
  labels:
    {{- include "futsal.frontend.labels" . | nindent 4 }}
spec:
  replicas: {{ .Values.frontend.replicaCount }}
  selector:
    matchLabels:
      {{- include "futsal.frontend.selectorLabels" . | nindent 6 }}
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 0
  template:
    metadata:
      labels:
        {{- include "futsal.frontend.labels" . | nindent 8 }}
    spec:
      securityContext:
        runAsNonRoot: true
        runAsUser: 101
        fsGroup: 101
        seccompProfile:
          type: RuntimeDefault
      topologySpreadConstraints:
        - maxSkew: 1
          topologyKey: kubernetes.io/hostname
          whenUnsatisfiable: ScheduleAnyway
          labelSelector:
            matchLabels:
              {{- include "futsal.frontend.selectorLabels" . | nindent 14 }}
      containers:
        - name: frontend
          image: "{{ .Values.frontend.image.repository }}:{{ .Values.frontend.image.tag }}"
          imagePullPolicy: {{ .Values.frontend.image.pullPolicy }}
          ports:
            - name: http
              containerPort: 8080
          livenessProbe:
            httpGet: { path: /, port: http }
            initialDelaySeconds: 5
            periodSeconds: 10
          readinessProbe:
            httpGet: { path: /, port: http }
            initialDelaySeconds: 2
            periodSeconds: 5
          resources:
            {{- toYaml .Values.frontend.resources | nindent 12 }}
          securityContext:
            allowPrivilegeEscalation: false
            readOnlyRootFilesystem: true
            capabilities:
              drop: ["ALL"]
          volumeMounts:
            - name: cache
              mountPath: /var/cache/nginx
            - name: run
              mountPath: /var/run
            - name: tmp
              mountPath: /tmp
      volumes:
        - name: cache
          emptyDir: {}
        - name: run
          emptyDir: {}
        - name: tmp
          emptyDir: {}
```

- [ ] **Step 2: `frontend-service.yaml`**

```yaml
apiVersion: v1
kind: Service
metadata:
  name: frontend
  labels:
    {{- include "futsal.frontend.labels" . | nindent 4 }}
spec:
  type: ClusterIP
  selector:
    {{- include "futsal.frontend.selectorLabels" . | nindent 4 }}
  ports:
    - name: http
      port: {{ .Values.frontend.service.port }}
      targetPort: http
```

- [ ] **Step 3: Lint**

```bash
cd deploy/helm/futsal
helm lint . --set backend.image.repository=example --set backend.image.tag=abc --set frontend.image.repository=example --set frontend.image.tag=abc --set host=test.nip.io --set corsAllowedOrigins=https://test.nip.io
```

- [ ] **Step 4: Commit**

```bash
git add deploy/helm/futsal/templates/frontend-deployment.yaml deploy/helm/futsal/templates/frontend-service.yaml
git commit -m "feat(helm): frontend Deployment + Service (unprivileged nginx, read-only rootfs)"
```

### Task 6.6: Ingress (single host, TLS, path-routed)

**Files:**
- Create: `deploy/helm/futsal/templates/ingress.yaml`

- [ ] **Step 1: Write the template**

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: futsal
  labels:
    {{- include "futsal.labels" . | nindent 4 }}
  annotations:
    cert-manager.io/cluster-issuer: {{ .Values.clusterIssuer | quote }}
    nginx.ingress.kubernetes.io/proxy-body-size: "10m"
spec:
  ingressClassName: nginx
  tls:
    - hosts: [{{ .Values.host | quote }}]
      secretName: futsal-tls
  rules:
    - host: {{ .Values.host | quote }}
      http:
        paths:
          - path: /api
            pathType: Prefix
            backend:
              service:
                name: backend
                port:
                  number: {{ .Values.backend.service.port }}
          - path: /actuator
            pathType: Prefix
            backend:
              service:
                name: backend
                port:
                  number: {{ .Values.backend.service.port }}
          - path: /
            pathType: Prefix
            backend:
              service:
                name: frontend
                port:
                  number: {{ .Values.frontend.service.port }}
```

- [ ] **Step 2: Lint and render**

```bash
cd deploy/helm/futsal
helm lint . --set backend.image.repository=example --set backend.image.tag=abc --set frontend.image.repository=example --set frontend.image.tag=abc --set host=test.nip.io --set corsAllowedOrigins=https://test.nip.io
helm template futsal . -n futsal \
  --set backend.image.repository=example --set backend.image.tag=abc \
  --set frontend.image.repository=example --set frontend.image.tag=abc \
  --set host=futsal-1-2-3-4.nip.io --set corsAllowedOrigins=https://futsal-1-2-3-4.nip.io \
  | grep -A 2 'host:'
```

Expected: output contains `host: futsal-1-2-3-4.nip.io`.

- [ ] **Step 3: Commit**

```bash
git add deploy/helm/futsal/templates/ingress.yaml
git commit -m "feat(helm): futsal Ingress with TLS + path routing (/ → frontend, /api + /actuator → backend)"
```

---

## Phase 7 — Scripts

### Task 7.1: Shared shell library

**Files:**
- Create: `scripts/_lib.sh`

- [ ] **Step 1: Write `scripts/_lib.sh`**

```bash
#!/usr/bin/env bash
# shellcheck shell=bash
# Shared helpers for futsal deploy scripts. Source this from other scripts:
#   # shellcheck source=scripts/_lib.sh
#   . "$(dirname "$0")/_lib.sh"

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
export REPO_ROOT

_start_ts=$(date +%s)

_elapsed() {
  local now
  now=$(date +%s)
  printf '%02d:%02d' $(( (now - _start_ts) / 60 )) $(( (now - _start_ts) % 60 ))
}

log()   { printf '[%s] %s\n' "$(_elapsed)" "$*"; }
warn()  { printf '[%s] WARN  %s\n' "$(_elapsed)" "$*" >&2; }
fail()  { printf '[%s] ERROR %s\n' "$(_elapsed)" "$*" >&2; exit 1; }

require_cmd() {
  for cmd in "$@"; do
    command -v "$cmd" >/dev/null 2>&1 || fail "required command not found: $cmd"
  done
}

# tfout <output_name>  — read a terraform output from deploy/terraform/.
tfout() {
  (cd "$REPO_ROOT/deploy/terraform" && terraform output -raw "$1")
}

# Wait for a kubectl condition with a timeout (seconds).
# wait_for <description> <timeout> <cmd...>
wait_for() {
  local desc="$1" timeout="$2"
  shift 2
  local deadline=$(( $(date +%s) + timeout ))
  until "$@" >/dev/null 2>&1; do
    if [ "$(date +%s)" -ge "$deadline" ]; then
      fail "timed out after ${timeout}s waiting for: $desc"
    fi
    sleep 5
  done
}
```

- [ ] **Step 2: shellcheck**

```bash
shellcheck scripts/_lib.sh
```

Expected: no output (clean).

- [ ] **Step 3: Commit**

```bash
chmod +x scripts/_lib.sh
git add scripts/_lib.sh
git commit -m "feat(scripts): shared lib (timestamped logs, tfout, wait_for, require_cmd)"
```

### Task 7.2: `scripts/precheck.sh`

**Files:**
- Create: `scripts/precheck.sh`

- [ ] **Step 1: Write `scripts/precheck.sh`**

```bash
#!/usr/bin/env bash
# shellcheck source=scripts/_lib.sh
. "$(dirname "$0")/_lib.sh"

log "precheck: verifying tools..."
require_cmd aws terraform kubectl helm skopeo jq gh

log "precheck: verifying AWS caller identity..."
aws sts get-caller-identity >/dev/null || fail "aws sts get-caller-identity failed — are sandbox credentials active?"

EXPECTED_REGION="${AWS_REGION:-us-east-1}"
CURRENT_REGION="$(aws configure get region 2>/dev/null || echo "$EXPECTED_REGION")"
[ "$CURRENT_REGION" = "$EXPECTED_REGION" ] || warn "AWS region is '$CURRENT_REGION', expected '$EXPECTED_REGION'. Continuing anyway."

log "precheck: verifying GHCR images exist for main..."
GHCR_USER="${GHCR_USER:?GHCR_USER env var not set (export the GitHub owner/user)}"
SHA="${SHA:-$(gh api "repos/{owner}/{repo}/commits/master" --jq .sha)}"
export SHA
for image in "ghcr.io/${GHCR_USER}/futsal-backend:${SHA}" "ghcr.io/${GHCR_USER}/futsal-frontend:${SHA}"; do
  docker manifest inspect "$image" >/dev/null 2>&1 \
    || skopeo inspect "docker://$image" >/dev/null 2>&1 \
    || fail "image not found: $image — check CI build status"
done

log "precheck: ok. target SHA: $SHA"
```

- [ ] **Step 2: shellcheck**

```bash
shellcheck scripts/precheck.sh
```

- [ ] **Step 3: Commit**

```bash
chmod +x scripts/precheck.sh
git add scripts/precheck.sh
git commit -m "feat(scripts): precheck.sh — verify tools, AWS creds, region, GHCR images"
```

### Task 7.3: `scripts/bootstrap.sh`

**Files:**
- Create: `scripts/bootstrap.sh`

- [ ] **Step 1: Write the script**

```bash
#!/usr/bin/env bash
# shellcheck source=scripts/_lib.sh
. "$(dirname "$0")/_lib.sh"

: "${GHCR_USER:?GHCR_USER env var required}"

log "running precheck..."
GHCR_USER="$GHCR_USER" "$(dirname "$0")/precheck.sh"
SHA="$(gh api "repos/{owner}/{repo}/commits/master" --jq .sha)"

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
```

- [ ] **Step 2: shellcheck**

```bash
shellcheck scripts/bootstrap.sh
```

Expected: no output. If shellcheck flags `{1..30}` in POSIX mode, ignore — the shebang is `#!/usr/bin/env bash`.

- [ ] **Step 3: Commit**

```bash
chmod +x scripts/bootstrap.sh
git add scripts/bootstrap.sh
git commit -m "feat(scripts): bootstrap.sh — terraform + secrets gen + platform + image mirror + app install"
```

### Task 7.4: `scripts/verify.sh`

**Files:**
- Create: `scripts/verify.sh`

- [ ] **Step 1: Write the script**

```bash
#!/usr/bin/env bash
# shellcheck source=scripts/_lib.sh
. "$(dirname "$0")/_lib.sh"

: "${HOST:?HOST env var required (public futsal hostname, e.g. futsal-1-2-3-4.nip.io)}"

BASE="https://$HOST"

log "verify: frontend serves HTML..."
curl -fsS "$BASE/" | grep -qi '<!doctype html>' || fail "frontend root did not return HTML"

log "verify: backend liveness..."
curl -fsS "$BASE/actuator/health/liveness" | grep -q '"status":"UP"' || fail "liveness not UP"

log "verify: backend readiness..."
curl -fsS "$BASE/actuator/health/readiness" | grep -q '"status":"UP"' || fail "readiness not UP"

log "verify: TLS chain..."
echo | openssl s_client -connect "${HOST}:443" -servername "$HOST" -verify_return_error >/dev/null 2>&1 \
  || fail "TLS verification failed for $HOST"

log "verify: public API shape (grounds list)..."
curl -fsS "$BASE/api/v1/grounds" >/dev/null || fail "grounds listing endpoint failed"

log "verify: running integration test suite against $BASE..."
(
  cd "$REPO_ROOT"
  ./mvnw -B -Dtest.base-url="$BASE" test -Dgroups=deployed || {
    warn "full integration suite failed against deployed URL. See output above."
    exit 1
  }
)

log "verify: all checks passed."
```

- [ ] **Step 2: shellcheck**

```bash
shellcheck scripts/verify.sh
```

- [ ] **Step 3: Commit**

```bash
chmod +x scripts/verify.sh
git add scripts/verify.sh
git commit -m "feat(scripts): verify.sh — smoke (health, TLS, public API) + integration suite against deployed URL"
```

Note: the integration suite is currently in-process (`@SpringBootTest`). The `-Dtest.base-url` override here is illustrative — if the existing tests don't accept it, `verify.sh` falls back to curl smoke checks only (the integration suite line will fail fast and print a clear message). A follow-up (out of scope) is adding a `@Tag("deployed")` http-only smoke test class that honors `test.base-url`.

### Task 7.5: `scripts/teardown.sh`

**Files:**
- Create: `scripts/teardown.sh`

- [ ] **Step 1: Write the script**

```bash
#!/usr/bin/env bash
# shellcheck source=scripts/_lib.sh
. "$(dirname "$0")/_lib.sh"

log "teardown: helm uninstall..."
helm uninstall futsal -n futsal --ignore-not-found=true || true
helm uninstall platform -n platform --ignore-not-found=true || true

log "teardown: waiting for LoadBalancer deletion (up to 2 min)..."
for _ in {1..24}; do
  if ! kubectl -n platform get svc platform-ingress-nginx-controller >/dev/null 2>&1; then
    break
  fi
  sleep 5
done

log "teardown: terraform destroy..."
(cd "$REPO_ROOT/deploy/terraform" && terraform destroy -auto-approve)

log "teardown: done. (sandbox auto-cleanup will handle any leftovers.)"
```

- [ ] **Step 2: shellcheck**

```bash
shellcheck scripts/teardown.sh
```

- [ ] **Step 3: Commit**

```bash
chmod +x scripts/teardown.sh
git add scripts/teardown.sh
git commit -m "feat(scripts): teardown.sh — helm uninstall + terraform destroy (optional early shutdown)"
```

---

## Phase 8 — Documentation

### Task 8.1: Deployment-specific README

**Files:**
- Create: `deploy/README.md`

- [ ] **Step 1: Write `deploy/README.md`**

```markdown
# AWS EKS Sandbox Deployment

Script-driven deployment of the futsal booking app to AWS EKS, tuned for a 4-hour Pluralsight sandbox. See the full design in [`docs/superpowers/specs/2026-04-25-aws-eks-sandbox-deployment-design.md`](../docs/superpowers/specs/2026-04-25-aws-eks-sandbox-deployment-design.md).

## Prerequisites

- Active Pluralsight AWS sandbox (4-hour lifetime).
- Local tools: `aws`, `terraform` >= 1.6, `kubectl`, `helm` 3, `skopeo`, `jq`, `gh`, `openssl`, `dig`, `shellcheck`.
- GitHub Actions has already built images to GHCR (see `.github/workflows/image-build.yml`).
- GHCR packages are set to **public** (one-time manual step).

## Configuration

```bash
cp deploy/terraform/terraform.tfvars.example deploy/terraform/terraform.tfvars
$EDITOR deploy/terraform/terraform.tfvars   # set letsencrypt_email at minimum
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
```

- [ ] **Step 2: Commit**

```bash
git add deploy/README.md
git commit -m "docs(deploy): runbook for sandbox EKS deployment"
```

### Task 8.2: Root README — add Deployment section + mapping table

**Files:**
- Modify: `README.md`

- [ ] **Step 1: Add a "Deployment" section**

Append the following to the end of `README.md`:

```markdown
## Deployment

The project deploys to **AWS EKS** via Terraform + Helm, driven by `scripts/bootstrap.sh`. It's designed for a 4-hour Pluralsight sandbox and reaches a running public HTTPS URL in ~30 min.

- Design: [docs/superpowers/specs/2026-04-25-aws-eks-sandbox-deployment-design.md](docs/superpowers/specs/2026-04-25-aws-eks-sandbox-deployment-design.md)
- Runbook: [deploy/README.md](deploy/README.md)
- CI image build: [.github/workflows/image-build.yml](.github/workflows/image-build.yml)

### Architecture (sandbox)

```
Browser ── HTTPS ──► NLB ──► ingress-nginx ──┬─► frontend (nginx + React build)
                                              └─► backend (Spring Boot)
                                                    │
                                                    ├─► Postgres (in-cluster, PVC)
                                                    ├─► PVC uploads
                                                    └─► Secrets (ESO ◄─ AWS Secrets Manager)
                                              Observability: Prometheus + Loki + Grafana (ops ns)
```

### Sandbox vs. Production

Every compromise is documented. The application-side pattern is identical in both columns.

| Concern | Sandbox | Real production |
|---|---|---|
| Terraform state | Local file | S3 + DynamoDB lock |
| Database | In-cluster Postgres (PVC) | RDS Multi-AZ + snapshots |
| TLS | Let's Encrypt via nip.io | ACM + Route53 on owned domain |
| Ingress | NGINX + NLB | ALB Controller or NGINX |
| Image push | GHCR → bootstrap mirrors to ECR | GitHub OIDC → ECR directly |
| Secrets population | Bootstrap script generates + writes | Platform process / sealed CI approval |
| Uploads | PVC-backed volume | S3 + presigned URLs |
| Node group | 2× t3.large on-demand | Spot + on-demand mix, multi-AZ, autoscaler |
| Observability | Prometheus/Loki emptyDir, 6h retention | Managed AMP/AMG, long retention, alerting wired |
| Grafana access | `kubectl port-forward` | SSO-gated Ingress |
| Tracing | Not installed | OTel SDK + Tempo/X-Ray |
```

- [ ] **Step 2: Commit**

```bash
git add README.md
git commit -m "docs(readme): deployment section with sandbox-vs-production mapping"
```

### Task 8.3: Retire the old deployment plan

**Files:**
- Modify: `docs/aws_eks_deployment_plan.md`

- [ ] **Step 1: Prepend a redirect banner to `docs/aws_eks_deployment_plan.md`**

Use Edit to insert the following three lines at the very top of the file (before the existing `# AWS EKS Deployment Plan` heading):

```markdown
> **Superseded.** This early planning note has been replaced by the full design at
> [docs/superpowers/specs/2026-04-25-aws-eks-sandbox-deployment-design.md](superpowers/specs/2026-04-25-aws-eks-sandbox-deployment-design.md)
> and the implementation plan at [docs/superpowers/plans/2026-04-25-aws-eks-sandbox-deployment.md](superpowers/plans/2026-04-25-aws-eks-sandbox-deployment.md).

```

- [ ] **Step 2: Commit**

```bash
git add docs/aws_eks_deployment_plan.md
git commit -m "docs: mark old aws_eks_deployment_plan.md as superseded"
```

---

## Phase 9 — End-to-End Verification (requires active sandbox)

This phase can only be run with an active Pluralsight sandbox. It is the real test of the plan.

### Task 9.1: First end-to-end run

- [ ] **Step 1: Open the Pluralsight sandbox and copy AWS credentials into the environment.**
- [ ] **Step 2: `export GHCR_USER=<your-gh-user>`**
- [ ] **Step 3: `cp deploy/terraform/terraform.tfvars.example deploy/terraform/terraform.tfvars` and set `letsencrypt_email`.**
- [ ] **Step 4: Run `./scripts/precheck.sh`. Expected: ok in < 10 s.**
- [ ] **Step 5: Run `./scripts/bootstrap.sh`. Expected: completes in ≤ 35 min, prints public URL.**
- [ ] **Step 6: Open the printed URL in a browser. Expected: frontend loads over HTTPS, login + signup work end-to-end.**
- [ ] **Step 7: `HOST=<host> ./scripts/verify.sh`. Expected: all smoke checks pass. (Integration suite may fail fast if tests don't honor `-Dtest.base-url`; not a blocker per the spec — smoke is the primary gate.)**
- [ ] **Step 8: Capture screenshots / a short screen recording for the README "Live Demo" section.**
- [ ] **Step 9 (optional): `./scripts/teardown.sh`. Expected: cluster destroyed cleanly. Commit any tuning discoveries as follow-ups.**

### Task 9.2: Post-run README additions (if time remains)

- [ ] **Step 1: Add a "Live Demo" subsection to `README.md` with an embedded screenshot/GIF of the sandbox deployment and a link to the recording.**
- [ ] **Step 2: Commit.**

```bash
git add README.md docs/screenshots/
git commit -m "docs(readme): add sandbox deployment screenshots / recording"
```

---

## Self-Review Notes

Every section of the spec maps to tasks above:

- Spec §1 goals → Phase 9 success gates.
- Spec §2 architecture → Phases 4–6.
- Spec §3 Terraform → Phase 4 (4.1–4.7).
- Spec §4 Helm workloads → Phases 5 & 6.
- Spec §5 image pipeline → Phase 3 + Task 7.3.
- Spec §6 secrets → Task 4.5 (SM containers), Task 4.6 (IRSA), Task 5.2 (ESO templates), Task 6.2 (app ExternalSecret), Task 7.3 (value population).
- Spec §7 observability → Tasks 5.1 + 5.3 + 6.4 (ServiceMonitor).
- Spec §8 scripts → Phase 7.
- Spec §9 repo structure → aggregate result.
- Spec §10 mapping table → Task 8.2.
- Spec §11 time budget → validated in Phase 9.
- Spec §12 risks → addressed in bootstrap idempotency + wait_for timeouts.
- Spec §13 follow-ups → explicitly out of scope, referenced in the deploy README.

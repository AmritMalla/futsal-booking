# System Architecture

> High-level architecture of the Futsal Arena deployed on AWS EKS with production-grade infrastructure patterns.

**Live Deployment**: [https://futsal-32-193-89-70.nip.io](https://futsal-32-193-89-70.nip.io)

![Futsal Arena App — Live on EKS](screenshots/app-homepage.png)

---

## Architecture Overview

The Futsal Arena is a full-stack web application that enables users to discover, book, and manage futsal court reservations. It is deployed as a cloud-native microservice on Amazon EKS with a fully automated infrastructure pipeline.

```mermaid
flowchart TB
    subgraph Internet
        User["👤 User Browser"]
    end

    subgraph AWS["AWS Cloud (us-east-1)"]
        subgraph VPC["VPC 10.42.0.0/16"]
            subgraph Public["Public Subnets"]
                NLB["Network Load Balancer<br>(internet-facing)"]
            end

            subgraph Private["Private Subnets (2 AZs)"]
                subgraph EKS["EKS Cluster (futsal-sandbox)"]
                    subgraph NS_Platform["Namespace: platform"]
                        Ingress["Ingress-Nginx<br>Controller"]
                        PG["PostgreSQL 16.4<br>(Bitnami)"]
                        CertMgr["cert-manager"]
                        ESO["External Secrets<br>Operator"]
                        Prom["Prometheus"]
                        Grafana["Grafana"]
                        Loki["Loki + Promtail"]
                    end

                    subgraph NS_Futsal["Namespace: futsal"]
                        FE["Frontend<br>(React + Nginx)<br>× 2 replicas"]
                        BE["Backend<br>(Spring Boot)<br>× 2 replicas"]
                        ESApp["ExternalSecret<br>(JWT, SMTP)"]
                    end
                end
            end
        end

        SM["AWS Secrets<br>Manager"]
        ECR["Amazon ECR"]
    end

    subgraph GitHub["GitHub"]
        GHCR["GHCR<br>(Container Registry)"]
        GHA["GitHub Actions<br>(CI/CD)"]
    end

    User -->|HTTPS| NLB
    NLB --> Ingress
    Ingress -->|"/api/*"| BE
    Ingress -->|"/*"| FE
    BE --> PG
    CertMgr -->|"TLS cert"| Ingress
    ESO -->|"IRSA"| SM
    ESApp -.->|"sync"| SM
    BE -.->|"envFrom"| ESApp
    GHA -->|"push images"| GHCR
    GHCR -.->|"skopeo mirror"| ECR
    ECR -.->|"pull images"| EKS
    Prom -->|"scrape /actuator/prometheus"| BE
    Grafana --> Prom
    Grafana --> Loki
```

---

## Namespace Strategy

The deployment is split into two Kubernetes namespaces with clear separation of concerns:

```mermaid
flowchart LR
    subgraph platform["📦 platform namespace"]
        direction TB
        P1["Ingress-Nginx Controller"]
        P2["cert-manager"]
        P3["External Secrets Operator"]
        P4["PostgreSQL 16.4"]
        P5["Prometheus"]
        P6["Grafana"]
        P7["Loki + Promtail"]
        P8["Alertmanager"]
        P9["ClusterSecretStore"]
        P10["ClusterIssuer<br>(Let's Encrypt)"]
    end

    subgraph futsal["📦 futsal namespace"]
        direction TB
        F1["Backend Deployment (×2)"]
        F2["Frontend Deployment (×2)"]
        F3["Ingress (TLS)"]
        F4["HPA (auto-scale 2→4)"]
        F5["PDB (minAvailable: 1)"]
        F6["ServiceMonitor"]
        F7["ExternalSecrets<br>(JWT, SMTP, DB)"]
        F8["Uploads PVC (2Gi)"]
    end

    platform -.->|"DB connection<br>Secret sync<br>TLS issuance<br>Metrics scrape"| futsal
```

| Namespace | Purpose | Managed By |
|-----------|---------|------------|
| `platform` | Shared infrastructure: ingress, database, certificates, secrets, monitoring | `platform` Helm chart |
| `futsal` | Application workloads: backend, frontend, ingress rules, autoscaling | `futsal` Helm chart |

**Why this separation?**
- Infrastructure can be upgraded independently of the application
- RBAC policies can restrict application teams to the `futsal` namespace
- Secrets are scoped per namespace with explicit cross-namespace `ExternalSecret` definitions
- Monitoring components can observe all namespaces without being affected by app deployments

---

## Request Flow

The following sequence diagram shows how a typical API request travels through the system:

```mermaid
sequenceDiagram
    actor User
    participant NLB as AWS NLB
    participant Nginx as Ingress-Nginx
    participant FE as Frontend (Nginx)
    participant BE as Backend (Spring Boot)
    participant PG as PostgreSQL

    User->>NLB: HTTPS GET https://futsal-x-x-x-x.nip.io/
    NLB->>Nginx: TCP forward (port 443)
    Nginx->>Nginx: TLS termination (Let's Encrypt cert)

    alt Static content (path: /*)
        Nginx->>FE: Proxy to frontend:8080
        FE-->>User: React SPA (HTML/JS/CSS)
    end

    User->>NLB: HTTPS POST /api/v1/bookings
    NLB->>Nginx: TCP forward
    Nginx->>BE: Proxy to backend:8080

    BE->>BE: JWT validation (Spring Security)
    BE->>PG: SQL query (HikariCP connection pool)
    PG-->>BE: Query result
    BE-->>Nginx: JSON response
    Nginx-->>NLB: Response
    NLB-->>User: HTTPS response
```

### Routing Rules

| Path | Target | Service Port | Description |
|------|--------|-------------|-------------|
| `/api/*` | Backend | 8080 | REST API endpoints |
| `/actuator/*` | Backend | 8080 | Health checks and metrics |
| `/*` | Frontend | 8080 | React SPA (catch-all) |

---

## Component Details

### Backend (Spring Boot)

- **Replicas**: 2 (min), scales to 4 via HPA at 70% CPU
- **Resources**: 500m–1000m CPU, 1Gi–2Gi memory
- **Health probes**: Liveness (`/actuator/health/liveness`), Readiness (`/actuator/health/readiness`)
- **Deployment strategy**: RollingUpdate with `maxSurge: 1`, `maxUnavailable: 0` (zero-downtime)
- **Topology**: `topologySpreadConstraints` distribute pods across nodes
- **Security**: Non-root (UID 1000), seccomp RuntimeDefault, drop ALL capabilities
- **Secrets**: Injected via `envFrom` from two Kubernetes secrets (`futsal-backend-db`, `futsal-backend-app`)
- **Persistent storage**: 2Gi EBS volume mounted at `/var/app/uploads`

### Frontend (React + Nginx)

- **Replicas**: 2 (fixed)
- **Resources**: 50m–200m CPU, 64Mi–128Mi memory
- **Container**: Read-only root filesystem with emptyDir mounts for Nginx temp dirs
- **Security**: Non-root (UID 101, nginx user), seccomp RuntimeDefault
- **Deployment strategy**: Same zero-downtime rolling update pattern

### PostgreSQL

- **Deployment**: Bitnami Helm chart (StatefulSet) in `platform` namespace
- **Storage**: 8Gi EBS gp2 persistent volume
- **Credentials**: Managed by External Secrets Operator from AWS Secrets Manager
- **Access**: Backend connects via Kubernetes DNS (`platform-postgresql.platform.svc.cluster.local`)

---

## High Availability Design

| Mechanism | Component | Configuration |
|-----------|-----------|---------------|
| **Horizontal Pod Autoscaler** | Backend | Min 2 → Max 4 replicas, target 70% CPU |
| **Pod Disruption Budget** | Backend | `minAvailable: 1` during voluntary disruptions |
| **Topology Spread** | Backend, Frontend | `maxSkew: 1` across nodes |
| **Rolling Updates** | All deployments | `maxSurge: 1, maxUnavailable: 0` |
| **Multi-AZ** | EKS nodes | Worker nodes across 2 availability zones |
| **Persistent Volumes** | PostgreSQL, Alertmanager | EBS gp2 with data durability |

---

## Helm Chart Dependency Graph

```mermaid
flowchart TD
    subgraph Platform["platform chart (umbrella)"]
        direction TB
        IngressNginx["ingress-nginx<br>v4.11.3"]
        CertManager["cert-manager<br>v1.15.3<br>(standalone install)"]
        ExtSecrets["external-secrets<br>v0.10.4<br>(standalone install)"]
        PostgreSQL["postgresql<br>(Bitnami) v15.5.38"]
        KPS["kube-prometheus-stack<br>v65.1.0<br>(standalone install)"]
        LokiStack["loki-stack<br>v2.10.2"]
    end

    subgraph Futsal["futsal chart"]
        direction TB
        Backend["backend deployment"]
        Frontend["frontend deployment"]
        AppIngress["Ingress (TLS)"]
        HPA["HPA"]
        PDB["PDB"]
        SM["ServiceMonitor"]
        ES["ExternalSecret"]
    end

    Platform -->|"provides infra for"| Futsal
    AppIngress -->|"uses"| IngressNginx
    AppIngress -->|"TLS from"| CertManager
    ES -->|"syncs from"| ExtSecrets
    Backend -->|"connects to"| PostgreSQL
    SM -->|"scraped by"| KPS
```

> **Note**: cert-manager, external-secrets, and kube-prometheus-stack are installed as **standalone Helm releases** (not as subcharts) to avoid CRD lifecycle issues. Their subchart entries in the platform chart are disabled (`enabled: false`) and only their custom resources (ClusterIssuer, ClusterSecretStore, ExternalSecrets) are managed by the platform chart.

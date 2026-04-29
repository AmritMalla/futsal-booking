# Observability Stack

> Metrics, logging, dashboarding, and alerting infrastructure for the Futsal Booking System.

---

## Live Dashboard Screenshots

### Grafana — Kubernetes Cluster Resources

Real-time cluster-wide CPU, memory, and namespace-level resource usage from our EKS deployment:

![Grafana Cluster Dashboard](screenshots/grafana-cluster-dashboard.png)

### Grafana — Node Exporter (Hardware Metrics)

Node-level hardware metrics including CPU usage, load average, and memory utilization across worker nodes:

![Grafana Node Dashboard](screenshots/grafana-node-dashboard.png)

---

## Observability Overview

The monitoring stack follows the three pillars of observability: **metrics**, **logs**, and **alerting** — all deployed within the `platform` namespace.

```mermaid
flowchart TB
    subgraph App["Application (futsal namespace)"]
        BE["Backend Pods\n/actuator/prometheus"]
        FE["Frontend Pods"]
    end

    subgraph Platform["Observability Stack (platform namespace)"]
        subgraph Metrics["Metrics Pipeline"]
            Prom["Prometheus\nServer"]
            NE["node-exporter\n(DaemonSet)"]
            KSM["kube-state-metrics"]
            SM["ServiceMonitor\n(CRD)"]
        end

        subgraph Logs["Logging Pipeline"]
            Promtail["Promtail\n(DaemonSet)"]
            Loki["Loki\n(Log Aggregation)"]
        end

        subgraph Viz["Visualization & Alerting"]
            Grafana["Grafana\n(Dashboards)"]
            AM["Alertmanager\n(1Gi persistent)"]
        end
    end

    BE -->|"scrape metrics"| SM
    SM -->|"discovered by"| Prom
    NE -->|"node metrics"| Prom
    KSM -->|"k8s object metrics"| Prom

    BE & FE -->|"stdout/stderr"| Promtail
    Promtail -->|"push logs"| Loki

    Prom -->|"datasource"| Grafana
    Loki -->|"datasource"| Grafana
    Prom -->|"alert rules"| AM
```

---

## Metrics Pipeline

### Components

| Component | Type | Purpose |
|-----------|------|---------|
| **Prometheus** | StatefulSet | Time-series database that scrapes and stores metrics |
| **node-exporter** | DaemonSet | Exports hardware and OS metrics from each node |
| **kube-state-metrics** | Deployment | Exports Kubernetes object state (pod status, replica counts, etc.) |
| **Prometheus Operator** | Deployment | Manages Prometheus instances and ServiceMonitor CRDs |

### Prometheus Configuration

| Parameter | Value | Rationale |
|-----------|-------|-----------|
| Retention | `6h` | Short retention for sandbox (saves storage costs) |
| Storage | `emptyDir` | No persistent storage (acceptable for sandbox) |
| ServiceMonitor discovery | `serviceMonitorSelectorNilUsesHelmValues: false` | Discover ServiceMonitors across **all** namespaces |

### Application Metrics (Spring Boot)

The backend exposes Prometheus-compatible metrics via Spring Boot Actuator and Micrometer:

```
GET /actuator/prometheus
```

This endpoint exports:

| Metric Category | Examples |
|----------------|---------|
| JVM | `jvm_memory_used_bytes`, `jvm_gc_pause_seconds`, `jvm_threads_current` |
| HTTP | `http_server_requests_seconds_count`, `http_server_requests_seconds_sum` |
| HikariCP | `hikaricp_connections_active`, `hikaricp_connections_idle` |
| System | `system_cpu_usage`, `process_uptime_seconds` |
| Tomcat | `tomcat_sessions_active_current`, `tomcat_threads_current` |

### ServiceMonitor

The `ServiceMonitor` CRD tells Prometheus how to scrape the backend:

```yaml
apiVersion: monitoring.coreos.com/v1
kind: ServiceMonitor
metadata:
  name: backend
  namespace: futsal
spec:
  selector:
    matchLabels:
      app.kubernetes.io/name: backend    # Target the backend Service
  namespaceSelector:
    matchNames: [futsal]                  # Look in futsal namespace
  endpoints:
    - port: http                          # Named port on the Service
      path: /actuator/prometheus          # Metrics endpoint
      interval: 30s                       # Scrape every 30 seconds
```

This is a **declarative** scrape configuration — no manual Prometheus config editing required. When the ServiceMonitor is created, the Prometheus Operator automatically reloads Prometheus to include it.

---

## Logging Pipeline

### Components

| Component | Type | Purpose |
|-----------|------|---------|
| **Loki** | StatefulSet | Log aggregation engine (like Prometheus, but for logs) |
| **Promtail** | DaemonSet | Log shipper that runs on every node and tails container logs |

### How It Works

```mermaid
flowchart LR
    subgraph Node["EKS Worker Node"]
        Container["Container\n(stdout/stderr)"]
        Kubelet["kubelet"]
        LogFile["/var/log/containers/*.log"]
        PT["Promtail\n(DaemonSet pod)"]
    end

    subgraph Platform["platform namespace"]
        L["Loki\n(port 3100)"]
    end

    Container -->|"writes to"| Kubelet
    Kubelet -->|"stores"| LogFile
    PT -->|"tails"| LogFile
    PT -->|"POST /loki/api/v1/push"| L
```

### Loki Configuration

| Parameter | Value | Rationale |
|-----------|-------|-----------|
| Persistence | Disabled | Logs are ephemeral in sandbox |
| Mode | Single-binary | Simple deployment for low-volume workloads |

Promtail automatically labels each log entry with Kubernetes metadata:

```
{namespace="futsal", pod="backend-685dd4f9b-8mgpj", container="backend"}
```

This enables powerful queries in Grafana like:

```
{namespace="futsal", container="backend"} |= "ERROR"
```

---

## Grafana Dashboards

### Architecture

Grafana uses the **sidecar pattern** for dashboard and datasource provisioning:

```mermaid
flowchart LR
    subgraph GrafanaPod["Grafana Pod (3 containers)"]
        DS["grafana-sc-datasources\n(sidecar)"]
        DB["grafana-sc-dashboard\n(sidecar)"]
        G["grafana\n(main)"]
    end

    subgraph K8s["Kubernetes Resources"]
        DSCM["ConfigMaps\nlabel: grafana_datasource=1"]
        DBCM["ConfigMaps\nlabel: grafana_dashboard=1"]
    end

    DSCM -->|"WATCH"| DS
    DS -->|"write to\n/etc/grafana/provisioning/datasources"| G
    DBCM -->|"WATCH"| DB
    DB -->|"write to\n/tmp/dashboards"| G
    DB -->|"POST /api/admin/provisioning/\ndashboards/reload"| G
```

### Sidecar Containers

| Container | Purpose | Watch Label |
|-----------|---------|-------------|
| `grafana-sc-datasources` | Auto-provisions data sources from ConfigMaps | `grafana_datasource=1` |
| `grafana-sc-dashboard` | Auto-provisions dashboards from ConfigMaps | `grafana_dashboard=1` |
| `grafana` | Main Grafana application | — |

### Pre-installed Dashboards

The kube-prometheus-stack ships with 20+ pre-built dashboards:

| Dashboard | What It Shows |
|-----------|--------------|
| Kubernetes / Compute Resources / Cluster | Cluster-wide CPU, memory, network |
| Kubernetes / Compute Resources / Namespace | Per-namespace resource usage |
| Kubernetes / Compute Resources / Pod | Individual pod metrics |
| Kubernetes / Compute Resources / Workload | Deployment/StatefulSet metrics |
| Node Exporter / Nodes | Node hardware metrics (CPU, disk, network) |
| CoreDNS | DNS query rates, latency, errors |
| Prometheus | Prometheus server self-metrics |
| Alertmanager | Alert routing and notification status |

### Data Sources

Two data sources are auto-provisioned:

| Data Source | Type | URL | Default |
|-------------|------|-----|---------|
| Prometheus | `prometheus` | `http://platform-kube-prometheus-s-prometheus.platform:9090/` | ✅ Yes |
| Loki | `loki` | `http://platform-loki:3100` | No |

### Grafana Authentication

Admin credentials are sourced from AWS Secrets Manager via External Secrets:

```
AWS Secrets Manager: /futsal/sandbox/grafana
  → ExternalSecret: grafana-admin
    → Secret keys: admin-user, admin-password
      → Grafana env: GF_SECURITY_ADMIN_USER, GF_SECURITY_ADMIN_PASSWORD
```

---

## Alerting

### Alertmanager

| Parameter | Value |
|-----------|-------|
| Enabled | Yes |
| Persistent Storage | 1Gi EBS gp2 |
| Storage Class | `gp2` |

Alertmanager receives alerts from Prometheus rules and routes them to notification channels. The persistent storage ensures alert state (silences, inhibitions) survives pod restarts.

### Built-in Alert Rules

The kube-prometheus-stack installs 35+ PrometheusRule resources covering:

| Category | Example Alerts |
|----------|---------------|
| Node | `NodeFilesystemAlmostOutOfSpace`, `NodeHighCpuLoad` |
| Pod | `KubePodCrashLooping`, `KubePodNotReady` |
| Deployment | `KubeDeploymentReplicasMismatch` |
| Prometheus | `PrometheusTSDBCompactionsFailing` |
| etcd | `etcdHighCommitDurations` |

---

## Metrics Collection Architecture

```mermaid
flowchart TB
    subgraph Targets["Scrape Targets"]
        BE["/actuator/prometheus\n(backend pods)"]
        NE["node-exporter\n:9100/metrics"]
        KSM["kube-state-metrics\n:8080/metrics"]
        KL["kubelet\n:10250/metrics"]
        CM["cert-manager\n(when enabled)"]
    end

    subgraph Prom["Prometheus Server"]
        Scrape["Scrape Engine\n(interval: 30s)"]
        TSDB["Time Series DB\n(retention: 6h)"]
        Rules["Alert Rules\n(35+ rules)"]
    end

    subgraph Outputs["Outputs"]
        Grafana["Grafana\n(PromQL queries)"]
        AM["Alertmanager\n(alert routing)"]
    end

    BE & NE & KSM & KL & CM --> Scrape
    Scrape --> TSDB
    TSDB --> Grafana
    Rules --> AM
```

---

## Key Design Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Prometheus retention | 6 hours | Sandbox: minimize storage costs, sufficient for debugging |
| Prometheus storage | emptyDir | No persistent data needed for short-lived sandbox |
| Alertmanager storage | 1Gi gp2 PVC | Alert state should survive restarts |
| Loki persistence | Disabled | Logs are ephemeral for sandbox workloads |
| Dashboard provisioning | Sidecar pattern | GitOps-friendly, no manual UI configuration |
| Datasource conflict fix | Loki `isDefault: false` | Only one default datasource allowed per Grafana org |
| ServiceMonitor namespace | All namespaces | `serviceMonitorSelectorNilUsesHelmValues: false` |
| Standalone install | kube-prometheus-stack | Avoid CRD lifecycle conflicts with umbrella chart |

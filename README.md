# Futsal Arena

A full-stack futsal arena platform built with Spring Boot and React for players, ground owners, and administrators. The project now covers the full booking lifecycle plus a standout social layer: players can publish confirmed bookings as open pickup games and let others discover and join them.

## Why This Project

Booking a futsal ground is often fragmented across phone calls, chat messages, and manual scheduling. This project turns that process into a structured platform where:

- players can discover grounds, review details, and book slots
- owners can manage grounds, slots, bookings, and revenue-related workflows
- admins can oversee bookings, payments, reviews, and platform analytics

## Highlights

- JWT-based authentication with role-aware routes and APIs
- Customer, owner, and admin user journeys
- Ground listing and detail views
- Time-slot based booking flow
- Open match / pickup game matchmaking
- Payment records, payment history, and refund handling
- Review system for grounds
- Owner dashboards for managing grounds and slots
- Admin dashboard with booking, payment, review, and analytics views
- Hardened ownership and authorization checks across booking, payment, reviews, reports, grounds, slots, and files
- Backend integration test coverage for auth, booking, payment, reviews, owner/admin access, analytics, reports, files, and open matches
- Swagger/OpenAPI documentation
- Dockerized backend + PostgreSQL local setup
- CI workflow for backend tests and frontend production build

## Product Walkthrough

### Core User Flows

1. A user browses available futsal grounds and opens a ground detail page.
2. The user selects an available time slot and creates a booking.
3. The user completes a payment flow and can later review booking/payment history.
4. A user can publish a confirmed booking as an open match so other players can join.
5. An owner manages grounds and time slots from the dashboard.
6. An admin monitors overall platform activity from the admin console.

### Roles

- `USER`: browses grounds, books slots, pays, and leaves reviews
- `OWNER`: manages futsal companies, grounds, slots, and owner-side operations
- `ADMIN`: monitors and manages platform-wide activity

## Architecture

### Backend

- Spring Boot 3
- Spring Security + JWT
- Spring Data JPA / Hibernate
- PostgreSQL
- Spring Validation
- Spring Mail
- Springdoc OpenAPI

### Frontend

- React 18
- TypeScript
- Material UI
- React Router
- Axios
- Recharts

### Deployment Shape

- Backend API runs on `8090`
- Frontend dev server runs on `3000`
- PostgreSQL runs on `5432`
- Planned production deployment target: AWS EKS

## Screens and Docs

Existing project assets:

- Architecture diagram: [docs/uml_diagram.png](docs/uml_diagram.png)
- UI/project image: [docs/img.png](docs/img.png)
- API collection: [docs/futsal_test_postman.json](docs/futsal_test_postman.json)
- AWS EKS deployment handoff: [docs/aws_eks_deployment_plan.md](docs/aws_eks_deployment_plan.md)
- Frontend guide: [FRONTEND_GUIDE.md](FRONTEND_GUIDE.md)
- Roadmap: [docs/portfolio_improvement_roadmap.md](docs/portfolio_improvement_roadmap.md)

## Repository Structure

```text
.
├── backend/            # Spring Boot backend, Dockerfile, tests
├── frontend/           # React + TypeScript frontend
├── infra/              # Terraform, Helm, and K8s assets
├── docs/               # Architecture diagrams, API collections, planning docs
├── observability/      # Grafana dashboards and monitoring configs
├── scripts/            # Bootstrap, teardown, and utility scripts
└── .github/workflows/  # CI/CD pipeline definitions
```

## Running Locally

### Prerequisites

- Java 17
- Maven or the included Maven wrapper
- Node.js 18+ recommended for the frontend
- PostgreSQL 15+ or Docker Desktop

### Option 1: Run with Docker for the Backend Stack

This starts the Spring Boot backend and PostgreSQL database.

```bash
cd backend
docker-compose up --build
```

Backend API:

```text
http://localhost:8090
```

Swagger UI:

```text
http://localhost:8090/swagger-ui.html
```

### Option 2: Run Backend Manually

1. Go to the backend directory: `cd backend`
2. Copy `.env.example` to `.env` and adjust values if needed.
3. Make sure PostgreSQL is running and a database named `futsal_booking` exists.
4. Start the backend:

```bash
./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

Backend API:

```text
http://localhost:8090
```

Swagger UI:

```text
http://localhost:8090/swagger-ui.html
```

### Run the Frontend

```bash
cd frontend
npm install
npm start
```

Frontend app:

```text
http://localhost:3000
```

The frontend is configured to talk to the backend on `http://localhost:8090`.

## Environment Configuration

The backend uses environment variables defined in [backend/.env.example](backend/.env.example).

Important values:

- `SERVER_PORT=8090`
- `DATABASE_URL=jdbc:postgresql://localhost:5432/futsal_booking`
- `DATABASE_USERNAME=postgres`
- `DATABASE_PASSWORD=postgres`
- `JWT_SECRET=your-super-secret-jwt-key-at-least-32-characters-long`
- `CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:8080`

The frontend uses its own environment file in [frontend/.env.example](frontend/.env.example).

## Sample Data and Demo Accounts

The repository includes a large sample dataset in [backend/src/main/resources/data.sql](backend/src/main/resources/data.sql) with:

- 25 users
- 7 futsal companies
- 18 grounds
- 150+ time slots
- 45 bookings
- 45 payments
- 30 reviews

Important note:

- `data.sql` is available as sample seed data, but it may not load automatically in every environment because SQL initialization behavior depends on the active Spring configuration. If you want demo accounts locally, import it manually or enable SQL init for your setup.

Default password in the sample dataset:

```text
password123
```

Example seeded accounts:

- Admin: `admin@futsal.com`
- Owner: `rajesh.owner@futsal.com`
- User: `amit@example.com`

## API Documentation

Swagger/OpenAPI is enabled through Springdoc.

- Swagger UI: `http://localhost:8090/swagger-ui.html`
- API docs JSON: `http://localhost:8090/api-docs`

A Postman collection is available at [docs/futsal_test_postman.json](docs/futsal_test_postman.json).

## Engineering Notes

### What's Implemented Well

- clear separation between backend and frontend
- role-based navigation and protected routes
- modular service/controller/repository backend organization
- Docker support for backend + database
- admin and owner workflows beyond basic CRUD
- strong ownership enforcement instead of trusting client-submitted user IDs
- meaningful automated coverage around the highest-risk business flows
- a standout open-match feature that pushes the product beyond a basic booking app

### Current Limitations

- payment processing is currently simulated rather than integrated with a real gateway
- the project does not yet include a public deployment link
- screenshot assets still need to be added to the README
- Kubernetes manifests and the AWS EKS deployment pipeline are not implemented yet

### Current Improvement Focus

The active roadmap prioritizes:

1. deployment and release readiness
2. AWS EKS deployment pipeline
3. portfolio screenshots and live demo documentation
4. optional payment gateway integration

## Commands

Backend test:

```powershell
cd backend
.\mvnw.cmd test
```

Frontend production build:

```bash
cd frontend
npm run build
```

## Interview Talking Points

If you use this project in your portfolio, the strongest discussion areas are:

- designing a multi-role booking platform
- modeling bookings, slots, payments, and reviews
- adding a matchmaking layer on top of a traditional booking domain
- securing APIs with JWT and role-based authorization
- enforcing ownership rules server-side instead of trusting client input
- building test coverage around business-critical and role-protected flows
- building admin and owner workflows in addition to end-user flows
- improving an existing project from feature-complete to portfolio-ready

## Next Steps

This repository is currently being improved phase by phase. The active plan lives here:

- [docs/portfolio_improvement_roadmap.md](docs/portfolio_improvement_roadmap.md)

The immediate focus is Phase 5:

- prepare AWS EKS deployment assets and pipeline
- document production environment variables and deployment flow
- add screenshots and, later, a public demo link

## Deployment

The project deploys to **AWS EKS** via Terraform + Helm, driven by `scripts/bootstrap.sh`. It's designed for a 4-hour Pluralsight sandbox and reaches a running public HTTPS URL in ~30 min.

- Design: [docs/superpowers/specs/2026-04-25-aws-eks-sandbox-deployment-design.md](docs/superpowers/specs/2026-04-25-aws-eks-sandbox-deployment-design.md)
- Runbook: [infra/README.md](infra/README.md)
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

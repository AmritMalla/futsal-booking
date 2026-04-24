# AWS EKS Deployment Plan

This note captures the intended production deployment direction for the futsal booking project. The current application is ready for deployment planning, but the Kubernetes manifests and CI/CD pipeline still need to be added.

## Target Shape

- `frontend`: React production build served as a containerized web app
- `backend`: Spring Boot API container
- `database`: managed PostgreSQL instance outside the cluster, preferably Amazon RDS
- `cluster`: Amazon EKS
- `ingress`: AWS Load Balancer Controller or NGINX Ingress
- `secrets`: Kubernetes secrets backed by GitHub Actions secrets, AWS Secrets Manager, or both

## Recommended Deployment Flow

1. Build backend and frontend artifacts in GitHub Actions.
2. Build and tag container images.
3. Push images to Amazon ECR.
4. Deploy to EKS with environment-specific manifests or Helm values.
5. Run smoke checks against the deployed frontend and backend health endpoints.

## Minimum Infrastructure Checklist

- EKS cluster
- ECR repositories for backend and frontend images
- RDS PostgreSQL instance
- Ingress/load balancer setup
- Domain and TLS certificate
- Environment secrets for:
  - database connection
  - JWT secret
  - CORS allowed origins
  - email configuration, if used

## Application Configuration To Prepare

- production `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`
- production `JWT_SECRET`
- production `CORS_ALLOWED_ORIGINS`
- container image tags and rollout strategy
- backend health endpoint exposure for readiness/liveness checks

## Suggested Repo Additions

- `deploy/eks/backend/`
- `deploy/eks/frontend/`
- `deploy/eks/base/`
- optional `helm/` chart if you prefer Helm over raw manifests
- GitHub Actions workflow for build, push, and deploy

## Rollout Order

1. Add Docker support for the frontend if it is not already containerized.
2. Create Kubernetes manifests or Helm charts for backend and frontend.
3. Move PostgreSQL to RDS rather than running the database inside EKS.
4. Add GitHub Actions jobs for:
   - backend test
   - frontend build
   - image build
   - ECR push
   - EKS deploy
5. Document the production deployment steps in the root README once the pipeline exists.

## Done Criteria

- a push to the deployment branch can build and publish images
- EKS can roll out backend and frontend successfully
- the README includes the live URL and deployment notes

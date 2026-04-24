# Futsal Booking System

A full-stack futsal booking platform built with Spring Boot and React for players, ground owners, and administrators. The project focuses on the complete booking lifecycle: ground discovery, slot reservation, payment tracking, reviews, owner operations, and admin analytics.

This repository is being actively improved as a portfolio project. The current roadmap is documented in [docs/portfolio_improvement_roadmap.md](docs/portfolio_improvement_roadmap.md).

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
- Payment records, payment history, and refund handling
- Review system for grounds
- Owner dashboards for managing grounds and slots
- Admin dashboard with booking, payment, review, and analytics views
- Swagger/OpenAPI documentation
- Dockerized backend + PostgreSQL local setup

## Product Walkthrough

### Core User Flows

1. A user browses available futsal grounds and opens a ground detail page.
2. The user selects an available time slot and creates a booking.
3. The user completes a payment flow and can later review booking/payment history.
4. An owner manages grounds and time slots from the dashboard.
5. An admin monitors overall platform activity from the admin console.

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

## Screens and Docs

Existing project assets:

- Architecture diagram: [docs/uml_diagram.png](docs/uml_diagram.png)
- UI/project image: [docs/img.png](docs/img.png)
- API collection: [docs/futsal_test_postman.json](docs/futsal_test_postman.json)
- Frontend guide: [FRONTEND_GUIDE.md](FRONTEND_GUIDE.md)
- Roadmap: [docs/portfolio_improvement_roadmap.md](docs/portfolio_improvement_roadmap.md)

## Repository Structure

```text
.
|-- src/main/java/com/amrit/futsal      # Spring Boot backend
|-- src/main/resources                  # application config + sample data
|-- src/test/java                       # backend tests
|-- frontend                            # React + TypeScript frontend
|-- docs                                # diagrams, collection, planning docs
|-- docker-compose.yml                  # backend + postgres local stack
|-- Dockerfile                          # backend container image
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

1. Copy `.env.example` to `.env` and adjust values if needed.
2. Make sure PostgreSQL is running and a database named `futsal_booking` exists.
3. Start the backend:

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

The backend uses environment variables defined in [.env.example](.env.example).

Important values:

- `SERVER_PORT=8090`
- `DATABASE_URL=jdbc:postgresql://localhost:5432/futsal_booking`
- `DATABASE_USERNAME=postgres`
- `DATABASE_PASSWORD=postgres`
- `JWT_SECRET=your-super-secret-jwt-key-at-least-32-characters-long`
- `CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:8080`

The frontend uses its own environment file in [frontend/.env.example](frontend/.env.example).

## Sample Data and Demo Accounts

The repository includes a large sample dataset in [src/main/resources/data.sql](src/main/resources/data.sql) with:

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

### Current Limitations

- payment processing is currently simulated rather than integrated with a real gateway
- backend service-level and security tests are in place, but broader integration coverage is still being expanded
- frontend production build succeeds, but there are still lint warnings to clean up
- the project does not yet include a public deployment link

### Current Improvement Focus

The active roadmap prioritizes:

1. completed portfolio packaging and documentation
2. automated tests for core flows
3. frontend quality cleanup
4. CI/CD and deployment
5. a standout feature such as team formation / join-a-game

## Commands

Backend test:

```powershell
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
- securing APIs with JWT and role-based authorization
- building admin and owner workflows in addition to end-user flows
- improving an existing project from feature-complete to portfolio-ready

## Next Steps

This repository is currently being improved phase by phase. The active plan lives here:

- [docs/portfolio_improvement_roadmap.md](docs/portfolio_improvement_roadmap.md)

The immediate focus is Phase 3:

- backend integration tests for auth and booking flows
- stronger automated coverage around payments and role-protected endpoints
- continued frontend cleanup after the core backend test foundation is in place

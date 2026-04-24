# Portfolio Improvement Roadmap

## Goal

Turn the futsal booking system into a portfolio project that feels credible to recruiters, easy to evaluate, and strong enough to discuss in interviews as a real product rather than a classroom CRUD app.

## Current Status

The roadmap has progressed substantially:

- Phase 1 is complete except for attaching final screenshots to the README
- Phase 2 is complete
- Phase 3 is complete
- Phase 4 is substantially complete for the current scope
- Phase 6 is complete with the open-match / pickup-game workflow
- Phase 5 is now the active focus, with AWS EKS planned as the deployment target

This roadmap prioritizes the highest-impact work first:

1. Make the project understandable and demoable
2. Strengthen reliability, security, and correctness
3. Improve engineering maturity and deployment story
4. Add one standout feature that makes the project memorable

## Current Assessment

The project already has a strong base:

- Spring Boot backend with JWT authentication, Swagger, Docker, file uploads, analytics, payments, and role-based flows
- React + TypeScript frontend with customer, owner, and admin experiences
- Existing docs, seeded resources, and a CI workflow

The biggest remaining gaps for portfolio use are:

- There is no public deployment yet
- The AWS EKS deployment pipeline still needs to be implemented
- The README still needs final screenshots and, later, a live demo URL
- Payment flow is still simulated

## Prioritization Principles

When deciding what to build next, prefer items that:

- increase trust in the system
- improve demo quality
- are easy to explain in interviews
- show professional engineering habits

## Roadmap Overview

| Priority | Area | Why it matters | Outcome |
| --- | --- | --- | --- |
| P0 | Portfolio packaging and documentation | Recruiters judge fast; poor presentation can hide good work | Clear project story, screenshots, setup, architecture, demo path |
| P1 | Security, authorization, and booking correctness | Strongest signal of engineering maturity | Safer API boundaries and more credible domain logic |
| P1 | Test coverage for core flows | Shows confidence and prevents regressions | Automated validation for auth, booking, payments |
| P2 | Frontend polish and quality cleanup | Improves demo impression | Cleaner UI behavior and no obvious warning debt |
| P2 | CI/CD and deployment | Shows production thinking | Public demo + automated checks |
| P3 | Standout feature | Makes the project memorable | Team matchmaking / join-game workflow |

## Phase 1: Portfolio Packaging and Demo Readiness

### Objective

Make the project easy to understand within 1-2 minutes.

### Tasks

- Replace the root `README.md` with a proper portfolio-oriented README
- Add a short product pitch and problem statement
- Add architecture and feature overview
- Add setup instructions for backend, frontend, database, and Docker
- Add screenshots or short GIFs for:
  - landing page
  - ground discovery
  - booking flow
  - owner dashboard
  - admin dashboard
- Add demo accounts and seeded data instructions
- Add a "Key Engineering Decisions" section
- Add a "Known Limitations / Future Improvements" section
- Add API usage notes and Swagger link
- Link related docs from the root README instead of leaving them isolated in `docs/`

### Deliverables

- New root README
- Screenshot assets under `docs/` or a dedicated `docs/screenshots/` folder
- Clear local run instructions

### Success Criteria

- A reviewer can understand the product and run it without guessing
- A recruiter can skim the README and know why this project is worth opening

## Phase 2: Security, Authorization, and Core Domain Hardening

### Objective

Remove the biggest "works for demo, but not safe in real life" weaknesses.

### Tasks

- Stop trusting `userId` from booking and payment request bodies
- Derive the current user from the authenticated JWT context on the server
- Enforce resource ownership:
  - users can only view and manage their own bookings/payments
  - owners can only manage their own grounds and slots
  - admins have elevated visibility by policy
- Review public routes in security configuration
- Restrict or secure `/actuator/**` in non-development environments
- Validate file access and upload behavior more carefully
- Ensure booking status changes and payment status changes stay consistent
- Review double-booking protection and concurrency behavior
- Add proper validation for invalid state transitions

### Deliverables

- Safer request models and service-layer ownership checks
- Stronger security configuration
- Cleaner authorization story for interview discussion

### Success Criteria

- Sensitive actions do not rely on client-submitted ownership fields
- Cross-user access attempts are rejected consistently
- Booking and payment flows behave predictably under edge cases

## Phase 3: Automated Tests for Core Business Flows

### Objective

Back up critical behavior with tests that prove the app is reliable.

### Tasks

- Add backend integration tests for:
  - authentication and registration
  - booking creation
  - slot already booked behavior
  - booking cancellation
  - payment success/failure/refund
  - admin-only endpoint access
  - owner-only resource management
- Add unit tests for business logic that has branching
- Introduce test fixtures/builders for readable setup
- Consider using a dedicated test profile with isolated database config
- Add frontend tests for a few high-value flows if time allows:
  - protected routes
  - login state handling
  - booking submission states

### Deliverables

- A meaningful backend test suite
- Optional frontend smoke tests for critical UI behavior

### Success Criteria

- Test suite covers the core user journey end to end
- Regressions in booking and authorization logic are caught automatically

### Status

Completed. The backend now has broad integration coverage around auth, booking, payments, reviews, owner/admin access, analytics, reports, files, and open matches.

## Phase 4: Frontend Quality and Product Polish

### Objective

Make the app feel more intentional and less like a stitched-together feature set.

### Tasks

- Clean all current frontend lint warnings
- Fix missing React hook dependencies and unused imports
- Improve loading, empty, and error states
- Replace placeholder or hardcoded stats with live data or clearly seeded demo data
- Improve 404 and unauthorized pages
- Review mobile responsiveness across key screens
- Add success toasts and better user feedback around booking/payment actions
- Review accessibility basics:
  - form labels
  - button clarity
  - keyboard navigation
  - color contrast

### Deliverables

- Warning-free frontend build
- More polished, resilient UI

### Success Criteria

- The app feels stable during a live demo
- UI states are handled cleanly even when APIs fail or data is missing

### Status

Largely complete for the current target. The frontend production build is clean, warnings were removed, and the new standout feature has visible product-level polish in the booking, ground, matches, and homepage flows.

## Phase 5: Engineering Maturity, CI/CD, and Deployment

### Objective

Show that the project is maintainable and deployable.

### Tasks

- Expand GitHub Actions:
  - backend test job
  - frontend install/build job
  - optional lint job
  - optional Docker build verification
- Improve environment configuration docs
- Separate development and production concerns more clearly
- Add deployment documentation for:
  - backend
  - frontend
  - PostgreSQL
  - environment variables
- Prepare AWS EKS deployment assets and pipeline
- Push container images to Amazon ECR
- Deploy the project publicly
- Add health check and deployment URLs to the README
- Consider adding a release checklist for demo readiness

### Deliverables

- Improved CI workflow
- Repeatable deployment story
- AWS EKS deployment plan and pipeline
- Public demo URL

### Success Criteria

- Every push gets meaningful automated validation
- A reviewer can access a live version without local setup

### Status

Active phase. CI has improved, but deployment work is still open. The planned production target is AWS EKS.

## Phase 6: Standout Feature for Differentiation

### Objective

Add one feature that makes the project more memorable than a standard booking app.

### Recommended Direction

Implement a team formation / join-a-game workflow:

- players can create an open game
- players can join an existing game with available spots
- owners can optionally promote open games at their grounds
- the system can show required player count, time, venue, and skill level

### Why this is the best differentiator

- It is aligned with the domain
- It is already hinted at in the project vision
- It adds product thinking, not just CRUD expansion
- It gives you a strong interview story around modeling, coordination, and user experience

### Success Criteria

- The feature feels integrated into the booking domain
- It adds a real social/product layer instead of just another admin screen

### Status

Completed. Users can now publish confirmed bookings as open matches, browse pickup games, and join or leave them from the public matches page and related booking/ground screens.

## Recommended Execution Order

### Must do before listing on your profile

- Phase 1
- Phase 2
- Phase 3

### Strongly recommended next

- Phase 5
- screenshot completion for the README

## First Working Sprint

This is the best first implementation batch because it improves both portfolio value and code quality quickly.

### Sprint 1 Scope

- Rewrite root README
- Clean frontend build warnings
- Stop trusting `userId` in booking/payment requests
- Add backend integration tests for auth and booking conflict prevention

### Why start here

- The README improves portfolio presentation immediately
- Warning cleanup improves perceived quality
- Auth/ownership hardening improves credibility
- Tests create a safety net before larger refactors

## Suggested Task Breakdown

1. Rewrite documentation and capture screenshots
2. Refactor authentication-bound request handling
3. Add test infrastructure and first integration tests
4. Clean frontend warnings and verify production build
5. Expand CI to run the new checks
6. Prepare deployment and public demo
7. Build the standout feature

## Definition of "Portfolio Ready"

The project is ready to feature prominently when:

- the README tells a clear product and engineering story
- the app can be run locally without guesswork
- core business flows are tested
- auth and ownership rules are trustworthy
- frontend builds cleanly
- CI validates the main code paths
- a live demo is available

## Immediate Next Step

Move into deployment readiness:

- create AWS EKS deployment manifests or Helm charts
- extend GitHub Actions to build, tag, and push images to Amazon ECR
- deploy frontend and backend to EKS with managed PostgreSQL
- attach screenshots and then add the live demo URL to the README

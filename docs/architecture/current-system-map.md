# Current System Map

## Objective

This document is the fast-path reference for understanding the CRM as it exists today. It describes the current product shape, build flow, runtime flow, deploy flow, governance boundary and the main technical constraints that still matter when changing the system.

## System Summary

- Product type: brownfield CRM monolith with a static web dashboard
- Backend: Spring Boot application that owns API, authentication, persistence, metrics and N8N integration
- Frontend: Next.js static export consumed as runtime assets by Spring Boot
- Runtime database: PostgreSQL
- Test database: H2 with PostgreSQL compatibility mode
- Delivery model: Dockerized Spring Boot + PostgreSQL behind Caddy

## Runtime Modules

### Backend Application

Location: `src/main/java/com/synkra/crm`

- `config/`: infrastructure wiring, security, runtime bootstrap and integration support
- `controller/`: API endpoints plus forwards for dashboard and login pages
- `dto/`: request and response contracts for HTTP traffic
- `model/`: JPA entities for CRM records, application users and webhook deliveries
- `repository/`: Spring Data repositories and aggregation queries
- `service/`: CRM orchestration and webhook delivery behavior

### Frontend Application

Location: `frontend/`

- Next.js App Router project with React and Tailwind CSS
- Uses authenticated browser calls to `/api/**`
- Built as static export with `basePath=/ui`
- Published into Spring Boot at `src/main/resources/static/ui/`

### Static Runtime Assets

Location: `src/main/resources/static/`

- `ui/`: exported dashboard bundle served by Spring Boot
- `auth/`: custom login page and stylesheet served directly by Spring Boot

### Persistence and Migrations

Location: `src/main/resources/db/migration/`

- Flyway migrations define runtime schema evolution
- PostgreSQL is the production-oriented runtime database
- H2 is reserved for automated tests only

### Deploy Assets

Location: `deploy/`

- `docker-compose.yml`: PostgreSQL + packaged Spring Boot runtime
- `Caddyfile`: reverse proxy example for public exposure
- `.env.example`: deployment variable template

## Main Runtime Flows

### User Access

1. The user accesses `/login`.
2. Spring Security authenticates against persisted application users.
3. After login, `/` forwards to `/ui/index.html`.
4. The dashboard fetches CRM data and metrics through authenticated `/api/**` endpoints.

### CRM Record Lifecycle

1. The frontend submits contact, deal or activity creation requests to `/api/**`.
2. Controllers validate request DTOs and delegate to `CrmService`.
3. `CrmService` persists domain entities through Spring Data repositories.
4. Response DTOs are returned to the client.
5. After commit, N8N delivery is recorded and dispatched through the webhook delivery pipeline.

### Dashboard Metrics

1. The frontend calls `/api/dashboard/metrics`.
2. Repository-level aggregate queries compute counts, sums and grouped metrics.
3. The service assembles the dashboard response without loading the entire dataset into memory for the core aggregates.

### Webhook Delivery

1. Successful CRM writes enqueue a persistent webhook delivery record.
2. The application attempts immediate delivery when a webhook URL is configured.
3. Failed deliveries are persisted with error details and scheduled for retry.
4. Retry processing continues until success or the configured retry limit is reached.

## Build and Packaging Flow

### Frontend Build

Command path:

1. `npm run build` inside `frontend/`
2. Next.js generates static export output
3. `frontend/scripts/copy-to-spring.mjs` replaces `src/main/resources/static/ui/`

### Java Build

- Maven is the authoritative packaging entrypoint
- The build invokes the frontend export step before packaging runtime assets
- The packaged JAR contains backend code plus the current `static/ui/` export

## Deploy Flow

1. Build the frontend and Java application locally or in CI.
2. Publish the generated JAR beside `deploy/docker-compose.yml`.
3. Fill `deploy/.env` from `deploy/.env.example`.
4. Start PostgreSQL and the Spring Boot container with Docker Compose.
5. Expose the application publicly through Caddy on top of `127.0.0.1:8081`.

## Boundary and Governance

- Product code belongs in `src/`, `frontend/`, `docs/`, `deploy/`, `pom.xml`, `README.md` and project governance files
- Local agent/tooling mirrors do not belong to product version control unless a specific story makes them part of delivery
- All meaningful product changes should be introduced through stories in `docs/stories/`
- Required quality gates remain `mvn test` and `mvn verify`

## Current Constraints

- The repository still versions generated dashboard assets in `src/main/resources/static/ui/` because Spring Boot serves the exported build directly
- Authentication bootstrap still depends on configured credentials to provision the initial persisted user
- Webhook delivery retry is application-local; it improves resilience but is not yet a fully externalized messaging system
- The project remains a monolith by design; modularization is deferred until a future story explicitly requires it

## Primary Files for Fast Reorientation

- `README.md`
- `docs/architecture/current-system-map.md`
- `docs/architecture/brownfield-baseline.md`
- `docs/aiox/repository-boundary.md`
- `docs/stories/005-nextjs-postgres-runtime.md`
- `docs/stories/011-architecture-map-and-platform-hardening.md`
- `docs/architecture/security-baseline.md`

# Story 005: Next.js Frontend and PostgreSQL Runtime Evolution

## Status

Done

## Story

As the CRM technical owner,
I want the current Spring Boot CRM to evolve to a production-oriented frontend and runtime database architecture,
so that the application keeps the Java backend while moving to Next.js, React, Tailwind CSS, and PostgreSQL.

## Context

- The backend already exposes authenticated CRM APIs and a server-rendered dashboard.
- The target production direction now requires Next.js + React for the frontend.
- PostgreSQL should be the runtime database and H2 should remain only for unit and integration tests.
- JUnit and Mockito should remain the testing foundation.

## Acceptance Criteria

- [x] A story exists for this architecture evolution.
- [x] The Spring Boot runtime is configured to use PostgreSQL outside the test profile.
- [x] H2 is used only in the `test` profile.
- [x] Runtime schema management is explicit and does not depend on H2 bootstrap behavior.
- [x] The dashboard entrypoint is replaced by a Next.js frontend using React and Tailwind CSS.
- [x] The new frontend consumes the existing authenticated CRM API without expanding business scope.
- [x] The project keeps JUnit and adds Mockito-backed unit coverage where appropriate.
- [x] Documentation and environment examples reflect the new runtime architecture.
- [x] Quality gates pass after the change.

## Tasks / Subtasks

- [x] Create the architecture evolution story.
- [x] Introduce PostgreSQL runtime dependencies and explicit schema management.
- [x] Restrict H2 usage to automated tests.
- [x] Replace the current server-rendered dashboard with a Next.js + Tailwind frontend.
- [x] Add or update unit and integration tests with JUnit and Mockito.
- [x] Update documentation and environment templates.
- [x] Run `mvn test` and `mvn verify`.

## Notes

- This is a brownfield evolution, not a backend rewrite.
- The current API surface should remain stable while the frontend changes.
- The implementation should keep Java 17 + Spring Boot as the application core.

## File List

- [x] `docs/stories/005-nextjs-postgres-runtime.md`
- [x] `pom.xml`
- [x] `src/main/java/com/synkra/crm/controller/DashboardController.java`
- [x] `src/main/java/com/synkra/crm/config/SecurityConfig.java`
- [x] `src/main/resources/application.yml`
- [x] `src/main/resources/application-dev.yml`
- [x] `src/test/resources/application-test.yml`
- [x] `src/main/resources/db/migration/V1__initial_schema.sql`
- [x] `frontend/`
- [x] `src/main/resources/static/ui/`
- [x] `src/test/java/com/synkra/crm/CrmServiceTest.java`
- [x] `src/test/java/com/synkra/crm/CrmApiSecurityIntegrationTests.java`
- [x] `.env.example`
- [x] `README.md`

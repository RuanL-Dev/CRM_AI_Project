# Story 008: Login Theme, Demo Data and Live CRM Forms

## Status

Done

## Story

As a CRM stakeholder,
I want the login page aligned with the dashboard theme, seeded demo data in development, and fully interactive CRM forms,
so that the product feels cohesive, demonstrable and operational during evaluation.

## Context

- The dashboard already uses a dark futuristic language, but the login screen still uses a separate visual style.
- The current forms still rely on manual contact IDs for deals and activities, which hurts usability.
- The user reported an internal error and requested visible sample data to evaluate the product experience.

## Acceptance Criteria

- [x] A story exists for the login, demo data and live-form improvements.
- [x] The login screen uses the same dark futuristic visual direction as the main dashboard.
- [x] Development environments load safe demo data without affecting tests.
- [x] Creating contacts, opportunities and activities persists records in the database and refreshes the dashboard state.
- [x] User-facing forms no longer depend on raw manual contact IDs.
- [x] Known backend errors are translated into actionable PT-BR messages instead of generic failures where feasible.
- [x] Quality gates pass after the change.

## Tasks / Subtasks

- [x] Create the story for this evolution.
- [x] Investigate the internal error path and harden backend error handling.
- [x] Improve the login theme to match the dashboard.
- [x] Seed demo data only for dev runtime.
- [x] Improve frontend forms and interactions for live CRM updates.
- [x] Run `npm run build`, `mvn test` and `mvn verify`.

## Notes

- Keep PostgreSQL for dev/runtime and H2 restricted to tests.
- Demo seed must be idempotent and safe for repeated local startups.

## File List

- [x] `docs/stories/008-login-theme-demo-data-and-live-forms.md`
- [x] `frontend/app/page.jsx`
- [x] `frontend/app/globals.css`
- [x] `src/main/java/com/synkra/crm/config/DevDataSeeder.java`
- [x] `src/main/java/com/synkra/crm/controller/ApiExceptionHandler.java`
- [x] `src/main/java/com/synkra/crm/service/CrmService.java`
- [x] `src/main/resources/application.yml`
- [x] `src/main/resources/application-dev.yml`
- [x] `src/test/resources/application-test.yml`
- [x] `src/main/resources/static/auth/login.html`
- [x] `src/main/resources/static/auth/login.css`
- [x] `src/main/resources/static/ui/`
- [x] `src/test/java/com/synkra/crm/CrmApiSecurityIntegrationTests.java`

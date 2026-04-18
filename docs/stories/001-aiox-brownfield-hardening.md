# Story 001: AIOX Brownfield Hardening Baseline

## Status

Completed

## Story

As the CRM technical owner,
I want the existing brownfield CRM aligned with essential AIOX governance and baseline hardening,
so that the application becomes more professional, secure, and robust for continued delivery.

## Context

- The project is a Spring Boot CRM brownfield application.
- The repository already includes local AIOX assets, but it is not yet operating under the AIOX story-driven workflow.
- The current application exposes unauthenticated endpoints, uses permissive development settings, has minimal tests, and lacks formal story tracking.

## Acceptance Criteria

- [x] A formal story exists in `docs/stories/` with acceptance criteria and file list.
- [x] The application has a minimal authentication layer protecting API and dashboard access.
- [x] Development-only concerns are isolated from safer default runtime configuration.
- [x] The dashboard rendering no longer injects API content via unsafe HTML concatenation.
- [x] Webhook integration has baseline timeout configuration and safer failure behavior.
- [x] Error responses no longer expose internal exception messages on generic server failures.
- [x] Automated tests cover the main protected API flow and basic validation behavior.
- [x] Project documentation explains architecture, security baseline, runtime profiles, and quality gates.

## Tasks / Subtasks

- [x] Create the brownfield AIOX story and document the baseline scope.
- [x] Add Spring Security dependency and runtime configuration.
- [x] Split environment configuration into safer defaults and local-dev overrides.
- [x] Refactor frontend rendering to use safe DOM APIs.
- [x] Improve webhook client resilience settings.
- [x] Harden generic exception handling.
- [x] Add integration tests for authentication and validation.
- [x] Update README and architecture documentation.

## Notes

- This story intentionally focuses on baseline alignment and hardening, not a full CRM domain redesign.
- Future stories should address domain layering, persistent production database strategy, pagination, and advanced observability.

## File List

- [x] `docs/stories/001-aiox-brownfield-hardening.md`
- [x] `docs/architecture/brownfield-baseline.md`
- [x] `README.md`
- [x] `.env.example`
- [x] `.gitignore`
- [x] `pom.xml`
- [x] `src/main/java/com/synkra/crm/config/AppConfig.java`
- [x] `src/main/java/com/synkra/crm/config/SecurityConfig.java`
- [x] `src/main/java/com/synkra/crm/controller/ApiExceptionHandler.java`
- [x] `src/main/java/com/synkra/crm/service/CrmService.java`
- [x] `src/main/java/com/synkra/crm/service/N8nWebhookService.java`
- [x] `src/main/resources/application.yml`
- [x] `src/main/resources/application-dev.yml`
- [x] `src/main/resources/static/js/app.js`
- [x] `src/test/java/com/synkra/crm/CrmApiSecurityIntegrationTests.java`
- [x] `src/test/resources/application-test.yml`

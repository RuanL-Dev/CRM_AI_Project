# CRM Brownfield Baseline

## Objective

Align the existing CRM with the AIOX operating model while establishing a safer technical baseline for continued evolution.

## Current Architecture

- Monolithic Spring Boot application
- Server-rendered dashboard with Thymeleaf and vanilla JavaScript
- JPA entities for `Contact`, `Deal`, and `Activity`
- H2-backed persistence for local bootstrap
- Synchronous webhook publishing to N8N

## Risks Identified

- Unauthenticated access to dashboard and API
- Development-oriented runtime defaults applied outside explicit dev profile
- Unsafe DOM rendering path in the dashboard
- Minimal automated test coverage
- Direct entity exposure from API responses
- External webhook integration without strong resilience mechanisms

## Baseline Decisions

1. Protect the dashboard and API with a minimal authentication layer.
2. Restrict development-only conveniences to the `dev` profile.
3. Keep the current monolith shape for now and defer deeper modularization to future stories.
4. Publish webhook calls after transaction commit and with explicit HTTP timeouts.
5. Use story-driven governance under `docs/stories/` for all future work.

## Next AIOX Stories

1. Introduce response DTOs and stronger API contracts.
2. Move from bootstrap persistence to a production-ready database strategy.
3. Add pagination, filtering, and richer domain workflows.
4. Add observability, audit logging, and deployment runbooks.

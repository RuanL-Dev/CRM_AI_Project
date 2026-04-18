# Story 013: Security Hardening Baseline

## Status

- [x] Formalizar a story de hardening de seguranca do CRM
- [x] Publicar um mapa objetivo da baseline de seguranca
- [x] Remover defaults inseguros do runtime base
- [x] Endurecer autenticacao, sessao e headers HTTP
- [x] Adicionar protecao basica contra brute force no login
- [x] Atualizar documentacao operacional e exemplos de ambiente
- [x] Rodar quality gates e atualizar file list

## Story

As the CRM technical owner,
I want the current CRM baseline hardened against common web application attacks,
so that authentication, sessions, configuration secrets and browser-facing responses follow a clearer and safer security posture.

## Context

- The repository already has Spring Security and persisted users, but the baseline still lacks several explicit controls expected in a modern CRM.
- Runtime configuration still exposes insecure defaults for bootstrap credentials and datasource credentials in the base configuration.
- The login flow does not currently include brute-force throttling or clear session-hardening behavior.
- Browser-facing responses still rely mostly on framework defaults and do not publish a full set of explicit security headers.
- The project needs a security map that is faster to inspect in future analyses and still aligned with the AIOX story-driven hardening model.

## Acceptance Criteria

- [x] A story exists for the security-hardening increment.
- [x] The repository contains an objective security document under `docs/architecture/` describing controls, gaps and residual risks.
- [x] Base runtime configuration no longer ships with insecure default CRM or datasource credentials.
- [x] The security configuration publishes explicit browser-facing headers for CSP, HSTS, referrer policy, permissions policy and framing rules.
- [x] Session handling is explicitly hardened for fixation, logout cleanup and forwarded HTTPS-aware deployments.
- [x] Login attempts are rate-limited or temporarily blocked after repeated failures.
- [x] API unauthenticated behavior stays machine-friendly while UI unauthenticated behavior remains login-page oriented.
- [x] README and environment examples explain the required secrets and the secure runtime expectations.
- [x] Quality gates pass after the change.

## Tasks / Subtasks

- [x] Create the story for the security-hardening increment.
- [x] Add a security baseline map under `docs/architecture/`.
- [x] Remove insecure defaults from `application.yml` and move safe local-only defaults to `application-dev.yml`.
- [x] Add runtime guardrails for weak bootstrap or datasource credentials outside local/test contexts.
- [x] Harden Spring Security headers, session handling and unauthenticated entry points.
- [x] Add login throttling and user-facing feedback for temporary lockout.
- [x] Update README, `.env.example` and related docs with the secure configuration baseline.
- [x] Update tests and run `mvn test` and `mvn verify`.

## Notes

- No CRM can be made permanently or absolutely immune to attack; this story focuses on materially reducing common attack paths and documenting the residual risk.
- The hardening scope should stay within the existing product boundary and avoid speculative enterprise controls that the current stack does not operate yet.
- External security guidance should be translated into controls that are maintainable in this repository, not pasted as aspirational checklist noise.

## File List

- [x] `docs/stories/013-security-hardening-baseline.md`
- [x] `docs/architecture/security-baseline.md`
- [x] `docs/architecture/current-system-map.md`
- [x] `docs/stories/001-aiox-brownfield-hardening.md`
- [x] `README.md`
- [x] `.env.example`
- [x] `.github/workflows/deploy-production.yml`
- [x] `deploy/.env.example`
- [x] `deploy/Caddyfile`
- [x] `deploy/README.md`
- [x] `deploy/docker-compose.yml`
- [x] `frontend/app/globals.css`
- [x] `frontend/tailwind.config.js`
- [x] `src/main/java/com/synkra/crm/config/AppUserBootstrapper.java`
- [x] `src/main/java/com/synkra/crm/config/SecurityConfig.java`
- [x] `src/main/java/com/synkra/crm/config/SecurityProperties.java`
- [x] `src/main/java/com/synkra/crm/config/RuntimeSecurityGuardrails.java`
- [x] `src/main/java/com/synkra/crm/controller/LoginController.java`
- [x] `src/main/java/com/synkra/crm/security/LoginAttemptService.java`
- [x] `src/main/java/com/synkra/crm/security/LoginRateLimitFilter.java`
- [x] `src/main/java/com/synkra/crm/security/RateLimitedAuthenticationFailureHandler.java`
- [x] `src/main/java/com/synkra/crm/security/RateLimitedAuthenticationSuccessHandler.java`
- [x] `src/main/resources/application.yml`
- [x] `src/main/resources/application-dev.yml`
- [x] `src/main/resources/application-prod.yml`
- [x] `src/main/resources/static/auth/login.css`
- [x] `src/main/resources/static/auth/login.html`
- [x] `src/main/resources/static/auth/login.js`
- [x] `src/main/resources/static/ui/`
- [x] `src/test/java/com/synkra/crm/CrmApiSecurityIntegrationTests.java`
- [x] `src/test/resources/application-test.yml`

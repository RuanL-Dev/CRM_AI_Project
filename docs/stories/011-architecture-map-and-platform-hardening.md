# Story 011: Architecture Map and Platform Hardening

## Status

- [x] Formalizar a story para documentação arquitetural e correções estruturais
- [x] Publicar um mapa arquitetural objetivo do estado atual do sistema
- [x] Atualizar documentação desatualizada de stack e source tree
- [x] Reduzir risco de drift entre `frontend/` e `static/ui/`
- [x] Remover exposição direta de entidades JPA nas respostas da API
- [x] Melhorar o cálculo de métricas do dashboard para reduzir agregações em memória
- [x] Substituir autenticação somente em memória por autenticação persistida no banco
- [x] Introduzir outbox persistente com retry para integração com N8N
- [x] Rodar quality gates e atualizar file list

## Story

As the CRM technical owner,
I want a current-state architecture map and targeted platform hardening changes,
so that future analysis is faster and the repository baseline better matches the real production direction.

## Context

- The repository documentation still contains framework files describing the old Thymeleaf + H2 baseline.
- The frontend source and exported static assets are both part of the repository flow, but build synchronization is still manual.
- The API still exposes JPA entities directly, which weakens contracts and couples persistence shape to clients.
- Dashboard metrics currently load full entity sets into memory before aggregating them.
- Authentication still depends on an in-memory user configured by properties.
- N8N webhook failures are only logged and do not have persistent retry or outbox tracking.

## Acceptance Criteria

- [x] A story exists for the architecture-map and platform-hardening work.
- [x] The repository contains an architecture document that explains current modules, runtime flow, build flow, deploy flow and known constraints.
- [x] `docs/framework/source-tree.md` and `docs/framework/tech-stack.md` reflect the current Next.js + PostgreSQL architecture.
- [x] The Java build reduces drift risk by synchronizing or validating frontend export state before packaging runtime assets.
- [x] API responses for contacts, deals and activities use explicit response DTOs instead of exposing JPA entities directly.
- [x] Dashboard metrics avoid full-record aggregation in memory for the core counts and grouped metrics.
- [x] Authentication loads users from persisted application data instead of `InMemoryUserDetailsManager`, while still preserving bootstrap access through configured credentials.
- [x] N8N delivery attempts are persisted, retried and observable through application state instead of only transient logs.
- [x] Quality gates pass after the change.

## Tasks / Subtasks

- [x] Create the story for this hardening increment.
- [x] Add an objective architecture map under `docs/architecture/`.
- [x] Update framework docs that still describe the old baseline.
- [x] Integrate frontend export synchronization into the Java build lifecycle.
- [x] Introduce response DTOs and map API responses explicitly.
- [x] Replace in-memory authentication with a persisted application-user model and bootstrap initializer.
- [x] Add persistent webhook delivery tracking with retry scheduling.
- [x] Refactor dashboard metrics to use repository-level aggregation.
- [x] Update tests and run `npm run build`, `mvn test` and `mvn verify`.

## Notes

- This remains a brownfield hardening story, not a domain expansion story.
- The user-facing CRM scope should remain stable while platform internals are improved.
- The architecture map should describe the current state and its known limitations, not an aspirational future state.

## File List

- [x] `docs/stories/011-architecture-map-and-platform-hardening.md`
- [x] `docs/architecture/current-system-map.md`
- [x] `docs/framework/source-tree.md`
- [x] `docs/framework/tech-stack.md`
- [x] `README.md`
- [x] `.env.example`
- [x] `pom.xml`
- [x] `src/main/java/com/synkra/crm/config/AppConfig.java`
- [x] `src/main/java/com/synkra/crm/config/AppUserBootstrapper.java`
- [x] `src/main/java/com/synkra/crm/config/SecurityConfig.java`
- [x] `src/main/java/com/synkra/crm/controller/CrmApiController.java`
- [x] `src/main/java/com/synkra/crm/dto/ActivityResponse.java`
- [x] `src/main/java/com/synkra/crm/dto/ContactResponse.java`
- [x] `src/main/java/com/synkra/crm/dto/ContactSummaryResponse.java`
- [x] `src/main/java/com/synkra/crm/dto/DashboardMetricsResponse.java`
- [x] `src/main/java/com/synkra/crm/dto/DashboardTimelineItemResponse.java`
- [x] `src/main/java/com/synkra/crm/dto/DealResponse.java`
- [x] `src/main/java/com/synkra/crm/model/AppUser.java`
- [x] `src/main/java/com/synkra/crm/model/WebhookDelivery.java`
- [x] `src/main/java/com/synkra/crm/model/WebhookDeliveryStatus.java`
- [x] `src/main/java/com/synkra/crm/repository/ActivityRepository.java`
- [x] `src/main/java/com/synkra/crm/repository/ActivityTypeCountView.java`
- [x] `src/main/java/com/synkra/crm/repository/AppUserRepository.java`
- [x] `src/main/java/com/synkra/crm/repository/ContactRepository.java`
- [x] `src/main/java/com/synkra/crm/repository/ContactStatusCountView.java`
- [x] `src/main/java/com/synkra/crm/repository/DealRepository.java`
- [x] `src/main/java/com/synkra/crm/repository/DealStageValueView.java`
- [x] `src/main/java/com/synkra/crm/repository/WebhookDeliveryRepository.java`
- [x] `src/main/java/com/synkra/crm/service/CrmService.java`
- [x] `src/main/java/com/synkra/crm/service/N8nWebhookService.java`
- [x] `src/main/resources/application.yml`
- [x] `src/main/resources/db/migration/V2__platform_hardening.sql`
- [x] `src/main/resources/static/ui/`
- [x] `src/test/java/com/synkra/crm/CrmApiSecurityIntegrationTests.java`
- [x] `src/test/java/com/synkra/crm/CrmServiceTest.java`
- [x] `src/test/java/com/synkra/crm/N8nWebhookServiceIntegrationTests.java`
- [x] `src/test/resources/application-test.yml`

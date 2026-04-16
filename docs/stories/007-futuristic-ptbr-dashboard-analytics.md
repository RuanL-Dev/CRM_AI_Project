# Story 007: Futuristic PT-BR Dashboard Analytics

## Status

Done

## Story

As the CRM product owner,
I want a futuristic dark-mode dashboard with business charts and full PT-BR presentation,
so that the CRM feels premium to clients and gives clear commercial visibility.

## Context

- The dashboard currently exposes only simple cards and tables.
- The product needs a stronger executive view with charts for leads, records and pipeline.
- The user explicitly requested PT-BR language and a futuristic dark mode aligned with the business.

## Acceptance Criteria

- [x] A story exists for the dashboard analytics and PT-BR redesign.
- [x] The main dashboard uses dark mode with futuristic visual language.
- [x] The dashboard includes visual charts for leads, records over time, pipeline and activity mix.
- [x] User-facing dashboard text is fully business-oriented and in PT-BR.
- [x] Technical stack language is removed from the user-facing CRM dashboard.
- [x] Backend metrics provide enough data for the visualizations without changing core product scope.
- [x] Quality gates pass after the change.

## Tasks / Subtasks

- [x] Create the story for the redesign.
- [x] Expand dashboard analytics in the backend.
- [x] Translate user-facing CRM interaction to PT-BR.
- [x] Rebuild the dashboard with futuristic dark mode and charts.
- [x] Run `npm run build`, `mvn test` and `mvn verify`.

## Notes

- Keep Java 17 + Spring Boot as the backend core.
- Keep the dashboard focused on business value and operational visibility.

## File List

- [x] `docs/stories/007-futuristic-ptbr-dashboard-analytics.md`
- [x] `src/main/java/com/synkra/crm/service/CrmService.java`
- [x] `src/main/java/com/synkra/crm/controller/ApiExceptionHandler.java`
- [x] `src/main/java/com/synkra/crm/dto/CreateContactRequest.java`
- [x] `src/main/java/com/synkra/crm/dto/CreateDealRequest.java`
- [x] `src/main/java/com/synkra/crm/dto/CreateActivityRequest.java`
- [x] `src/test/java/com/synkra/crm/CrmApiSecurityIntegrationTests.java`
- [x] `frontend/app/page.jsx`
- [x] `frontend/app/globals.css`
- [x] `src/main/resources/static/ui/`

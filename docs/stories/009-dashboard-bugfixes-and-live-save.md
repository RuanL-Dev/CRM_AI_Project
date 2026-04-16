# Story 009: Dashboard Bugfixes and Live Save

## Status

Done

## Story

As a CRM evaluator,
I want dashboard legend layout fixed and live save flows working without internal errors,
so that I can interact with the product confidently and evaluate the visual experience without broken states.

## Acceptance Criteria

- [x] A story exists for the dashboard bugfixes.
- [x] Funil and Atividades legends no longer overflow their components.
- [x] Saving contact, opportunity and activity works from the frontend and persists in the database.
- [x] The internal error caused by failed save requests is removed from the normal user flow.
- [x] Quality gates pass after the change.

## File List

- [x] `docs/stories/009-dashboard-bugfixes-and-live-save.md`
- [x] `frontend/app/page.jsx`
- [x] `frontend/app/globals.css`
- [x] `src/main/java/com/synkra/crm/controller/ApiExceptionHandler.java`
- [x] `src/test/java/com/synkra/crm/CrmApiSecurityIntegrationTests.java`
- [x] `src/main/resources/static/ui/`

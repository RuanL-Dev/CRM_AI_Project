# Story 006: Custom Login and Business-Focused Frontend Copy

## Status

Done

## Story

As the CRM product owner,
I want a custom login experience and business-oriented frontend content,
so that the application feels professional to end users and focuses on CRM value instead of implementation details.

## Context

- The current authentication entrypoint still falls back to Spring Security's default login page.
- The dashboard copy still includes architecture-oriented content that is useful to engineering but not to end users.
- The product should present a commercial CRM experience from the first interaction.

## Acceptance Criteria

- [x] A story exists for this UX and authentication refinement.
- [x] The application no longer shows the default Spring Security login screen.
- [x] Users can authenticate through a custom login page with explicit login action.
- [x] The dashboard copy is business-oriented and avoids implementation and stack discussion.
- [x] The update preserves the current backend behavior and security expectations.
- [x] Quality gates pass after the change.

## Tasks / Subtasks

- [x] Create the story for the refinement.
- [x] Add a custom login page and wire Spring Security to it.
- [x] Replace architecture-oriented frontend copy with business-facing CRM language.
- [x] Rebuild the frontend static assets.
- [x] Run `mvn test` and `mvn verify`.

## Notes

- This is a UX and authentication refinement over the existing architecture.
- The focus is end-user presentation, not backend scope expansion.

## File List

- [x] `docs/stories/006-custom-login-and-business-copy.md`
- [x] `src/main/java/com/synkra/crm/config/SecurityConfig.java`
- [x] `src/main/java/com/synkra/crm/controller/LoginController.java`
- [x] `src/main/resources/static/auth/login.html`
- [x] `src/main/resources/static/auth/login.css`
- [x] `frontend/app/page.jsx`
- [x] `src/main/resources/static/ui/`
- [x] `src/test/java/com/synkra/crm/CrmApiSecurityIntegrationTests.java`

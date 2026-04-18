# Story 014: Deploy Readiness and Runtime Diagnostics

## Status

- [x] Formalizar a story para diagnostico de falha no deploy
- [x] Validar a causa mais provavel do timeout de readiness
- [x] Definir a estrategia de readiness via endpoint tecnico dedicado
- [x] Endurecer o workflow para falhar cedo com secrets invalidos para runtime seguro
- [x] Expor diagnostico remoto de containers quando a aplicacao nao fica pronta
- [x] Corrigir a validacao remota de checksum para usar manifesto portavel no servidor
- [x] Atualizar documentacao operacional e file list
- [x] Rodar quality gates do projeto

## Story

As the CRM technical owner,
I want the production deploy workflow to fail with actionable diagnostics,
so that runtime startup issues do not surface only as a generic readiness timeout.

## Context

- O workflow atual reinicia a stack e espera um sinal de prontidao da aplicacao no host remoto.
- Quando a aplicacao nao sobe, o erro final fica reduzido a `Application did not become ready`, sem logs do container.
- A baseline atual de seguranca exige secrets fortes fora de `dev` e `test`, o que pode derrubar o startup se o ambiente remoto estiver inconsistente.
- O deploy precisa falhar mais cedo para configuracoes que ja violam os guardrails conhecidos do runtime.
- A decisao atual do projeto e usar um endpoint tecnico dedicado para readiness interno, sem depender do HTML da tela de login.
- A validacao de checksum tambem precisa ser portavel entre runner e servidor remoto; manifests com caminho local do runner quebram no host de deploy.

## Acceptance Criteria

- [x] A story exists for the deploy-readiness diagnostic hardening.
- [x] The deploy workflow validates required production secrets against the current runtime baseline before uploading artifacts.
- [x] The application exposes a dedicated unauthenticated readiness endpoint for internal deploy checks.
- [x] The workflow waits for readiness long enough for the Spring Boot runtime and PostgreSQL dependency chain to stabilize.
- [x] On readiness failure, the workflow prints remote container status and recent logs for `crm-app` and `crm-postgres`.
- [x] Deploy documentation explains the stronger secret expectations and the new diagnostic behavior.

## Tasks / Subtasks

- [x] Create the story for deploy-readiness diagnostics.
- [x] Inspect the current deploy workflow and runtime configuration for likely startup blockers.
- [x] Add a dedicated readiness endpoint aligned with the production deploy flow.
- [x] Add preflight validation for production secrets used by the runtime guardrails.
- [x] Add remote diagnostic output for compose status and container logs when readiness fails.
- [x] Increase readiness tolerance to reduce false negatives on slower startups.
- [x] Generate the checksum manifest with a path that is valid on the remote deploy directory.
- [x] Run `mvn test` and `mvn verify`.

## Notes

- This increment preserves the current deploy architecture and improves operability only.
- The goal is not to mask startup failures, but to make them explicit in GitHub Actions output.
- The checksum manifest is now emitted relative to the artifact directory so `sha256sum -c` works after upload on the server.

## File List

- [x] `docs/stories/014-deploy-readiness-and-runtime-diagnostics.md`
- [x] `.github/workflows/deploy-production.yml`
- [x] `deploy/README.md`
- [x] `src/main/java/com/synkra/crm/controller/LoginController.java`
- [x] `src/main/java/com/synkra/crm/config/SecurityConfig.java`
- [x] `src/test/java/com/synkra/crm/CrmApiSecurityIntegrationTests.java`

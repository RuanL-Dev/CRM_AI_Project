# Story 012: Secure GitHub Actions CD to Production Server

## Status

- [x] Formalizar a story para CI/CD seguro
- [x] Versionar workflow de deploy sem expor segredos
- [x] Ajustar boundary para permitir `.github/workflows/` no repositório
- [x] Documentar segredos obrigatórios do GitHub Actions e do servidor
- [x] Publicar manualmente a baseline atual no servidor para validar o fluxo
- [x] Validar `/login` e confirmar o deploy da baseline `6ebcce0`

## Story

As the CRM technical owner,
I want a secure GitHub-native deployment pipeline to the production server,
so that each commit on `main` can be delivered automatically without exposing passwords, keys or runtime secrets in the repository.

## Context

- The repository already has a deployable Spring Boot jar and production-oriented `deploy/` assets.
- The current production server runs an older jar than the latest commit pushed to GitHub.
- The repository boundary currently ignores `.github/`, which blocks versioned CI/CD workflows.
- Production secrets must remain outside git and outside plaintext repository files.

## Acceptance Criteria

- [x] A story exists for the secure CI/CD setup.
- [x] The repository contains a GitHub Actions workflow that builds, verifies and deploys the CRM on pushes to `main`.
- [x] The workflow uses GitHub Secrets for SSH credentials and runtime configuration instead of versioned secrets.
- [x] The repository documents exactly which GitHub Secrets are required and how the remote `.env` is managed safely.
- [x] No password, private key or production token is added to tracked files.
- [x] The current baseline commit `6ebcce0` is deployed to the server and `/login` is validated after rollout.

## Tasks / Subtasks

- [x] Create the story for the CI/CD setup.
- [x] Update `.gitignore` to allow versioned `.github/workflows/`.
- [x] Add a deploy workflow for `main` using Maven build + SSH/SCP rollout.
- [x] Add deployment documentation for GitHub Secrets and remote runtime behavior.
- [x] Publish the current jar and deploy assets manually to the server once.
- [x] Validate `/login` after rollout.

## Notes

- The repository must not store real production credentials.
- The remote server should keep runtime secrets in `.env`, refreshed only from GitHub Secrets during deploy.
- The workflow should fail fast when required secrets are missing.

## File List

- [x] `docs/stories/012-secure-github-actions-cd.md`
- [x] `.gitignore`
- [x] `.github/workflows/deploy-production.yml`
- [x] `deploy/.env.example`
- [x] `deploy/docker-compose.yml`
- [x] `deploy/README.md`

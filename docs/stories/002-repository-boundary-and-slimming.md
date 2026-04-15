# Story 002: Repository Boundary And Slimming

## Status

Done

## Story

As the CRM technical owner,
I want the repository to keep only application code and the minimum AIOX project governance artifacts,
so that the project is cleaner, easier to maintain, and closer to a production-ready repository layout.

## Context

- The first publish included large local AIOX and IDE integration directories.
- Those directories are useful as local tooling, but they are not part of the CRM runtime or core product source.
- The repository should preserve story-driven governance while reducing noise and maintenance overhead.

## Acceptance Criteria

- [x] A second story exists for repository slimming and boundary definition.
- [x] The repository keeps application code, documentation, and minimal project governance only.
- [x] Local IDE/framework integration directories are removed from version control.
- [x] `.gitignore` prevents those local tooling directories from being recommitted by default.
- [x] A root `AGENTS.md` explains the project-local AIOX rules for this CRM repository.
- [x] Documentation explains which artifacts belong in the repo and which remain local-only.
- [x] The project still passes `mvn test` and `mvn verify` after the cleanup.

## Tasks / Subtasks

- [x] Create the second story for repository slimming.
- [x] Add a project-local `AGENTS.md` with minimal AIOX operating rules.
- [x] Document the repository boundary for code vs tooling.
- [x] Update `.gitignore` to ignore local tooling directories.
- [x] Remove IDE/framework local directories from git tracking.
- [x] Re-run Maven quality gates.
- [ ] Commit and publish the cleanup.

## Notes

- This story does not remove AIOX from the delivery process.
- It narrows the committed footprint to what is necessary for the CRM application and project governance.

## File List

- [x] `docs/stories/002-repository-boundary-and-slimming.md`
- [x] `AGENTS.md`
- [x] `docs/aiox/repository-boundary.md`
- [x] `.gitignore`
- [x] `README.md`
- [x] untracked from git: `.agent/`, `.aiox-core/`, `.antigravity/`, `.claude/`, `.codex/`, `.cursor/`, `.gemini/`, `.github/`

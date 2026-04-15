# Repository Boundary

## In Scope For Version Control

- Application source code in `src/`
- Build definition in `pom.xml`
- Product and operations documentation in `docs/`
- Project-local governance files such as `AGENTS.md`, `README.md`, `.gitignore`, and environment examples

## Out Of Scope For Version Control

- IDE-specific rule packs
- Local agent activation packs
- Local framework mirrors and scaffolding copied only for workstation convenience
- Generated build output
- Real secrets and machine-specific settings

## Decision

This repository keeps the CRM application and the minimum AIOX governance needed to operate the project.

Local productivity tooling may still exist on a workstation, but it should not define the repository boundary unless the application build or delivery process depends on it directly.

## Result

- Smaller repository footprint
- Lower review noise
- Cleaner onboarding for contributors
- Clear separation between product code and local orchestration tooling

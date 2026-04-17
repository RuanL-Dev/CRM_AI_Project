# Source Tree

- `src/main/java/com/synkra/crm`: Spring Boot backend source, including config, controllers, DTOs, entities, repositories and services
- `src/main/resources/static/ui`: exported Next.js dashboard served by Spring Boot
- `src/main/resources/static/auth`: custom login assets served directly by Spring Boot
- `src/main/resources/db/migration`: Flyway schema migrations for runtime environments
- `src/test/java/com/synkra/crm`: unit and integration tests for application behavior and security
- `src/test/resources`: test-only runtime configuration using H2
- `frontend`: Next.js + React + Tailwind source workspace for the dashboard
- `deploy`: production-oriented Docker Compose and reverse-proxy assets
- `docs/architecture`: brownfield baseline and current-state architecture references
- `docs/framework`: AIOX framework support docs
- `docs/stories`: story-driven change log and delivery contracts

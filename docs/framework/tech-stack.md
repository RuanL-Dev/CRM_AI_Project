# Tech Stack

- Backend: Java 17, Spring Boot 3.3, Spring Web, Spring Data JPA, Bean Validation, Spring Security
- Frontend: Next.js 15 static export, React 18, Tailwind CSS 3, PostCSS
- Runtime database: PostgreSQL 16 with Flyway migrations
- Test database: H2 in PostgreSQL compatibility mode
- Authentication: persisted application users bootstrapped from configured credentials
- Automation: N8N webhook integration with persisted delivery tracking and retry flow
- Build: Maven 3.9+ plus frontend build/export sync

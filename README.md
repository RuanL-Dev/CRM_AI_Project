# Neon CRM (Java) - AIOX + N8N

CRM profissional em Java (Spring Boot), tratado como projeto brownfield sob governanca AIOX.

## O que foi entregue

- Backend Java com API REST para contatos, deals e atividades
- UI futuristica responsiva (dashboard + formularios + tabelas)
- Integracao com N8N via webhook para eventos de CRM
- Banco H2 embarcado para bootstrap rapido
- Estrutura documental inicial seguindo o padrao AIOX (`docs/framework/*`)
- Story ativa de hardening brownfield em `docs/stories/001-aiox-brownfield-hardening.md`

## Governanca AIOX

- Fonte de verdade: `.aiox-core/constitution.md`
- Workflow obrigatorio: story first, acceptance criteria, checklist e file list
- Story atual: `docs/stories/001-aiox-brownfield-hardening.md`
- Arquitetura brownfield inicial: `docs/architecture/brownfield-baseline.md`

## Perfis de runtime

- `dev`: H2 em memoria, console H2 habilitado e `ddl-auto=update`
- `default`: configuracao mais segura, sem console H2 e com `ddl-auto=validate`
- `test`: H2 isolado para testes automatizados

## Executar localmente

1. Execute em modo desenvolvimento:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

2. Abra no navegador:

- `http://localhost:8080`
- Console H2: `http://localhost:8080/h2-console`

## Autenticacao inicial

O dashboard e a API exigem autenticacao.

Variaveis padrao:

```bash
CRM_USERNAME=admin
CRM_PASSWORD=change-me-now
```

Defina valores reais antes de publicar qualquer ambiente compartilhado.

## Configuracao N8N

Defina a variavel de ambiente antes de iniciar:

```bash
# Windows (PowerShell)
$env:N8N_WEBHOOK_URL="https://seu-n8n/webhook/crm-events"

# Git Bash
export N8N_WEBHOOK_URL="https://seu-n8n/webhook/crm-events"
```

Eventos enviados automaticamente:

- `contact.created`
- `deal.created`
- `deal.stage.updated`
- `activity.created`

Formato enviado ao N8N:

```json
{
  "eventType": "deal.created",
  "sentAt": "2026-04-15T16:00:00Z",
  "payload": { "...": "dados da entidade" }
}
```

## Endpoints principais

- `GET /api/contacts`
- `POST /api/contacts`
- `GET /api/deals`
- `POST /api/deals`
- `PATCH /api/deals/{id}/stage?stage=WON`
- `GET /api/activities`
- `POST /api/activities`
- `GET /api/dashboard/metrics`

## Quality Gates

Para este projeto Java, use os equivalentes operacionais do baseline AIOX:

```bash
mvn test
mvn verify
```

Recomendado para a proxima iteracao:

- checkstyle / spotless
- spotbugs
- dependency vulnerability scanning

# CRM AI Project

CRM profissional orientado a vendas, relacionamento e automacao comercial, construido com baseline backend em Java/Spring Boot e governado pelo fluxo AIOX para evolucao controlada, segura e orientada por stories.

O objetivo do projeto e oferecer uma base robusta para operacao de CRM, centralizando gestao de contatos, oportunidades, atividades e integracoes, com uma arquitetura preparada para amadurecer para um ambiente de producao mais escalavel.

## Visao do Produto

O CRM foi iniciado com suporte do Codex e esta sendo padronizado com as diretrizes do AIOX para atingir um nivel de aplicacao profissional, segura e robusta. Isso significa trabalhar com:

- governanca por stories e acceptance criteria
- documentacao arquitetural viva
- evolucao incremental com quality gates
- separacao clara entre codigo do produto e tooling local
- endurecimento progressivo de seguranca, integracoes e persistencia

## Funcionalidades Principais

- Cadastro e consulta de contatos
- Cadastro e acompanhamento de deals/oportunidades comerciais
- Registro de atividades ligadas ao funil comercial
- Dashboard com metricas operacionais do CRM
- Integracao de eventos com N8N via webhook
- Autenticacao basica para protecao do dashboard e da API
- Estrutura brownfield preparada para evolucao orientada a stories

## Stack Atual

A implementacao atual do repositorio utiliza:

- Backend: Java 17 + Spring Boot
- API: Spring Web
- Persistencia atual: Spring Data JPA + H2
- Seguranca: Spring Security
- Renderizacao atual de interface: Thymeleaf + JavaScript
- Build e dependencia: Maven
- Testes: JUnit + Spring Boot Test
- Integracao de automacao: N8N via webhook HTTP

## Stack Alvo de Producao

A direcao definida para a evolucao do produto e:

- Frontend: React.js
- Estilizacao do frontend: Tailwind CSS
- Backend de dominio e APIs: Java + Spring Boot
- Banco de dados de producao: PostgreSQL
- Integracoes e automacoes: N8N

Importante: React.js, Tailwind CSS e PostgreSQL fazem parte da arquitetura alvo de producao. O baseline atual ainda opera com frontend server-rendered e H2 para bootstrap e desenvolvimento rapido.

## Arquitetura em Alto Nivel

O projeto segue uma abordagem brownfield organizada em camadas:

- Camada de apresentacao: dashboard web e endpoints HTTP
- Camada de aplicacao: regras de negocio para contatos, deals, atividades e metricas
- Camada de persistencia: entidades JPA e repositorios
- Camada de integracao: publicacao de eventos para workflows externos no N8N
- Camada de governanca: stories, arquitetura e documentacao operacional sob padrao AIOX

Documentos relacionados:

- `docs/architecture/brownfield-baseline.md`
- `docs/aiox/repository-boundary.md`
- `docs/stories/`

## Modulos do CRM

### Contatos

Permite registrar e consultar clientes e leads dentro da base comercial.

### Deals

Organiza oportunidades comerciais e permite acompanhar a evolucao do funil.

### Atividades

Centraliza interacoes e tarefas vinculadas ao processo comercial.

### Dashboard

Consolida indicadores operacionais para visao rapida da operacao.

### Integracao N8N

Dispara eventos de dominio para fluxos externos de automacao.

## Perfis de Runtime

O projeto possui perfis distintos para reduzir risco operacional entre ambientes:

- `dev`: H2 em memoria, console H2 habilitado e `ddl-auto=update`
- `default`: configuracao mais segura, sem console H2 e com `ddl-auto=validate`
- `test`: H2 isolado para testes automatizados

## Executando Localmente

### 1. Pre-requisitos

- Java 17+
- Maven 3.9+

### 2. Subir a aplicacao em desenvolvimento

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 3. Acessos locais

- Aplicacao: `http://localhost:8080`
- Console H2: `http://localhost:8080/h2-console`

## Autenticacao Inicial

O dashboard e a API exigem autenticacao.

Variaveis padrao:

```bash
CRM_USERNAME=admin
CRM_PASSWORD=change-me-now
```

Antes de qualquer ambiente compartilhado, substitua essas credenciais por valores seguros.

## Integracao com N8N

Configure a URL do webhook antes de iniciar a aplicacao:

```bash
# Windows PowerShell
$env:N8N_WEBHOOK_URL="https://seu-n8n/webhook/crm-events"

# Git Bash
export N8N_WEBHOOK_URL="https://seu-n8n/webhook/crm-events"
```

Eventos atualmente publicados:

- `contact.created`
- `deal.created`
- `deal.stage.updated`
- `activity.created`

Exemplo de payload:

```json
{
  "eventType": "deal.created",
  "sentAt": "2026-04-15T16:00:00Z",
  "payload": {
    "id": 10,
    "title": "Enterprise Renewal"
  }
}
```

## Endpoints Principais

- `GET /api/contacts`
- `POST /api/contacts`
- `GET /api/deals`
- `POST /api/deals`
- `PATCH /api/deals/{id}/stage?stage=WON`
- `GET /api/activities`
- `POST /api/activities`
- `GET /api/dashboard/metrics`

## Estrutura do Repositorio

- `src/`: codigo-fonte da aplicacao
- `docs/`: stories, arquitetura e documentacao operacional
- `AGENTS.md`: regras locais de governanca AIOX
- `pom.xml`: configuracao do build Maven

Ferramentas locais de IDE, agentes e framework nao fazem parte do escopo versionado principal da aplicacao.

## Governanca AIOX

Este repositorio opera com padrao AIOX para evolucao controlada:

- story-first development
- acceptance criteria como contrato de entrega
- checklist e file list atualizados a cada story
- documentacao arquitetural como artefato vivo
- foco em hardening, rastreabilidade e qualidade

Referencias de governanca no projeto:

- `AGENTS.md`
- `docs/stories/001-aiox-brownfield-hardening.md`
- `docs/stories/002-repository-boundary-and-slimming.md`
- `docs/stories/003-professional-readme-refresh.md`

## Qualidade e Validacao

Para este projeto Java, os quality gates operacionais atuais sao:

```bash
mvn test
mvn verify
```

Evolucoes recomendadas para as proximas iteracoes:

- padronizacao automatica de estilo
- analise estatica adicional
- varredura de vulnerabilidades de dependencias
- pipeline CI com validacoes obrigatorias

## Roadmap Tecnico

Proximos passos naturais para elevar o CRM ao padrao de producao alvo:

- migrar a persistencia de bootstrap para PostgreSQL
- introduzir frontend React.js com Tailwind CSS
- formalizar DTOs de contrato e paginacao da API
- consolidar migracoes de banco para ambientes persistentes
- expandir observabilidade, seguranca e cobertura de testes

## Status do Projeto

O projeto ja possui uma baseline funcional para CRM com API, dashboard, seguranca inicial e integracao N8N. A fase atual e de profissionalizacao da base, com alinhamento progressivo ao AIOX e preparacao para stack de producao com React.js, Tailwind CSS e PostgreSQL.

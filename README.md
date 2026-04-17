<h1 align="center">
  <br>
  CRM AI Project
  <br>
</h1>

<p align="center">
  CRM comercial orientado a vendas e relacionamento, com backend em Java/Spring Boot, integracao com N8N e governanca AIOX para evolucao profissional e segura.
</p>

<p align="center">
  <img alt="Java" src="https://img.shields.io/badge/Java-17-ED8B00?style=flat-square&logo=openjdk&logoColor=white"/>
  <img alt="Spring Boot" src="https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=flat-square&logo=springboot&logoColor=white"/>
  <img alt="Next.js" src="https://img.shields.io/badge/Next.js-15-111827?style=flat-square&logo=nextdotjs&logoColor=white"/>
  <img alt="Tailwind CSS" src="https://img.shields.io/badge/Tailwind_CSS-3-06B6D4?style=flat-square&logo=tailwindcss&logoColor=white"/>
  <img alt="PostgreSQL" src="https://img.shields.io/badge/PostgreSQL-16-336791?style=flat-square&logo=postgresql&logoColor=white"/>
  <img alt="Maven" src="https://img.shields.io/badge/Maven-Build-C71A36?style=flat-square&logo=apachemaven&logoColor=white"/>
  <img alt="N8N" src="https://img.shields.io/badge/N8N-Integrated-EA4B71?style=flat-square&logo=n8n&logoColor=white"/>
  <img alt="AIOX" src="https://img.shields.io/badge/AIOX-Governed-2563EB?style=flat-square"/>
</p>

---

## Sobre o Projeto

O **CRM AI Project** e uma base de CRM voltada para operacoes comerciais que precisam centralizar contatos, deals, atividades e automacoes externas. O repositorio foi iniciado com apoio do Codex e esta sendo profissionalizado sob as diretrizes do **AIOX**, com foco em qualidade, rastreabilidade e evolucao segura.

Na implementacao atual, o projeto entrega uma API REST em **Java 17 + Spring Boot**, um frontend em **Next.js + React + Tailwind CSS**, autenticacao com usuarios persistidos no banco e envio de eventos para o **N8N** com trilha persistente de entrega. Em runtime, a persistencia passa a ser **PostgreSQL**, enquanto o **H2** fica restrito aos testes automatizados.

---

## Funcionalidades

### Gestao Comercial
- **Contatos** - cadastro e consulta de leads e clientes
- **Deals** - acompanhamento de oportunidades e mudanca de estagio do funil
- **Atividades** - registro de interacoes operacionais associadas ao processo comercial

### Dashboard
- Visualizacao centralizada de metricas operacionais do CRM
- Painel web para acompanhamento rapido da operacao

### API REST
- Endpoints para contatos, deals, atividades e metricas
- DTOs de entrada e resposta para o contrato HTTP
- Protecao por autenticacao no dashboard e na API com bootstrap inicial por variavel de ambiente

### Integracao N8N
- Publicacao automatica de eventos do CRM via webhook HTTP com persistencia de entrega
- Eventos para criacao de contatos, deals, mudanca de estagio e atividades

---

## Stack Tecnologica

| Camada | Tecnologia |
|--------|-----------|
| Linguagem | Java 17 |
| Framework backend | Spring Boot |
| API | Spring Web |
| Persistencia de runtime | Spring Data JPA + PostgreSQL |
| Migracoes | Flyway |
| Seguranca | Spring Security + usuarios persistidos |
| Frontend | Next.js + React |
| Estilizacao | Tailwind CSS |
| Build | Maven |
| Testes | JUnit + Mockito + Spring Boot Test |
| Integracao | N8N via webhook HTTP com outbox persistente e retry |

## Stack de Testes

| Camada | Tecnologia |
|--------|------------|
| Banco de testes | H2 |
| Testes unitarios | JUnit + Mockito |
| Testes de integracao | Spring Boot Test + MockMvc |

Importante: o **H2** foi mantido exclusivamente para os testes unitarios e de integracao. O runtime principal da aplicacao passa a usar **PostgreSQL**.

---

## Arquitetura

```text
crm-n8n-java/
|-- src/
|   |-- main/
|   |   |-- java/com/synkra/crm/
|   |   |   |-- config/        # Configuracao da aplicacao e seguranca
|   |   |   |-- controller/    # Dashboard, API REST e tratamento de erros
|   |   |   |-- dto/           # Requests e responses da API
|   |   |   |-- model/         # Entidades de dominio, usuarios e entregas de webhook
|   |   |   |-- repository/    # Repositorios JPA
|   |   |   `-- service/       # Regras de negocio e integracao N8N
|   |   `-- resources/
|   |       |-- static/ui/     # Export estatico do frontend Next.js
|   |       |-- db/migration/  # Migracoes Flyway para PostgreSQL
|   |       `-- application*.yml
|   `-- test/                  # Testes de aplicacao e seguranca
|-- docs/
|   |-- architecture/
|   |-- aiox/
|   |-- framework/
|   `-- stories/
|-- frontend/                  # Workspace Next.js + React + Tailwind
|-- deploy/                    # Docker Compose e proxy reverso
|-- AGENTS.md
`-- pom.xml
```

### Visao em Camadas

- **Apresentacao** - dashboard web e endpoints HTTP
- **Aplicacao** - orquestracao das regras de negocio de CRM
- **Persistencia** - entidades JPA, usuarios da aplicacao e acesso a dados
- **Integracao** - publicacao de eventos para N8N com persistencia de entrega
- **Governanca** - stories, documentacao arquitetural e padrao AIOX

---

## Configuracao e Execucao

### Pre-requisitos

- Java 17+
- Maven 3.9+
- Node.js 20+
- NPM 10+
- PostgreSQL 16+

### Rodando localmente

```bash
cd frontend
npm install

cd ..
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Observacao: o `mvn verify` sincroniza automaticamente o export estatico do frontend antes do empacotamento do `jar`, reduzindo drift entre `frontend/` e `src/main/resources/static/ui/`.

Acessos locais:

- Aplicacao: `http://localhost:8080`

### Perfis de runtime

- `dev` - PostgreSQL com logs SQL habilitados
- `default` - PostgreSQL com `ddl-auto=validate`
- `test` - H2 isolado para testes automatizados

---

## Variaveis de Ambiente

Credenciais iniciais:

```env
CRM_USERNAME=admin
CRM_PASSWORD=change-me-now
CRM_DATASOURCE_URL=jdbc:postgresql://localhost:5432/crm_ai_project
CRM_DATASOURCE_USERNAME=crm_app
CRM_DATASOURCE_PASSWORD=crm_app
CRM_BOOTSTRAP_ENABLED=true
```

Integracao com N8N:

```env
N8N_WEBHOOK_URL=https://seu-n8n/webhook/crm-events
N8N_RETRY_DELAY_MS=30000
N8N_RETRY_SCHEDULER_DELAY_MS=30000
N8N_MAX_ATTEMPTS=5
```

Antes de qualquer ambiente compartilhado, substitua as credenciais padrao por valores seguros.

---

## API REST

Base URL atual: `/api`

### Endpoints principais

| Metodo | Rota | Descricao |
|--------|------|-----------|
| `GET` | `/api/contacts` | Lista contatos |
| `POST` | `/api/contacts` | Cria contato |
| `GET` | `/api/deals` | Lista deals |
| `POST` | `/api/deals` | Cria deal |
| `PATCH` | `/api/deals/{id}/stage?stage=WON` | Atualiza estagio do deal |
| `GET` | `/api/activities` | Lista atividades |
| `POST` | `/api/activities` | Cria atividade |
| `GET` | `/api/dashboard/metrics` | Retorna metricas do dashboard |

---

## Integracao N8N

O CRM publica eventos automaticamente para workflows externos.

Cada tentativa de entrega fica registrada em banco com status, contador de tentativas, ultimo erro e proxima tentativa agendada quando a chamada falha.

### Eventos atualmente enviados

- `contact.created`
- `deal.created`
- `deal.stage.updated`
- `activity.created`

### Exemplo de payload

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

---

## Qualidade

Os quality gates operacionais atuais do projeto sao:

```bash
mvn test
mvn verify
```

Testes presentes na base:

- smoke test da aplicacao
- testes de integracao de seguranca da API
- testes unitarios de servico com Mockito

---

## Metodo AIOX

Este repositorio opera com governanca AIOX:

- desenvolvimento orientado por story
- acceptance criteria como contrato de entrega
- checklist e file list atualizados por iteracao
- documentacao arquitetural viva
- separacao clara entre codigo do produto e tooling local

Referencias principais:

- `AGENTS.md`
- `docs/architecture/current-system-map.md`
- `docs/architecture/brownfield-baseline.md`
- `docs/aiox/repository-boundary.md`
- `docs/stories/001-aiox-brownfield-hardening.md`
- `docs/stories/002-repository-boundary-and-slimming.md`
- `docs/stories/003-professional-readme-refresh.md`
- `docs/stories/004-readme-presentation-alignment.md`
- `docs/stories/005-nextjs-postgres-runtime.md`

---

## Roadmap

- [x] Migrar o runtime para PostgreSQL
- [x] Substituir o dashboard por Next.js + React + Tailwind CSS
- [ ] Formalizar DTOs de resposta e paginacao da API
- [ ] Consolidar migracoes para ambientes persistentes
- [ ] Ampliar observabilidade, seguranca e cobertura de testes

---

## Status do Projeto

O projeto agora possui uma baseline funcional com API Spring Boot, frontend em **Next.js + React**, autenticacao persistida, integracao com **N8N** com retry persistido e runtime em **PostgreSQL**. A etapa atual passa a ser o endurecimento continuo dessa base com mais observabilidade, contratos mais ricos e maior cobertura automatizada.

---

<p align="center">
  Construido com governanca <strong>AIOX</strong>
</p>

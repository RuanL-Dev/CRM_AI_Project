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
  <img alt="Maven" src="https://img.shields.io/badge/Maven-Build-C71A36?style=flat-square&logo=apachemaven&logoColor=white"/>
  <img alt="N8N" src="https://img.shields.io/badge/N8N-Integrated-EA4B71?style=flat-square&logo=n8n&logoColor=white"/>
  <img alt="AIOX" src="https://img.shields.io/badge/AIOX-Governed-2563EB?style=flat-square"/>
</p>

---

## Sobre o Projeto

O **CRM AI Project** e uma base de CRM voltada para operacoes comerciais que precisam centralizar contatos, deals, atividades e automacoes externas. O repositorio foi iniciado com apoio do Codex e esta sendo profissionalizado sob as diretrizes do **AIOX**, com foco em qualidade, rastreabilidade e evolucao segura.

Na implementacao atual, o projeto entrega uma API REST e um dashboard web server-rendered em **Java 17 + Spring Boot**, com persistencia bootstrap em **H2**, autenticacao basica e envio de eventos para o **N8N**.

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
- DTOs de entrada para criacao de entidades principais
- Protecao por autenticacao basica no dashboard e na API

### Integracao N8N
- Publicacao automatica de eventos do CRM via webhook HTTP
- Eventos para criacao de contatos, deals, mudanca de estagio e atividades

---

## Stack Tecnologica

| Camada | Tecnologia |
|--------|-----------|
| Linguagem | Java 17 |
| Framework backend | Spring Boot |
| API | Spring Web |
| Persistencia atual | Spring Data JPA + H2 |
| Seguranca | Spring Security |
| Frontend atual | Thymeleaf + JavaScript |
| Estilizacao atual | CSS customizado |
| Build | Maven |
| Testes | JUnit + Spring Boot Test |
| Integracao | N8N via webhook HTTP |

## Stack Alvo de Producao

| Camada | Tecnologia planejada |
|--------|----------------------|
| Frontend | React.js |
| Estilizacao | Tailwind CSS |
| Backend | Java + Spring Boot |
| Banco de dados de producao | PostgreSQL |
| Automacao | N8N |

Importante: **React.js**, **Tailwind CSS** e **PostgreSQL** representam a direcao de producao desejada. O estado atual do repositorio ainda utiliza frontend server-rendered e **H2** como persistencia de bootstrap.

---

## Arquitetura

```text
crm-n8n-java/
|-- src/
|   |-- main/
|   |   |-- java/com/synkra/crm/
|   |   |   |-- config/        # Configuracao da aplicacao e seguranca
|   |   |   |-- controller/    # Dashboard, API REST e tratamento de erros
|   |   |   |-- dto/           # Requests de entrada
|   |   |   |-- model/         # Entidades de dominio
|   |   |   |-- repository/    # Repositorios JPA
|   |   |   `-- service/       # Regras de negocio e integracao N8N
|   |   `-- resources/
|   |       |-- static/        # CSS e JavaScript do dashboard atual
|   |       |-- templates/     # Views server-rendered
|   |       `-- application*.yml
|   `-- test/                  # Testes de aplicacao e seguranca
|-- docs/
|   |-- architecture/
|   |-- aiox/
|   |-- framework/
|   `-- stories/
|-- AGENTS.md
`-- pom.xml
```

### Visao em Camadas

- **Apresentacao** - dashboard web e endpoints HTTP
- **Aplicacao** - orquestracao das regras de negocio de CRM
- **Persistencia** - entidades JPA e acesso a dados
- **Integracao** - publicacao de eventos para N8N
- **Governanca** - stories, documentacao arquitetural e padrao AIOX

---

## Configuracao e Execucao

### Pre-requisitos

- Java 17+
- Maven 3.9+

### Rodando localmente

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Acessos locais:

- Aplicacao: `http://localhost:8080`
- Console H2: `http://localhost:8080/h2-console`

### Perfis de runtime

- `dev` - H2 em memoria, console H2 habilitado e `ddl-auto=update`
- `default` - configuracao mais segura, sem console H2 e com `ddl-auto=validate`
- `test` - H2 isolado para testes automatizados

---

## Variaveis de Ambiente

Credenciais iniciais:

```env
CRM_USERNAME=admin
CRM_PASSWORD=change-me-now
```

Integracao com N8N:

```env
N8N_WEBHOOK_URL=https://seu-n8n/webhook/crm-events
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
- `docs/architecture/brownfield-baseline.md`
- `docs/aiox/repository-boundary.md`
- `docs/stories/001-aiox-brownfield-hardening.md`
- `docs/stories/002-repository-boundary-and-slimming.md`
- `docs/stories/003-professional-readme-refresh.md`
- `docs/stories/004-readme-presentation-alignment.md`

---

## Roadmap

- [ ] Migrar a persistencia de bootstrap para PostgreSQL
- [ ] Introduzir frontend React.js com Tailwind CSS
- [ ] Formalizar DTOs de resposta e paginacao da API
- [ ] Consolidar migracoes para ambientes persistentes
- [ ] Ampliar observabilidade, seguranca e cobertura de testes

---

## Status do Projeto

O projeto ja possui uma baseline funcional com API, dashboard, seguranca inicial e integracao com N8N. A etapa atual e de amadurecimento estrutural, mantendo a implementacao existente enquanto a arquitetura evolui para uma stack de producao com **React.js**, **Tailwind CSS** e **PostgreSQL**.

---

<p align="center">
  Construido com governanca <strong>AIOX</strong>
</p>

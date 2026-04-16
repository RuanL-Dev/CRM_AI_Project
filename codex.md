# CODEX Knowledge Base

## Visão Geral

Este projeto é um CRM brownfield evoluído com baseline AIOX.

Stack atual:
- Backend: Java 17 + Spring Boot
- Frontend: Next.js + React + Tailwind CSS
- Banco de runtime: PostgreSQL
- Banco de testes: H2
- Migrações: Flyway
- Testes: JUnit + Mockito

Objetivo atual do produto:
- operar um CRM com dashboard visual
- cadastrar contatos, oportunidades e atividades
- exibir indicadores comerciais em PT-BR
- manter visual dark mode futurista

## Arquitetura

Backend:
- expõe APIs REST em `/api`
- serve a UI exportada estaticamente em `/ui`
- usa autenticação Spring Security com login customizado

Frontend:
- código fonte em `frontend/`
- build exportado para `src/main/resources/static/ui/`
- dashboard principal em [frontend/app/page.jsx](C:/Users/ruanl/Operacao_IA_curso/Teste_IA_CodigoOuro/crm-n8n-java/frontend/app/page.jsx)

Login:
- página customizada em [src/main/resources/static/auth/login.html](C:/Users/ruanl/Operacao_IA_curso/Teste_IA_CodigoOuro/crm-n8n-java/src/main/resources/static/auth/login.html)
- estilização em [src/main/resources/static/auth/login.css](C:/Users/ruanl/Operacao_IA_curso/Teste_IA_CodigoOuro/crm-n8n-java/src/main/resources/static/auth/login.css)

## Fluxo Funcional

Fluxos já implementados:
- login com usuário e senha
- visualizar dashboard com métricas
- criar contato
- criar oportunidade vinculada a um contato existente
- criar atividade vinculada a um contato existente
- atualizar dashboard após gravação

Dashboard atual:
- leads por período
- registros criados por dia
- distribuição do pipeline
- mix operacional
- prioridades do dia
- base comercial

## Persistência e Dados

Banco de runtime:
- PostgreSQL
- URL configurada por variável `CRM_DATASOURCE_URL`

Banco de testes:
- H2 somente no perfil `test`

Seed de desenvolvimento:
- implementado em [src/main/java/com/synkra/crm/config/DevDataSeeder.java](C:/Users/ruanl/Operacao_IA_curso/Teste_IA_CodigoOuro/crm-n8n-java/src/main/java/com/synkra/crm/config/DevDataSeeder.java)
- ativo apenas no perfil `dev`
- controlado por `app.seed-demo-data.enabled`

## Segurança

Autenticação:
- Spring Security com `formLogin`
- endpoint de login: `/login`

CSRF:
- token obtido por `/api/security/csrf`
- frontend já integrado ao fluxo CSRF

Credenciais locais padrão:
- usuário: `admin`
- senha: `change-me-now`

## Configuração

Arquivos principais:
- [src/main/resources/application.yml](C:/Users/ruanl/Operacao_IA_curso/Teste_IA_CodigoOuro/crm-n8n-java/src/main/resources/application.yml)
- [src/main/resources/application-dev.yml](C:/Users/ruanl/Operacao_IA_curso/Teste_IA_CodigoOuro/crm-n8n-java/src/main/resources/application-dev.yml)
- [src/test/resources/application-test.yml](C:/Users/ruanl/Operacao_IA_curso/Teste_IA_CodigoOuro/crm-n8n-java/src/test/resources/application-test.yml)

Variáveis importantes:
- `CRM_DATASOURCE_URL`
- `CRM_DATASOURCE_USERNAME`
- `CRM_DATASOURCE_PASSWORD`
- `CRM_USERNAME`
- `CRM_PASSWORD`
- `N8N_WEBHOOK_URL`

## Execução Local

Frontend:
```bash
cd frontend
npm run build
```

Backend:
```bash
mvn test
mvn verify
java -jar target/crm-n8n-java-0.1.0.jar --spring.profiles.active=dev
```

Exemplo de PostgreSQL local usado no projeto:
- host: `localhost`
- porta: `5434`
- banco: `crm_ai_project`

## Qualidade

Validações usadas no projeto:
- `npm run build`
- `mvn test`
- `mvn verify`

Cobertura já adicionada:
- autenticação
- CSRF
- criação de contato
- criação de oportunidade
- criação de atividade
- erro por e-mail duplicado

## Problemas já resolvidos

- tela de login padrão do Spring removida
- dashboard traduzido para PT-BR
- visual dark mode futurista implementado
- vulnerabilidade alta do Next.js corrigida por upgrade controlado
- erro de gravação por `Content-Type` incorreto corrigido
- erro genérico no fluxo normal reduzido com handlers mais específicos
- necessidade de digitar ID manual para oportunidade/atividade removida

## Pontos de Atenção

- o projeto ainda usa export estático do frontend; qualquer mudança visual precisa passar por `npm run build`
- se a aplicação estiver rodando, `mvn verify` pode falhar no `repackage` por lock do `.jar`
- o processo local em `:8080` pode precisar ser religado manualmente após rebuilds
- para tabelas em telas menores, ainda vale revisar continuamente a UX responsiva

## Stories Relevantes

- `005-nextjs-postgres-runtime`
- `006-custom-login-and-business-copy`
- `007-futuristic-ptbr-dashboard-analytics`
- `008-login-theme-demo-data-and-live-forms`
- `009-dashboard-bugfixes-and-live-save`

## Regra de Trabalho

Este projeto vem sendo conduzido em modo brownfield com AIOX:
- criar ou atualizar story antes de mudanças relevantes
- implementar apenas o escopo pedido
- validar com quality gates
- manter documentação e checklist coerentes

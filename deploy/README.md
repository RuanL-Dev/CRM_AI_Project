# Deploy

Este diretorio versiona a configuracao de deploy usada para publicar o CRM no servidor.

Arquivos:
- `docker-compose.yml`: sobe PostgreSQL e a aplicacao Spring Boot em Docker
- `.env.example`: modelo das variaveis exigidas em producao
- `Caddyfile`: bloco de proxy reverso de exemplo para publicar o CRM em um dominio proprio
- `deploy.local.ps1.example`: modelo da configuracao local usada pelo script de deploy pos-commit

Principios:
- nenhuma credencial real e versionada
- o backend publica apenas em `127.0.0.1:8081`
- o acesso publico deve ser feito por um proxy reverso no host
- o arquivo `.env` remoto e gerado localmente durante o deploy e removido do workspace ao final
- o projeto nao usa mais GitHub Actions para publicar a aplicacao

## Fluxo atual

O deploy agora roda localmente a cada novo commit, via hook versionado em `.githooks/post-commit`, que chama `scripts/deploy-on-commit.ps1`.

1. `mvn verify`
2. upload do `jar` e dos arquivos versionados de deploy para o servidor
3. refresh do `.env` remoto a partir da configuracao local em `.local/deploy.local.ps1`
4. validacao de checksum do `jar`
5. `docker compose up -d --force-recreate crm-postgres crm-app`
6. validacao de `http://127.0.0.1:8081/healthz`
7. validacao do endpoint publico `/login`

### Configuracao local obrigatoria

- `CRM_DEPLOY_HOST`
- `CRM_DEPLOY_PORT`
- `CRM_DEPLOY_USER`
- `CRM_DEPLOY_PATH`
- `CRM_DEPLOY_SSH_PRIVATE_KEY` ou `CRM_DEPLOY_SSH_KEY_PATH`
- `CRM_DEPLOY_KNOWN_HOSTS` ou `CRM_DEPLOY_KNOWN_HOSTS_PATH`
- `CRM_PUBLIC_LOGIN_URL`
- `CRM_POSTGRES_DB`
- `CRM_POSTGRES_USER`
- `CRM_POSTGRES_PASSWORD`
- `CRM_APP_PROFILE`
- `CRM_APP_USERNAME`
- `CRM_APP_PASSWORD`
- `CRM_BOOTSTRAP_ENABLED`
- `N8N_WEBHOOK_URL`
- `N8N_CONNECT_TIMEOUT_MS`
- `N8N_READ_TIMEOUT_MS`
- `N8N_RETRY_DELAY_MS`
- `N8N_RETRY_SCHEDULER_DELAY_MS`
- `N8N_MAX_ATTEMPTS`

### Ativacao

1. Copie `deploy/deploy.local.ps1.example` para `.local/deploy.local.ps1`.
2. Preencha as credenciais reais do servidor e da aplicacao.
3. Ative o hook com `git config core.hooksPath .githooks`.
4. Faca um commit normal; o deploy sera executado ao final do commit.

Passos esperados no servidor:
1. Copiar `.env.example` para `.env` e preencher com segredos reais.
2. Publicar o `jar` da aplicacao no mesmo diretorio do `docker-compose.yml`.
3. Subir a stack com `docker-compose up -d`.
4. Mesclar o bloco do `Caddyfile` ao Caddy do host e recarregar o servico.

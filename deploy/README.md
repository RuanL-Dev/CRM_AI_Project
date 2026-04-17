# Deploy

Este diretório versiona a configuração de deploy usada para publicar o CRM no servidor.

Arquivos:
- `docker-compose.yml`: sobe PostgreSQL e a aplicação Spring Boot em Docker
- `.env.example`: modelo das variáveis exigidas em produção
- `Caddyfile`: bloco de proxy reverso de exemplo para publicar o CRM em um domínio próprio
- `.github/workflows/deploy-production.yml`: pipeline de build, publicação e restart via GitHub Actions

Princípios:
- nenhuma credencial real é versionada
- o backend publica apenas em `127.0.0.1:8081`
- o acesso público deve ser feito por um proxy reverso no host
- o arquivo `.env` de produção é gerado a partir de GitHub Secrets durante o deploy

## GitHub Actions

O deploy automático roda a cada push em `main` e executa:

1. `npm ci` no frontend
2. `mvn verify`
3. upload do `jar` e dos arquivos versionados de deploy para o servidor
4. refresh do `.env` remoto a partir dos GitHub Secrets
5. `docker compose up -d --force-recreate crm-postgres crm-app`
6. validação do endpoint público `/login`

### GitHub Secrets obrigatórios

- `CRM_DEPLOY_HOST`
- `CRM_DEPLOY_PORT`
- `CRM_DEPLOY_USER`
- `CRM_DEPLOY_PATH`
- `CRM_DEPLOY_SSH_PRIVATE_KEY`
- `CRM_DEPLOY_KNOWN_HOSTS`
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

### Recomendação operacional

- Use uma chave SSH dedicada ao GitHub Actions, sem senha interativa, com escopo restrito ao deploy.
- Armazene a fingerprint do host remoto em `CRM_DEPLOY_KNOWN_HOSTS`.
- Não use segredos reais em `deploy/.env.example`, `docker-compose.yml` ou workflows versionados.

Passos esperados no servidor:
1. Copiar `.env.example` para `.env` e preencher com segredos reais.
2. Publicar o `jar` da aplicação no mesmo diretório do `docker-compose.yml`.
3. Subir a stack com `docker-compose up -d`.
4. Mesclar o bloco do `Caddyfile` ao Caddy do host e recarregar o serviço.

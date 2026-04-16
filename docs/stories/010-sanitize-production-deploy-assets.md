# Story 010 - Sanitize Production Deploy Assets

## Status

- [x] Registrar no repositório os arquivos de deploy usados em produção
- [x] Remover credenciais fixas e substituir por variáveis de ambiente
- [x] Documentar o uso do proxy reverso e do `.env` de produção
- [x] Atualizar a file list da story

## Acceptance Criteria

1. Os arquivos de deploy ficam versionados no repositório sem expor usuário, senha ou chave real.
2. O `docker-compose` representa a topologia efetivamente usada no servidor.
3. Existe documentação mínima para preencher variáveis e publicar o CRM em produção.

## File List

- `deploy/docker-compose.yml`
- `deploy/.env.example`
- `deploy/Caddyfile`
- `deploy/README.md`

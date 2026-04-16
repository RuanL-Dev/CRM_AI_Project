# Deploy

Este diretório versiona a configuração de deploy usada para publicar o CRM no servidor.

Arquivos:
- `docker-compose.yml`: sobe PostgreSQL e a aplicação Spring Boot em Docker
- `.env.example`: modelo das variáveis exigidas em produção
- `Caddyfile`: bloco de proxy reverso de exemplo para publicar o CRM em um domínio próprio

Princípios:
- nenhuma credencial real é versionada
- o backend publica apenas em `127.0.0.1:8081`
- o acesso público deve ser feito por um proxy reverso no host

Passos esperados no servidor:
1. Copiar `.env.example` para `.env` e preencher com segredos reais.
2. Publicar o `jar` da aplicação no mesmo diretório do `docker-compose.yml`.
3. Subir a stack com `docker-compose up -d`.
4. Mesclar o bloco do `Caddyfile` ao Caddy do host e recarregar o serviço.

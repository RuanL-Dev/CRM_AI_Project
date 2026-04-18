# Security Baseline

## Objective

Este documento resume a baseline atual de seguranca do CRM para acelerar futuras analises tecnicas, code reviews e hardening stories.

## AIOX Alignment

- Hardening entra por story dedicada e rastreavel.
- Controles reais valem mais do que checklist aspiracional.
- Defaults inseguros, boundary frouxo e governanca opaca devem ser tratados como defeitos de baseline.
- O objetivo nao e declarar o produto "imune", mas reduzir superficie de ataque e deixar riscos residuais claros.

## Current Control Areas

### Authentication

- Spring Security com formulario de login customizado.
- Usuarios carregados do banco em `app_users`.
- Bootstrap inicial controlado por variaveis de ambiente.
- Protecao adicional contra repeticao de falhas no endpoint de login.

### Session and Browser Surface

- Fluxo web autenticado por sessao.
- CSRF exigido para operacoes mutantes.
- Headers explicitos para CSP, HSTS, framing, referrer policy e permissions policy.
- Logout limpa sessao e cookies associados.

### Configuration and Secrets

- `application.yml` deixa de trazer credenciais padrao inseguras.
- Defaults locais ficam restritos ao perfil `dev`.
- Ambientes nao-locais passam por guardrails de senha e configuracao.
- Segredos operacionais devem ficar apenas em `.env`, secrets do GitHub Actions ou no servidor.

### Deployment Surface

- Proxy HTTPS continua sendo a borda publica esperada.
- A aplicacao fica configurada para interpretar forwarded headers do proxy.
- Seguranca operacional ainda depende de higiene do host, rotacao de credenciais e atualizacao do runtime.

## Implemented Baseline Controls

- Remocao de credenciais padrao inseguras do runtime base.
- Validacao de credenciais fracas fora de `dev` e `test`.
- Separacao entre resposta nao autenticada de API e UI.
- Rate limiting de login por janela curta para reduzir brute force trivial.
- Endurecimento de headers HTTP e comportamento de sessao.

## Known Constraints

- O bloqueio de login e em memoria do processo; ele nao substitui rate limiting distribuido no proxy.
- O frontend exportado pelo Next.js ainda exige uma CSP pragmatica com `unsafe-inline` para scripts e estilos gerados.
- Nao existe MFA, federacao de identidade, RBAC granular ou auditoria centralizada.
- A seguranca do deploy continua dependendo de Docker, host Linux, Caddy e GitHub Secrets bem mantidos.

## Residual Risks

- Comprometimento do servidor, da cadeia de build ou do navegador do usuario continua fora do alcance do app.
- Credenciais fortes e rotacao operacional continuam obrigatorias.
- Dependencias e imagens de runtime precisam de atualizacao recorrente para reduzir CVEs conhecidas.
- Integracoes externas como N8N seguem ampliando superficie de ataque e precisam de configuracao propria segura.

## Review Triggers

- Mudanca em login, sessao, proxys, deploy ou integrações externas.
- Introducao de novos endpoints mutantes ou uploads.
- Inclusao de novos terceiros no frontend.
- Mudanca de estrategia de identidade, autorizacao ou secrets management.

## External Reference Set

- OWASP ASVS: https://owasp.org/www-project-application-security-verification-standard/
- OWASP Authentication Cheat Sheet: https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html
- OWASP Session Management Cheat Sheet: https://cheatsheetseries.owasp.org/cheatsheets/Session_Management_Cheat_Sheet.html
- OWASP Secrets Management Cheat Sheet: https://cheatsheetseries.owasp.org/cheatsheets/Secrets_Management_Cheat_Sheet.html
- OWASP Transport Layer Security Cheat Sheet: https://cheatsheetseries.owasp.org/cheatsheets/Transport_Layer_Security_Cheat_Sheet.html
- Spring Security Reference: https://docs.spring.io/spring-security/reference/
- NIST SP 800-63B: https://pages.nist.gov/800-63-4/sp800-63b.html
- CISA Secure by Design: https://www.cisa.gov/securebydesign

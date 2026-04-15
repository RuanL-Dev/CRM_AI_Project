# AGENTS.md - CRM AI Project

Este repositório usa o CRM como produto principal e o AIOX como método de trabalho.

## Regras Locais

- Siga desenvolvimento orientado por story em `docs/stories/`.
- Não implemente fora do que os acceptance criteria da story pedirem.
- Atualize checklist e file list na story antes de concluir.
- Rode os quality gates do projeto Java antes de publicar:

```bash
mvn test
mvn verify
```

## Limite do Repositório

- `src/`, `pom.xml`, `README.md`, `docs/` e arquivos de configuração do app pertencem ao produto.
- Artefatos pesados de integração de IDE, agentes locais e scaffolding de framework não devem ser versionados aqui por padrão.
- Se uma futura automação do projeto realmente depender de algum artefato AIOX versionado, isso deve entrar por story específica.

## Direção Arquitetural

- Projeto tratado como brownfield.
- Hardening e governança vêm antes de expansão funcional.
- Mudanças grandes devem nascer de novas stories, não de edits ad hoc.

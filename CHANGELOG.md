# Changelog

Todas as mudanças relevantes deste projeto ficam registradas aqui.
O formato segue o [Keep a Changelog](https://keepachangelog.com/pt-BR/1.1.0/) e as versões seguem o [Versionamento Semântico](https://semver.org/lang/pt-BR/).

## [1.0.0] - 2026-11-27

### Adicionado

- Laboratório de busca semântica com o Qdrant em Java 21: CRUD completo (`criar`, `inserir-lote`, `inserir-novo`, `buscar`, `atualizar`, `deletar`) pela linha de comando.
- Roteiro da demonstração ao vivo (`./demo.sh`), com pausa a cada passo.
- Modelo de embedding multilíngue (ONNX, 384 dimensões) executado dentro da JVM e script para baixá-lo.
- Busca vetorial com filtros de categoria e preço numa única consulta.
- Parâmetros em `application.properties`, sobrescrevíveis por variáveis de ambiente.
- Qdrant 1.20.2 em Docker (`docker-compose.yml`) e catálogo de 30 produtos.
- Testes unitários e de integração (Testcontainers), cobertura com JaCoCo e Checkstyle.

[1.0.0]: https://github.com/MarilliaBraz/seminario_qdrant_lab/releases/tag/v1.0.0

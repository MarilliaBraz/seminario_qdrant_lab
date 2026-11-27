# seminario_qdrant_lab

Laboratório de **busca semântica** com o banco de dados vetorial **[Qdrant](https://qdrant.tech)** em **Java 21**: uma loja on-line em que o cliente encontra produtos pelo **significado** da frase, não por palavra-chave.

> "algo para ouvir música enquanto corro" → **Fone Bluetooth Esportivo**, sem nenhuma palavra em comum.

Trabalho do seminário **Ecossistema NoSQL, Data Lakes, Data Warehouse e BI** (Tema 17: NoSQL vetorial), de **Marillia Braz** e **Maria Eduarda**.

## Stack

- **Linguagem:** Java 21 (LTS)
- **Build:** Maven, com JAR executável único (maven-shade-plugin)
- **Banco vetorial:** Qdrant 1.20 em Docker, acessado pelo cliente oficial `io.qdrant:client` (gRPC, porta 6334)
- **Modelo de IA:** `paraphrase-multilingual-MiniLM-L12-v2` (384 dimensões, formato ONNX), executado **dentro da JVM** pelo LangChain4j, sem Python
- **Testes:** JUnit 5 e Testcontainers (Qdrant real em contêiner), cobertura com JaCoCo
- **Qualidade:** Checkstyle com as regras de nomenclatura das convenções

## Arquitetura

Organização em `core` (infraestrutura genérica, sem regra de negócio) e `business` (regras por domínio), seguindo o `CONVENTIONS.md` do repositório [`mile_bag_audite`](https://github.com/MarilliaBraz/mile_bag_audite). `core` nunca depende de `business`.

```
src/main/java/br/edu/seminario/qdrant/
├── QdrantLabApplication.java        # ponto de entrada: monta as dependências e despacha o comando
├── core/                            # genérico, reutilizável
│   ├── config/
│   │   ├── Configuracao.java        # application.properties + variáveis de ambiente
│   │   └── QdrantClienteFactory.java# conexão gRPC com verificação de saúde
│   ├── embedding/
│   │   ├── GeradorEmbeddings.java   # texto -> vetor (abstração)
│   │   └── GeradorEmbeddingsOnnx.java
│   └── util/                        # Argumentos, Futuros (async do cliente), Terminal (saída UTF-8)
└── business/
    ├── produto/
    │   ├── controller/              # comandos de linha de comando e formatação da saída
    │   ├── service/                 # ProdutoService: regras do CRUD (ex.: desconto padrão)
    │   ├── repository/              # ProdutoRepository + ProdutoRepositoryQdrant, catálogo JSON
    │   ├── model/                   # Produto, FiltroBusca, ResultadoBusca...
    │   └── mapper/                  # Produto <-> payload JSON do Qdrant
    └── demo/controller/             # roteiro da demonstração ao vivo
```

Outros arquivos: `docker-compose.yml` (Qdrant v1.20.2), `dados/produtos.json` (30 produtos), `baixar_modelo.sh`, `demo.sh` e `src/main/resources/application.properties`.

### Equivalência com SQL

| Operação | SQL | Qdrant (Java) — `ProdutoRepositoryQdrant` |
|---|---|---|
| Criar estrutura | `CREATE TABLE` | `createCollectionAsync` (tamanho do vetor + métrica cosseno + HNSW) |
| Inserir | `INSERT` | `upsertAsync` (ID + vetor + payload JSON) |
| Consultar | `SELECT ... WHERE` | `queryAsync` (vetor da pergunta + `Filter`, numa única chamada) |
| Atualizar | `UPDATE` | `setPayloadAsync` (sem recalcular o vetor) |
| Remover | `DELETE` | `deleteAsync` (por ID ou por filtro) |

## Rodando localmente

Pré-requisitos: JDK 21, Maven e Docker.

```bash
docker compose up -d --wait     # sobe o Qdrant (REST 6333, gRPC 6334)
./baixar_modelo.sh              # baixa o modelo de IA (~250 MB, uma única vez)
mvn package                     # roda os testes e gera target/qdrant-lab.jar
./demo.sh                       # demonstração guiada: ENTER avança cada passo
```

Depois do download do modelo, tudo roda **offline**. Painel web do Qdrant: <http://localhost:6333/dashboard>.

### Comandos

```bash
java -jar target/qdrant-lab.jar criar                      # CREATE: recria a coleção vazia
java -jar target/qdrant-lab.jar inserir-lote               # INSERT: os 30 produtos do catálogo
java -jar target/qdrant-lab.jar inserir-novo 40 "Patinete Elétrico" "Veículo leve para a cidade" eletronicos 2999 6
java -jar target/qdrant-lab.jar buscar "presente para quem gosta de ficar em casa" --categoria casa
java -jar target/qdrant-lab.jar buscar "cuidar da pele" --preco-max 100 --limite 5
java -jar target/qdrant-lab.jar atualizar 7 499,90 30      # UPDATE: id, preço e estoque
java -jar target/qdrant-lab.jar atualizar                  # sem argumentos: produto 1 com 20% de desconto
java -jar target/qdrant-lab.jar deletar 12                 # DELETE por ID
java -jar target/qdrant-lab.jar deletar --categoria pets   # DELETE por filtro
```

### Configuração

Os parâmetros ficam em `src/main/resources/application.properties` (host e porta do Qdrant, nome da coleção, parâmetros do HNSW, pasta do modelo, desconto padrão etc.). Qualquer chave pode ser trocada por variável de ambiente, em maiúsculas e com `.` e `-` virando `_`:

```bash
QDRANT_HOST=192.168.0.10 QDRANT_PORTA_GRPC=6334 java -jar target/qdrant-lab.jar buscar "fone"
```

## Testes

```bash
mvn test        # 53 testes; relatório de cobertura em target/site/jacoco/index.html
```

- **Unitários:** serviço, controllers, roteiro da demo, configuração e conversões, com um repositório em memória e um gerador de embeddings determinístico (não precisam de Docker nem do modelo).
- **Integração:** `ProdutoRepositoryQdrantTest` sobe um Qdrant real com Testcontainers e testa CREATE, INSERT, busca com filtros, UPDATE e DELETE. Sem Docker, esse teste é pulado.

## Convenções

Este repositório segue o [`CONVENTIONS.md`](https://github.com/MarilliaBraz/mile_bag_audite/blob/main/CONVENTIONS.md) do `mile_bag_audite`: estrutura `core`/`business`, princípios SOLID, nomenclatura, parametrização fora do código, SemVer com [`CHANGELOG.md`](./CHANGELOG.md) e Conventional Commits. Para ativar os hooks de commit depois de clonar:

```bash
git config core.hooksPath .githooks
```

## Licença

Todos os direitos reservados. Ver [`LICENSE`](./LICENSE).

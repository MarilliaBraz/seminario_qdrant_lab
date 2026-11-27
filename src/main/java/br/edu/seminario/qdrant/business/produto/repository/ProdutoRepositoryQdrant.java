package br.edu.seminario.qdrant.business.produto.repository;

import static br.edu.seminario.qdrant.core.util.Futuros.aguardar;
import static io.qdrant.client.ConditionFactory.matchKeyword;
import static io.qdrant.client.ConditionFactory.range;
import static io.qdrant.client.PointIdFactory.id;
import static io.qdrant.client.QueryFactory.nearest;
import static io.qdrant.client.ValueFactory.value;
import static io.qdrant.client.VectorsFactory.vectors;
import static io.qdrant.client.WithPayloadSelectorFactory.enable;

import br.edu.seminario.qdrant.business.produto.mapper.ProdutoMapper;
import br.edu.seminario.qdrant.business.produto.model.FiltroBusca;
import br.edu.seminario.qdrant.business.produto.model.Produto;
import br.edu.seminario.qdrant.business.produto.model.ResultadoBusca;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Collections.CreateCollection;
import io.qdrant.client.grpc.Collections.Distance;
import io.qdrant.client.grpc.Collections.HnswConfigDiff;
import io.qdrant.client.grpc.Collections.PayloadSchemaType;
import io.qdrant.client.grpc.Collections.VectorParams;
import io.qdrant.client.grpc.Collections.VectorsConfig;
import io.qdrant.client.grpc.Common.Condition;
import io.qdrant.client.grpc.Common.Filter;
import io.qdrant.client.grpc.Common.Range;
import io.qdrant.client.grpc.Points.PointStruct;
import io.qdrant.client.grpc.Points.QueryPoints;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Implementação do repositório no Qdrant, pelo cliente oficial em Java (gRPC). */
public final class ProdutoRepositoryQdrant implements ProdutoRepository {

    private final QdrantClient cliente;
    private final String colecao;
    private final int hnswM;
    private final int hnswEfConstruct;

    public ProdutoRepositoryQdrant(QdrantClient cliente, String colecao, int hnswM, int hnswEfConstruct) {
        this.cliente = cliente;
        this.colecao = colecao;
        this.hnswM = hnswM;
        this.hnswEfConstruct = hnswEfConstruct;
    }

    // ---------------------------------------------------------------- CREATE

    @Override
    public void criarColecao(int dimensaoVetor) {
        if (aguardar(cliente.collectionExistsAsync(colecao))) {
            aguardar(cliente.deleteCollectionAsync(colecao));
        }
        aguardar(cliente.createCollectionAsync(CreateCollection.newBuilder()
                .setCollectionName(colecao)
                .setVectorsConfig(VectorsConfig.newBuilder()
                        .setParams(VectorParams.newBuilder().setSize(dimensaoVetor).setDistance(Distance.Cosine)))
                .setHnswConfig(HnswConfigDiff.newBuilder().setM(hnswM).setEfConstruct(hnswEfConstruct))
                .build()));

        // Índices de payload: permitem filtrar por categoria e preço durante a busca vetorial
        aguardar(cliente.createPayloadIndexAsync(
                colecao, ProdutoMapper.CAMPO_CATEGORIA, PayloadSchemaType.Keyword, null, true, null, null));
        aguardar(cliente.createPayloadIndexAsync(
                colecao, ProdutoMapper.CAMPO_PRECO, PayloadSchemaType.Float, null, true, null, null));
    }

    // ---------------------------------------------------------------- INSERT

    @Override
    public void inserir(List<Produto> produtos, List<List<Float>> vetores) {
        if (produtos.size() != vetores.size()) {
            throw new IllegalArgumentException("Cada produto precisa de exatamente um vetor");
        }
        List<PointStruct> pontos = new ArrayList<>();
        for (int i = 0; i < produtos.size(); i++) {
            Produto produto = produtos.get(i);
            pontos.add(PointStruct.newBuilder()
                    .setId(id(produto.id()))
                    .setVectors(vectors(vetores.get(i)))
                    .putAllPayload(ProdutoMapper.paraPayload(produto))
                    .build());
        }
        aguardar(cliente.upsertAsync(colecao, pontos));
    }

    // ---------------------------------------------------------------- READ

    /** O vetor da pergunta e o filtro vão juntos numa única chamada: o Qdrant filtra durante a busca. */
    @Override
    public List<ResultadoBusca> buscar(List<Float> vetorConsulta, FiltroBusca filtro, int limite) {
        QueryPoints.Builder consulta = QueryPoints.newBuilder()
                .setCollectionName(colecao)
                .setQuery(nearest(vetorConsulta))
                .setLimit(limite)
                .setWithPayload(enable(true));
        montarFiltro(filtro).ifPresent(consulta::setFilter);

        return aguardar(cliente.queryAsync(consulta.build())).stream()
                .map(ponto -> new ResultadoBusca(
                        ProdutoMapper.dePayload(ponto.getId().getNum(), ponto.getPayloadMap()), ponto.getScore()))
                .toList();
    }

    @Override
    public Optional<Produto> buscarPorId(long idProduto) {
        return aguardar(cliente.retrieveAsync(colecao, List.of(id(idProduto)), true, false, null)).stream()
                .findFirst()
                .map(ponto -> ProdutoMapper.dePayload(ponto.getId().getNum(), ponto.getPayloadMap()));
    }

    @Override
    public long contar() {
        return aguardar(cliente.countAsync(colecao, null, true));
    }

    // ---------------------------------------------------------------- UPDATE

    @Override
    public void atualizarPrecoEEstoque(long idProduto, double novoPreco, long novoEstoque) {
        aguardar(cliente.setPayloadAsync(
                colecao,
                Map.of(ProdutoMapper.CAMPO_PRECO, value(novoPreco), ProdutoMapper.CAMPO_ESTOQUE, value(novoEstoque)),
                List.of(id(idProduto)),
                true, null, null));
    }

    // ---------------------------------------------------------------- DELETE

    @Override
    public void deletarPorId(long idProduto) {
        aguardar(cliente.deleteAsync(colecao, List.of(id(idProduto))));
    }

    @Override
    public void deletarPorCategoria(String categoria) {
        aguardar(cliente.deleteAsync(colecao, Filter.newBuilder()
                .addMust(matchKeyword(ProdutoMapper.CAMPO_CATEGORIA, categoria))
                .build()));
    }

    private static Optional<Filter> montarFiltro(FiltroBusca filtro) {
        if (filtro == null || filtro.isVazio()) {
            return Optional.empty();
        }
        List<Condition> condicoes = new ArrayList<>();
        if (filtro.hasCategoria()) {
            condicoes.add(matchKeyword(ProdutoMapper.CAMPO_CATEGORIA, filtro.categoria()));
        }
        if (filtro.hasPrecoMaximo()) {
            condicoes.add(range(ProdutoMapper.CAMPO_PRECO, Range.newBuilder().setLte(filtro.precoMaximo()).build()));
        }
        return Optional.of(Filter.newBuilder().addAllMust(condicoes).build());
    }
}

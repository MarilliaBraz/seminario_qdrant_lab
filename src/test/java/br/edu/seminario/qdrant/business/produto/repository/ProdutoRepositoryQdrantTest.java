package br.edu.seminario.qdrant.business.produto.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.edu.seminario.qdrant.business.produto.model.FiltroBusca;
import br.edu.seminario.qdrant.business.produto.model.Produto;
import br.edu.seminario.qdrant.business.produto.model.ResultadoBusca;
import br.edu.seminario.qdrant.core.config.QdrantClienteFactory;
import br.edu.seminario.qdrant.suporte.GeradorEmbeddingsFalso;
import io.qdrant.client.QdrantClient;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.qdrant.QdrantContainer;

/** Integração com um Qdrant de verdade em contêiner, na mesma versão do docker-compose.yml. Sem Docker, é pulado. */
@Testcontainers(disabledWithoutDocker = true)
class ProdutoRepositoryQdrantTest {

    @Container
    private static final QdrantContainer QDRANT = new QdrantContainer("qdrant/qdrant:v1.20.2");

    private static final GeradorEmbeddingsFalso EMBEDDINGS = new GeradorEmbeddingsFalso();
    private static final List<Produto> PRODUTOS = List.of(
            new Produto(1, "Fone Bluetooth Esportivo", "Fone para correr ouvindo música", "eletronicos", 199.90, 35),
            new Produto(2, "Caixa de Som Portátil", "Som para levar à praia", "eletronicos", 349.00, 20),
            new Produto(3, "Protetor Solar", "Proteção para a pele na praia", "beleza", 59.90, 70),
            new Produto(4, "Ração para Cães", "Alimento para cachorro", "pets", 239.00, 28));

    private static QdrantClient cliente;
    private ProdutoRepositoryQdrant repositorio;

    @BeforeAll
    static void conectar() {
        cliente = QdrantClienteFactory.conectar(QDRANT.getHost(), QDRANT.getMappedPort(6334));
    }

    @AfterAll
    static void desconectar() {
        cliente.close();
    }

    @BeforeEach
    void prepararColecao() {
        repositorio = new ProdutoRepositoryQdrant(cliente, "produtos_teste", 16, 100);
        repositorio.criarColecao(GeradorEmbeddingsFalso.DIMENSAO);
        repositorio.inserir(PRODUTOS, EMBEDDINGS.gerarVarios(PRODUTOS.stream().map(Produto::textoParaEmbedding).toList()));
    }

    @Test
    void insereEContaOsPontos() {
        assertEquals(4, repositorio.contar());
    }

    @Test
    void recriarAColecaoApagaOsPontosAnteriores() {
        repositorio.criarColecao(GeradorEmbeddingsFalso.DIMENSAO);

        assertEquals(0, repositorio.contar());
    }

    @Test
    void buscaVetorialComFiltroDePrecoEDeCategoria() {
        List<ResultadoBusca> praiaBarata = repositorio.buscar(
                EMBEDDINGS.gerar("levar à praia"), new FiltroBusca(null, 300.0), 10);
        List<ResultadoBusca> soPets = repositorio.buscar(
                EMBEDDINGS.gerar("cachorro"), new FiltroBusca("pets", null), 10);

        assertTrue(praiaBarata.stream().noneMatch(r -> r.produto().id() == 2), "a caixa de som custa R$ 349");
        assertEquals(List.of(4L), soPets.stream().map(r -> r.produto().id()).toList());
    }

    @Test
    void buscaSemFiltroRespeitaOLimite() {
        assertEquals(2, repositorio.buscar(EMBEDDINGS.gerar("praia"), FiltroBusca.nenhum(), 2).size());
    }

    @Test
    void atualizaSoOPayload() {
        repositorio.atualizarPrecoEEstoque(1, 159.92, 30);

        Produto fone = repositorio.buscarPorId(1).orElseThrow();
        assertEquals(159.92, fone.preco());
        assertEquals(30, fone.estoque());
        assertEquals("Fone Bluetooth Esportivo", fone.nome());
    }

    @Test
    void deletaPorIdEPorCategoria() {
        repositorio.deletarPorId(1);
        repositorio.deletarPorCategoria("pets");

        assertEquals(2, repositorio.contar());
        assertTrue(repositorio.buscarPorId(1).isEmpty());
    }

    @Test
    void listasDeTamanhosDiferentesSaoRecusadas() {
        assertThrows(IllegalArgumentException.class, () -> repositorio.inserir(PRODUTOS, List.of()));
    }

    @Test
    void conexaoComServidorInexistenteGeraErroClaro() {
        IllegalStateException erro = assertThrows(IllegalStateException.class,
                () -> QdrantClienteFactory.conectar("localhost", 1));

        assertTrue(erro.getMessage().startsWith("Qdrant indisponível em localhost:1"));
    }
}

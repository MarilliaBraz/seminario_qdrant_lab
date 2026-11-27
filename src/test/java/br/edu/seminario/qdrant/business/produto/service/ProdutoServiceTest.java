package br.edu.seminario.qdrant.business.produto.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.edu.seminario.qdrant.business.produto.model.AtualizacaoProduto;
import br.edu.seminario.qdrant.business.produto.model.FiltroBusca;
import br.edu.seminario.qdrant.business.produto.model.LoteInserido;
import br.edu.seminario.qdrant.business.produto.model.ResultadoBusca;
import br.edu.seminario.qdrant.suporte.Ambiente;
import br.edu.seminario.qdrant.suporte.GeradorEmbeddingsFalso;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProdutoServiceTest {

    private Ambiente ambiente;
    private ProdutoService service;

    @BeforeEach
    void prepararCatalogo() {
        ambiente = new Ambiente();
        service = ambiente.service;
        service.criarColecao();
        service.inserirCatalogo();
    }

    @Test
    void criaColecaoComADimensaoConfigurada() {
        assertEquals(GeradorEmbeddingsFalso.DIMENSAO, ambiente.repositorio.dimensaoCriada());
    }

    @Test
    void insereOCatalogoComUmVetorPorProduto() {
        LoteInserido lote = service.inserirCatalogo();

        assertEquals(30, lote.produtos().size());
        assertEquals(30, lote.vetores().size());
        assertEquals(30, service.contar());
    }

    @Test
    void atualizacaoPadraoDaDescontoDeVintePorCentoEBaixaDeCincoNoEstoque() {
        AtualizacaoProduto atualizacao = service.atualizarPrecoEEstoque(1, OptionalDouble.empty(), OptionalLong.empty());

        assertEquals(199.90, atualizacao.antes().preco());
        assertEquals(35, atualizacao.antes().estoque());
        assertEquals(159.92, atualizacao.depois().preco());
        assertEquals(30, atualizacao.depois().estoque());
    }

    @Test
    void atualizacaoPadraoNuncaDeixaOEstoqueNegativo() {
        // Barraca de Camping (#12) tem 7 unidades: 7 -> 2 -> 0, nunca -3
        service.atualizarPrecoEEstoque(12, OptionalDouble.empty(), OptionalLong.empty());
        AtualizacaoProduto segunda = service.atualizarPrecoEEstoque(12, OptionalDouble.empty(), OptionalLong.empty());

        assertEquals(2, segunda.antes().estoque());
        assertEquals(0, segunda.depois().estoque());
    }

    @Test
    void atualizacaoComValoresInformadosUsaOsValores() {
        AtualizacaoProduto atualizacao = service.atualizarPrecoEEstoque(7, OptionalDouble.of(499.90), OptionalLong.of(30));

        assertEquals(499.90, atualizacao.depois().preco());
        assertEquals(30, atualizacao.depois().estoque());
    }

    @Test
    void atualizarProdutoInexistenteGeraErroClaro() {
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> service.atualizarPrecoEEstoque(999, OptionalDouble.empty(), OptionalLong.empty()));

        assertEquals("Produto #999 não existe na coleção.", erro.getMessage());
    }

    @Test
    void precoNegativoERecusado() {
        assertThrows(IllegalArgumentException.class,
                () -> service.atualizarPrecoEEstoque(1, OptionalDouble.of(-1), OptionalLong.empty()));
    }

    @Test
    void buscaRespeitaFiltroDePrecoELimite() {
        List<ResultadoBusca> resultados = service.buscar("coisas para levar à praia", new FiltroBusca(null, 300.0), 3);

        assertEquals(3, resultados.size());
        assertTrue(resultados.stream().allMatch(r -> r.produto().preco() <= 300.0));
    }

    @Test
    void buscaRespeitaFiltroDeCategoria() {
        List<ResultadoBusca> resultados = service.buscar("algo para o meu cachorro", new FiltroBusca("pets", null), 10);

        assertEquals(3, resultados.size());
        assertTrue(resultados.stream().allMatch(r -> r.produto().categoria().equals("pets")));
    }

    @Test
    void buscaSemFraseOuComLimiteInvalidoERecusada() {
        assertThrows(IllegalArgumentException.class, () -> service.buscar(" ", FiltroBusca.nenhum(), 3));
        assertThrows(IllegalArgumentException.class, () -> service.buscar("praia", FiltroBusca.nenhum(), 0));
    }

    @Test
    void deletaPorIdEPorCategoria() {
        service.deletarPorId(1);
        service.deletarPorCategoria("pets");

        assertEquals(26, service.contar());
    }

    @Test
    void carregarModeloPreparaOGerador() {
        service.carregarModelo();

        assertTrue(ambiente.embeddings.isCarregado());
    }
}

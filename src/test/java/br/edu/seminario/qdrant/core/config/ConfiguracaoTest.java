package br.edu.seminario.qdrant.core.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ConfiguracaoTest {

    @Test
    void carregaValoresPadraoDoArquivo() {
        Configuracao config = Configuracao.carregar(Map.of());

        assertEquals("localhost", config.qdrantHost());
        assertEquals(6334, config.qdrantPortaGrpc());
        assertEquals("produtos", config.colecao());
        assertEquals(16, config.hnswM());
        assertEquals(100, config.hnswEfConstruct());
        assertEquals(Path.of("modelo"), config.pastaModelo());
        assertEquals(384, config.dimensaoVetor());
        assertEquals(Path.of("dados", "produtos.json"), config.arquivoProdutos());
        assertEquals(3, config.limitePadraoBusca());
        assertEquals(0.20, config.descontoPadrao());
        assertEquals(5, config.baixaEstoquePadrao());
        assertEquals("http://localhost:6333/dashboard", config.urlPainel());
    }

    @Test
    void variavelDeAmbienteSobrescreveOArquivo() {
        Configuracao config = Configuracao.carregar(Map.of("QDRANT_HOST", "qdrant.interno", "QDRANT_PORTA_GRPC", "7334"));

        assertEquals("qdrant.interno", config.qdrantHost());
        assertEquals(7334, config.qdrantPortaGrpc());
    }

    @Test
    void variavelDeAmbienteVaziaNaoApagaOValorDoArquivo() {
        Configuracao config = Configuracao.carregar(Map.of("QDRANT_HOST", "  "));

        assertEquals("localhost", config.qdrantHost());
    }

    @Test
    void numeroInvalidoNaVariavelDeAmbienteGeraErroClaro() {
        IllegalStateException erro = assertThrows(IllegalStateException.class,
                () -> Configuracao.carregar(Map.of("QDRANT_PORTA_GRPC", "abc")));

        assertEquals("Parâmetro qdrant.porta-grpc precisa ser um número inteiro", erro.getMessage());
    }

    @Test
    void decimalInvalidoGeraErroClaro() {
        IllegalStateException erro = assertThrows(IllegalStateException.class,
                () -> Configuracao.carregar(Map.of("ATUALIZACAO_DESCONTO_PADRAO", "vinte")));

        assertEquals("Parâmetro atualizacao.desconto-padrao precisa ser um número", erro.getMessage());
    }

    @Test
    void converteChaveEmNomeDeVariavelDeAmbiente() {
        assertEquals("QDRANT_HNSW_EF_CONSTRUCT", Configuracao.paraVariavelAmbiente("qdrant.hnsw.ef-construct"));
    }
}

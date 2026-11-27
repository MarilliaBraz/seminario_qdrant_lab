package br.edu.seminario.qdrant.business.produto.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.edu.seminario.qdrant.business.produto.model.Produto;
import br.edu.seminario.qdrant.suporte.Ambiente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProdutoControllerTest {

    private Ambiente ambiente;
    private ProdutoController controller;

    @BeforeEach
    void prepararCatalogo() {
        ambiente = new Ambiente();
        controller = ambiente.controller;
        controller.criar();
        controller.inserirLote();
    }

    @Test
    void criarELoteMostramOsPassosNoTerminal() {
        String saida = ambiente.saida();

        assertTrue(saida.contains("PASSO 1: criando a coleção de vetores (equivale ao CREATE TABLE)"));
        assertTrue(saida.contains("Coleção 'produtos' criada: vetor de 26 dimensões, distância Cosine, índice HNSW"));
        assertTrue(saida.contains("30 embeddings gerados em"));
        assertTrue(saida.contains("UPSERT concluído. Total de pontos na coleção: 30"));
    }

    @Test
    void inserirNovoSemArgumentosUsaODroneDaDemo() {
        controller.inserirNovo(new String[0]);

        assertTrue(ambiente.saida().contains("Pontos na coleção: 30 -> 31"));
        assertEquals(31, ambiente.service.contar());
    }

    @Test
    void inserirNovoComArgumentos() {
        controller.inserirNovo(new String[] {"40", "Patinete Elétrico", "Veículo leve", "eletronicos", "2999", "6"});

        assertTrue(ambiente.repositorio.buscarPorId(40).isPresent());
    }

    @Test
    void inserirNovoComArgumentosIncompletosMostraOUso() {
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> controller.inserirNovo(new String[] {"40", "Patinete"}));

        assertTrue(erro.getMessage().startsWith("Uso: inserir-novo"));
    }

    @Test
    void buscarMostraFiltrosEResultadosComScore() {
        controller.buscar(new String[] {"coisas para levar à praia", "--preco-max", "300", "--limite", "2"});

        String saida = ambiente.saida();
        assertTrue(saida.contains("PASSO 4: buscando \"coisas para levar à praia\"  [filtros: preço<=R$300.00]"));
        assertEquals(2, saida.lines().filter(linha -> linha.contains("[score ")).count());
    }

    @Test
    void buscarSemResultadoAvisa() {
        controller.buscar(new String[] {"qualquer coisa", "--categoria", "inexistente"});

        assertTrue(ambiente.saida().contains("Nenhum produto encontrado com esses filtros."));
    }

    @Test
    void buscarSemFraseMostraOUso() {
        assertThrows(IllegalArgumentException.class, () -> controller.buscar(new String[0]));
    }

    @Test
    void atualizarSemArgumentosAplicaAPromocaoNoFone() {
        controller.atualizar(new String[0]);

        String saida = ambiente.saida();
        assertTrue(saida.contains("PASSO 5: atualizando produto #1 (equivale ao UPDATE)"));
        assertTrue(saida.contains("R$    159,92 | estoque 30"));
    }

    @Test
    void atualizarComArgumentos() {
        controller.atualizar(new String[] {"7", "499,90", "30"});

        Produto tenis = ambiente.repositorio.buscarPorId(7).orElseThrow();
        assertEquals(499.90, tenis.preco());
        assertEquals(30, tenis.estoque());
    }

    @Test
    void deletarPorIdEPorCategoria() {
        controller.deletar(new String[] {"12"});
        controller.deletar(new String[] {"--categoria", "pets"});

        String saida = ambiente.saida();
        assertTrue(saida.contains("PASSO 6: removendo produto #12"));
        assertTrue(saida.contains("Pontos na coleção: 29 -> 26"));
    }

    @Test
    void formataProdutoNoPadraoBrasileiro() {
        Produto drone = ProdutoController.PRODUTO_EXEMPLO;

        assertEquals("#31  Drone com Câmera 4K                    | eletronicos | R$  2.499,00 | estoque 3",
                ProdutoFormatador.produto(drone));
    }

    @Test
    void erroVaiParaASaidaDeErros() {
        ambiente.terminal.erro("Qdrant indisponível");

        assertEquals("[ERRO] Qdrant indisponível" + System.lineSeparator(), ambiente.erros());
    }
}

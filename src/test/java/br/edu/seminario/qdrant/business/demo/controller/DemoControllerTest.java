package br.edu.seminario.qdrant.business.demo.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.edu.seminario.qdrant.suporte.Ambiente;
import java.util.Scanner;
import org.junit.jupiter.api.Test;

class DemoControllerTest {

    private static final int PAUSAS = 9;
    private static final String PAINEL = "http://localhost:6333/dashboard";

    @Test
    void roteiroCompletoPassaPorTodoOCrudETerminaComOPainel() {
        Ambiente ambiente = new Ambiente();
        Scanner teclado = new Scanner(System.lineSeparator().repeat(PAUSAS));

        new DemoController(ambiente.controller, ambiente.service, ambiente.terminal, teclado, PAINEL).executar();

        String saida = ambiente.saida();
        assertTrue(ambiente.embeddings.isCarregado());
        assertEquals(PAUSAS, saida.lines().filter(linha -> linha.startsWith(">>> ENTER para:")).count());
        assertTrue(saida.contains("Pontos na coleção: 30 -> 31"));
        assertTrue(saida.contains("R$    159,92 | estoque 30"));
        assertTrue(saida.contains("Pontos na coleção: 31 -> 30"));
        assertTrue(saida.contains("Pontos na coleção: 30 -> 27"));
        assertTrue(saida.contains("Fim da demo. Painel web: " + PAINEL));
    }

    @Test
    void roteiroNaoTravaQuandoAEntradaAcaba() {
        Ambiente ambiente = new Ambiente();

        new DemoController(ambiente.controller, ambiente.service, ambiente.terminal, new Scanner(""), PAINEL).executar();

        assertEquals(27, ambiente.service.contar());
    }
}

package br.edu.seminario.qdrant.core.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class ArgumentosTest {

    private static final String[] ARGUMENTOS = {"praia", "--preco-max", "300", "--categoria"};

    @Test
    void encontraOValorDaOpcao() {
        assertEquals(Optional.of("300"), Argumentos.opcao(ARGUMENTOS, "--preco-max"));
    }

    @Test
    void opcaoAusenteOuSemValorVoltaVazia() {
        assertTrue(Argumentos.opcao(ARGUMENTOS, "--limite").isEmpty());
        assertTrue(Argumentos.opcao(ARGUMENTOS, "--categoria").isEmpty());
    }

    @Test
    void aceitaVirgulaComoSeparadorDecimal() {
        assertEquals(499.90, Argumentos.numero("499,90"));
        assertEquals(499.90, Argumentos.numero(" 499.90 "));
    }

    @Test
    void numeroInvalidoGeraErroClaro() {
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class, () -> Argumentos.numero("caro"));
        assertEquals("Valor numérico inválido: caro", erro.getMessage());
    }

    @Test
    void inteiroRecusaCasasDecimais() {
        assertEquals(31, Argumentos.inteiro("31"));
        assertThrows(IllegalArgumentException.class, () -> Argumentos.inteiro("3,5"));
    }
}

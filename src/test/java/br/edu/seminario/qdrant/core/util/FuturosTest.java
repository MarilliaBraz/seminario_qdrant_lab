package br.edu.seminario.qdrant.core.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;

class FuturosTest {

    @Test
    void devolveOResultadoDoFuturo() {
        assertEquals(42, Futuros.aguardar(CompletableFuture.completedFuture(42)));
    }

    @Test
    void falhaNoFuturoViraErroComAMensagemDaCausa() {
        CompletableFuture<Integer> futuro = CompletableFuture.failedFuture(new RuntimeException("coleção não existe"));

        IllegalStateException erro = assertThrows(IllegalStateException.class, () -> Futuros.aguardar(futuro));

        assertEquals("Erro no Qdrant: coleção não existe", erro.getMessage());
    }
}

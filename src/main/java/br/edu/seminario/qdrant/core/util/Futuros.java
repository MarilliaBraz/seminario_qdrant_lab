package br.edu.seminario.qdrant.core.util;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/** O cliente do Qdrant é assíncrono (devolve um Future); aqui esperamos o resultado de forma uniforme. */
public final class Futuros {

    private Futuros() { }

    public static <T> T aguardar(Future<T> futuro) {
        try {
            return futuro.get();
        } catch (InterruptedException erro) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Operação interrompida", erro);
        } catch (ExecutionException erro) {
            Throwable causa = erro.getCause() == null ? erro : erro.getCause();
            throw new IllegalStateException("Erro no Qdrant: " + causa.getMessage(), erro);
        }
    }
}

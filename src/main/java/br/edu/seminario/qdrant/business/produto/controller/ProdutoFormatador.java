package br.edu.seminario.qdrant.business.produto.controller;

import br.edu.seminario.qdrant.business.produto.model.Produto;
import br.edu.seminario.qdrant.business.produto.model.ResultadoBusca;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/** Formata produtos, resultados e vetores para a saída no terminal (valores em reais, padrão brasileiro). */
public final class ProdutoFormatador {

    private static final Locale BRASIL = Locale.forLanguageTag("pt-BR");

    private ProdutoFormatador() { }

    public static String produto(Produto produto) {
        return String.format(BRASIL, "#%-3d %-38s | %-11s | R$ %,9.2f | estoque %d",
                produto.id(), produto.nome(), produto.categoria(), produto.preco(), produto.estoque());
    }

    public static String resultado(ResultadoBusca resultado) {
        return String.format(BRASIL, "[score %.3f] %s", resultado.score(), produto(resultado.produto()));
    }

    /** Primeiros números do vetor, para mostrar que o texto virou uma lista de números. */
    public static String inicioDoVetor(List<Float> vetor, int quantidade) {
        return vetor.stream().limit(quantidade)
                .map(valor -> String.format(Locale.US, "%.3f", valor))
                .collect(Collectors.joining(", ", "[", ", ...]"));
    }
}

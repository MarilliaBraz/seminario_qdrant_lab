package br.edu.seminario.qdrant.business.produto.model;

/** Filtros opcionais aplicados durante a busca vetorial (equivalem ao WHERE do SQL). */
public record FiltroBusca(String categoria, Double precoMaximo) {

    public static FiltroBusca nenhum() {
        return new FiltroBusca(null, null);
    }

    public boolean hasCategoria() {
        return categoria != null && !categoria.isBlank();
    }

    public boolean hasPrecoMaximo() {
        return precoMaximo != null;
    }

    public boolean isVazio() {
        return !hasCategoria() && !hasPrecoMaximo();
    }
}

package br.edu.seminario.qdrant.business.produto.repository;

import br.edu.seminario.qdrant.business.produto.model.Produto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Lê o catálogo de produtos de um arquivo JSON (lista de objetos com os campos de {@link Produto}). */
public final class CatalogoProdutoArquivo {

    private final Path arquivo;
    private final ObjectMapper json = new ObjectMapper();

    public CatalogoProdutoArquivo(Path arquivo) {
        this.arquivo = arquivo;
    }

    public List<Produto> ler() {
        try {
            List<Produto> produtos = json.readValue(Files.readString(arquivo), new TypeReference<>() { });
            if (produtos.isEmpty()) {
                throw new IllegalStateException("O catálogo " + arquivo.toAbsolutePath() + " está vazio");
            }
            return List.copyOf(produtos);
        } catch (IOException erro) {
            throw new IllegalStateException("Não foi possível ler " + arquivo.toAbsolutePath(), erro);
        }
    }
}

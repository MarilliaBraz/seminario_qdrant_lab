package br.edu.seminario.qdrant.suporte;

import br.edu.seminario.qdrant.business.produto.controller.ProdutoController;
import br.edu.seminario.qdrant.business.produto.repository.CatalogoProdutoArquivo;
import br.edu.seminario.qdrant.business.produto.service.ProdutoService;
import br.edu.seminario.qdrant.core.util.Terminal;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/** Monta serviço, controller e terminal em memória, como a aplicação faz, mas sem Qdrant nem modelo. */
public final class Ambiente {

    public static final Path CATALOGO = Path.of("dados", "produtos.json");

    public final ProdutoRepositoryEmMemoria repositorio = new ProdutoRepositoryEmMemoria();
    public final GeradorEmbeddingsFalso embeddings = new GeradorEmbeddingsFalso();
    public final ProdutoService service = new ProdutoService(repositorio, embeddings,
            new CatalogoProdutoArquivo(CATALOGO), new ProdutoService.RegrasAtualizacao(GeradorEmbeddingsFalso.DIMENSAO, 0.20, 5));
    private final ByteArrayOutputStream saida = new ByteArrayOutputStream();
    private final ByteArrayOutputStream erros = new ByteArrayOutputStream();
    public final Terminal terminal = new Terminal(
            new PrintStream(saida, true, StandardCharsets.UTF_8), new PrintStream(erros, true, StandardCharsets.UTF_8));
    public final ProdutoController controller = new ProdutoController(service, terminal,
            new ProdutoController.Exibicao("produtos", GeradorEmbeddingsFalso.DIMENSAO, 3));

    public String saida() {
        return saida.toString(StandardCharsets.UTF_8);
    }

    public String erros() {
        return erros.toString(StandardCharsets.UTF_8);
    }
}

package br.edu.seminario.qdrant.business.demo.controller;

import br.edu.seminario.qdrant.business.produto.controller.ProdutoController;
import br.edu.seminario.qdrant.business.produto.service.ProdutoService;
import br.edu.seminario.qdrant.core.util.Terminal;
import java.util.Scanner;

/** Roteiro da demonstração ao vivo: cada passo do CRUD espera um ENTER para continuar. */
public final class DemoController {

    private final ProdutoController produtos;
    private final ProdutoService service;
    private final Terminal terminal;
    private final Scanner teclado;
    private final String urlPainel;

    public DemoController(ProdutoController produtos, ProdutoService service, Terminal terminal,
            Scanner teclado, String urlPainel) {
        this.produtos = produtos;
        this.service = service;
        this.terminal = terminal;
        this.teclado = teclado;
        this.urlPainel = urlPainel;
    }

    public void executar() {
        terminal.info("Carregando o modelo de IA...");
        service.carregarModelo();
        pausa("criar a coleção (CREATE)");
        produtos.criar();
        pausa("inserir 30 produtos em lote (INSERT)");
        produtos.inserirLote();
        pausa("inserir um produto novo ao vivo (INSERT)");
        produtos.inserirNovo(new String[0]);
        pausa("busca semântica sem palavra-chave em comum (READ)");
        produtos.buscar(new String[] {"algo para ouvir música enquanto corro"});
        pausa("busca semântica + filtro de preço (READ com filtro)");
        produtos.buscar(new String[] {"coisas para levar à praia", "--preco-max", "300"});
        pausa("encontrar o produto recém-inserido pelo significado (READ)");
        produtos.buscar(new String[] {"quero filmar viagens do alto"});
        pausa("atualizar preço e estoque (UPDATE)");
        produtos.atualizar(new String[0]);
        pausa("remover o produto inserido ao vivo (DELETE por ID)");
        produtos.deletar(new String[] {String.valueOf(ProdutoController.PRODUTO_EXEMPLO.id())});
        pausa("remover uma categoria inteira (DELETE por filtro)");
        produtos.deletar(new String[] {"--categoria", "pets"});
        terminal.mensagem(System.lineSeparator() + "Fim da demo. Painel web: " + urlPainel);
    }

    private void pausa(String proximoPasso) {
        terminal.pergunta(System.lineSeparator() + ">>> ENTER para: " + proximoPasso + " ");
        if (teclado.hasNextLine()) {
            teclado.nextLine();
        }
    }
}

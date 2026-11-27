package br.edu.seminario.qdrant;

import br.edu.seminario.qdrant.business.demo.controller.DemoController;
import br.edu.seminario.qdrant.business.produto.controller.ProdutoController;
import br.edu.seminario.qdrant.business.produto.repository.CatalogoProdutoArquivo;
import br.edu.seminario.qdrant.business.produto.repository.ProdutoRepositoryQdrant;
import br.edu.seminario.qdrant.business.produto.service.ProdutoService;
import br.edu.seminario.qdrant.core.config.Configuracao;
import br.edu.seminario.qdrant.core.config.QdrantClienteFactory;
import br.edu.seminario.qdrant.core.embedding.GeradorEmbeddingsOnnx;
import br.edu.seminario.qdrant.core.util.Terminal;
import io.qdrant.client.QdrantClient;
import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Laboratório Qdrant: busca semântica num catálogo de e-commerce.
 *
 * <pre>
 * Uso: java -jar target/qdrant-lab.jar &lt;comando&gt; [argumentos]
 *   demo                                                  roteiro completo, ENTER avança cada passo
 *   criar                                                 CREATE: cria a coleção
 *   inserir-lote                                          INSERT: produtos do catálogo JSON
 *   inserir-novo [id nome descricao categoria preco estoque]   INSERT de um produto
 *   buscar "frase" [--categoria X] [--preco-max N] [--limite N]   READ
 *   atualizar [id preco estoque]                          UPDATE: padrão é o produto 1 com desconto
 *   deletar [id] | deletar --categoria X                  DELETE
 * </pre>
 */
public final class QdrantLabApplication {

    private static final String COMANDOS = "demo, criar, inserir-lote, inserir-novo, buscar, atualizar, deletar";

    // Referência estática para o logger não ser coletado: o gRPC usa java.util.logging
    private static final Logger LOG_GRPC = Logger.getLogger("io.grpc");

    private QdrantLabApplication() { }

    public static void main(String[] args) {
        Terminal terminal = Terminal.padrao();
        if (args.length == 0) {
            terminal.erro("Informe um comando: " + COMANDOS);
            System.exit(1);
        }
        LOG_GRPC.setLevel(Level.OFF); // erros de conexão aparecem só como mensagem [ERRO] clara
        try {
            executar(args[0], Arrays.copyOfRange(args, 1, args.length), terminal);
        } catch (IllegalArgumentException | IllegalStateException erro) {
            terminal.erro(erro.getMessage());
            System.exit(1);
        }
    }

    private static void executar(String comando, String[] argumentos, Terminal terminal) {
        Configuracao config = Configuracao.carregar();
        try (QdrantClient cliente = QdrantClienteFactory.conectar(config.qdrantHost(), config.qdrantPortaGrpc())) {
            ProdutoService service = new ProdutoService(
                    new ProdutoRepositoryQdrant(cliente, config.colecao(), config.hnswM(), config.hnswEfConstruct()),
                    new GeradorEmbeddingsOnnx(config.pastaModelo()),
                    new CatalogoProdutoArquivo(config.arquivoProdutos()),
                    new ProdutoService.RegrasAtualizacao(
                            config.dimensaoVetor(), config.descontoPadrao(), config.baixaEstoquePadrao()));
            ProdutoController produtos = new ProdutoController(service, terminal,
                    new ProdutoController.Exibicao(config.colecao(), config.dimensaoVetor(), config.limitePadraoBusca()));

            switch (comando) {
                case "demo" -> new DemoController(produtos, service, terminal, new Scanner(System.in),
                        config.urlPainel()).executar();
                case "criar" -> produtos.criar();
                case "inserir-lote" -> produtos.inserirLote();
                case "inserir-novo" -> produtos.inserirNovo(argumentos);
                case "buscar" -> produtos.buscar(argumentos);
                case "atualizar" -> produtos.atualizar(argumentos);
                case "deletar" -> produtos.deletar(argumentos);
                default -> throw new IllegalArgumentException("Comando desconhecido: " + comando + ". Use: " + COMANDOS);
            }
        }
    }
}

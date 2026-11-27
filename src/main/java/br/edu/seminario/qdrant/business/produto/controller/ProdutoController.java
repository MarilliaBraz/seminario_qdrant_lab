package br.edu.seminario.qdrant.business.produto.controller;

import br.edu.seminario.qdrant.business.produto.model.AtualizacaoProduto;
import br.edu.seminario.qdrant.business.produto.model.FiltroBusca;
import br.edu.seminario.qdrant.business.produto.model.LoteInserido;
import br.edu.seminario.qdrant.business.produto.model.Produto;
import br.edu.seminario.qdrant.business.produto.model.ResultadoBusca;
import br.edu.seminario.qdrant.business.produto.service.ProdutoService;
import br.edu.seminario.qdrant.core.util.Argumentos;
import br.edu.seminario.qdrant.core.util.Terminal;
import java.util.List;
import java.util.Locale;
import java.util.OptionalDouble;
import java.util.OptionalLong;

/** Comandos de linha de comando do CRUD: lê os argumentos, chama o serviço e mostra o resultado. */
public final class ProdutoController {

    /** Produto inserido ao vivo na demo quando {@code inserir-novo} é chamado sem argumentos. */
    public static final Produto PRODUTO_EXEMPLO = new Produto(31, "Drone com Câmera 4K",
            "Aeronave controlada pelo celular que filma paisagens do alto em alta resolução",
            "eletronicos", 2499.00, 3);

    /** Produto atualizado quando {@code atualizar} é chamado sem argumentos (o fone de ouvido). */
    public static final long ID_ATUALIZAR_PADRAO = 1;

    private static final int NUMEROS_EXIBIDOS_DO_VETOR = 5;
    private static final int CAMPOS_PRODUTO = 6;

    private final ProdutoService service;
    private final Terminal terminal;
    private final Exibicao exibicao;

    /** Dados usados só para descrever o que foi feito (nome da coleção, tamanho do vetor e limite padrão). */
    public record Exibicao(String colecao, int dimensaoVetor, int limitePadraoBusca) { }

    public ProdutoController(ProdutoService service, Terminal terminal, Exibicao exibicao) {
        this.service = service;
        this.terminal = terminal;
        this.exibicao = exibicao;
    }

    public void criar() {
        terminal.titulo("PASSO 1: criando a coleção de vetores (equivale ao CREATE TABLE)");
        service.criarColecao();
        terminal.info("Coleção '" + exibicao.colecao() + "' criada: vetor de " + exibicao.dimensaoVetor()
                + " dimensões, distância Cosine, índice HNSW");
        terminal.info("Índices de payload: categoria (keyword), preco (float)");
    }

    public void inserirLote() {
        terminal.titulo("PASSO 2: inserindo produtos (texto -> embedding -> Qdrant)");
        LoteInserido lote = service.inserirCatalogo();
        terminal.info(String.format(Locale.US, "%d embeddings gerados em %.2fs",
                lote.vetores().size(), lote.tempoEmbeddings().toNanos() / 1e9));
        terminal.info("Exemplo: '" + lote.produtos().get(0).nome() + "' virou " + lote.vetores().get(0).size()
                + " números: " + ProdutoFormatador.inicioDoVetor(lote.vetores().get(0), NUMEROS_EXIBIDOS_DO_VETOR));
        terminal.info("UPSERT concluído. Total de pontos na coleção: " + service.contar());
    }

    public void inserirNovo(String[] argumentos) {
        Produto produto = argumentos.length == 0 ? PRODUTO_EXEMPLO : lerProduto(argumentos);
        terminal.titulo("PASSO 3: inserindo novo produto #" + produto.id() + " ao vivo");
        long antes = service.contar();
        service.inserir(produto);
        terminal.info(ProdutoFormatador.produto(produto));
        terminal.info("Pontos na coleção: " + antes + " -> " + service.contar());
    }

    public void buscar(String[] argumentos) {
        if (argumentos.length == 0) {
            throw new IllegalArgumentException("Uso: buscar \"frase\" [--categoria X] [--preco-max N] [--limite N]");
        }
        String frase = argumentos[0];
        FiltroBusca filtro = new FiltroBusca(
                Argumentos.opcao(argumentos, "--categoria").orElse(null),
                Argumentos.opcao(argumentos, "--preco-max").map(Argumentos::numero).orElse(null));
        int limite = Argumentos.opcao(argumentos, "--limite")
                .map(texto -> (int) Argumentos.inteiro(texto)).orElse(exibicao.limitePadraoBusca());

        terminal.titulo("PASSO 4: buscando \"" + frase + "\"" + descrever(filtro));
        List<ResultadoBusca> resultados = service.buscar(frase, filtro, limite);
        if (resultados.isEmpty()) {
            terminal.info("Nenhum produto encontrado com esses filtros.");
        }
        resultados.forEach(resultado -> terminal.info(ProdutoFormatador.resultado(resultado)));
    }

    public void atualizar(String[] argumentos) {
        long id = argumentos.length > 0 ? Argumentos.inteiro(argumentos[0]) : ID_ATUALIZAR_PADRAO;
        OptionalDouble preco = argumentos.length > 1
                ? OptionalDouble.of(Argumentos.numero(argumentos[1])) : OptionalDouble.empty();
        OptionalLong estoque = argumentos.length > 2
                ? OptionalLong.of(Argumentos.inteiro(argumentos[2])) : OptionalLong.empty();

        AtualizacaoProduto atualizacao = service.atualizarPrecoEEstoque(id, preco, estoque);
        terminal.titulo("PASSO 5: atualizando produto #" + id + " (equivale ao UPDATE)");
        terminal.info("ANTES:");
        terminal.info(ProdutoFormatador.produto(atualizacao.antes()));
        terminal.info("DEPOIS:");
        terminal.info(ProdutoFormatador.produto(atualizacao.depois()));
    }

    public void deletar(String[] argumentos) {
        long antes = service.contar();
        var categoria = Argumentos.opcao(argumentos, "--categoria");
        if (categoria.isPresent()) {
            terminal.titulo("PASSO 6: removendo todos os produtos da categoria '" + categoria.get() + "'");
            service.deletarPorCategoria(categoria.get());
        } else {
            long id = argumentos.length > 0 ? Argumentos.inteiro(argumentos[0]) : PRODUTO_EXEMPLO.id();
            terminal.titulo("PASSO 6: removendo produto #" + id);
            service.deletarPorId(id);
        }
        terminal.info("Pontos na coleção: " + antes + " -> " + service.contar());
    }

    private static Produto lerProduto(String[] argumentos) {
        if (argumentos.length != CAMPOS_PRODUTO) {
            throw new IllegalArgumentException("Uso: inserir-novo <id> <nome> <descricao> <categoria> <preco> <estoque>");
        }
        return new Produto(Argumentos.inteiro(argumentos[0]), argumentos[1], argumentos[2], argumentos[3],
                Argumentos.numero(argumentos[4]), Argumentos.inteiro(argumentos[5]));
    }

    private static String descrever(FiltroBusca filtro) {
        if (filtro.isVazio()) {
            return "";
        }
        String texto = (filtro.hasCategoria() ? " categoria=" + filtro.categoria() : "")
                + (filtro.hasPrecoMaximo() ? String.format(Locale.US, " preço<=R$%.2f", filtro.precoMaximo()) : "");
        return "  [filtros:" + texto + "]";
    }
}

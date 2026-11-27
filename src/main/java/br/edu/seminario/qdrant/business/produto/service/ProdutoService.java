package br.edu.seminario.qdrant.business.produto.service;

import br.edu.seminario.qdrant.business.produto.model.AtualizacaoProduto;
import br.edu.seminario.qdrant.business.produto.model.FiltroBusca;
import br.edu.seminario.qdrant.business.produto.model.LoteInserido;
import br.edu.seminario.qdrant.business.produto.model.Produto;
import br.edu.seminario.qdrant.business.produto.model.ResultadoBusca;
import br.edu.seminario.qdrant.business.produto.repository.CatalogoProdutoArquivo;
import br.edu.seminario.qdrant.business.produto.repository.ProdutoRepository;
import br.edu.seminario.qdrant.core.embedding.GeradorEmbeddings;
import java.time.Duration;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalLong;

/** Regras do laboratório: junta o modelo de embedding, o catálogo e o repositório vetorial. */
public final class ProdutoService {

    private final ProdutoRepository repositorio;
    private final GeradorEmbeddings embeddings;
    private final CatalogoProdutoArquivo catalogo;
    private final RegrasAtualizacao regras;

    /** Tamanho do vetor e valores padrão do UPDATE quando o usuário não informa preço e estoque. */
    public record RegrasAtualizacao(int dimensaoVetor, double descontoPadrao, long baixaEstoquePadrao) { }

    public ProdutoService(ProdutoRepository repositorio, GeradorEmbeddings embeddings,
            CatalogoProdutoArquivo catalogo, RegrasAtualizacao regras) {
        this.repositorio = repositorio;
        this.embeddings = embeddings;
        this.catalogo = catalogo;
        this.regras = regras;
    }

    public void carregarModelo() {
        embeddings.carregar();
    }

    public void criarColecao() {
        repositorio.criarColecao(regras.dimensaoVetor());
    }

    public LoteInserido inserirCatalogo() {
        List<Produto> produtos = catalogo.ler();
        long inicio = System.nanoTime();
        List<List<Float>> vetores = embeddings.gerarVarios(produtos.stream().map(Produto::textoParaEmbedding).toList());
        Duration tempo = Duration.ofNanos(System.nanoTime() - inicio);
        repositorio.inserir(produtos, vetores);
        return new LoteInserido(produtos, vetores, tempo);
    }

    public void inserir(Produto produto) {
        repositorio.inserir(List.of(produto), List.of(embeddings.gerar(produto.textoParaEmbedding())));
    }

    public List<ResultadoBusca> buscar(String frase, FiltroBusca filtro, int limite) {
        if (frase == null || frase.isBlank()) {
            throw new IllegalArgumentException("Informe a frase da busca");
        }
        if (limite < 1) {
            throw new IllegalArgumentException("O limite precisa ser maior que zero");
        }
        return repositorio.buscar(embeddings.gerar(frase), filtro, limite);
    }

    /**
     * Atualiza preço e estoque. Sem valores informados, aplica a regra padrão:
     * desconto configurado no preço e baixa configurada no estoque (nunca abaixo de zero).
     */
    public AtualizacaoProduto atualizarPrecoEEstoque(long idProduto, OptionalDouble novoPreco, OptionalLong novoEstoque) {
        Produto antes = repositorio.buscarPorId(idProduto)
                .orElseThrow(() -> new IllegalArgumentException("Produto #" + idProduto + " não existe na coleção."));
        double preco = novoPreco.orElse(arredondarCentavos(antes.preco() * (1 - regras.descontoPadrao())));
        long estoque = novoEstoque.orElse(Math.max(antes.estoque() - regras.baixaEstoquePadrao(), 0));
        if (preco < 0 || estoque < 0) {
            throw new IllegalArgumentException("Preço e estoque não podem ser negativos");
        }
        repositorio.atualizarPrecoEEstoque(idProduto, preco, estoque);
        Produto depois = repositorio.buscarPorId(idProduto).orElse(antes.comPrecoEEstoque(preco, estoque));
        return new AtualizacaoProduto(antes, depois);
    }

    public void deletarPorId(long idProduto) {
        repositorio.deletarPorId(idProduto);
    }

    public void deletarPorCategoria(String categoria) {
        repositorio.deletarPorCategoria(categoria);
    }

    public long contar() {
        return repositorio.contar();
    }

    private static double arredondarCentavos(double valor) {
        return Math.round(valor * 100) / 100.0;
    }
}

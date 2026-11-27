package br.edu.seminario.qdrant.suporte;

import br.edu.seminario.qdrant.business.produto.model.FiltroBusca;
import br.edu.seminario.qdrant.business.produto.model.Produto;
import br.edu.seminario.qdrant.business.produto.model.ResultadoBusca;
import br.edu.seminario.qdrant.business.produto.repository.ProdutoRepository;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Repositório em memória que calcula o cosseno de verdade, para testar sem o Qdrant. */
public final class ProdutoRepositoryEmMemoria implements ProdutoRepository {

    private final Map<Long, Produto> produtos = new LinkedHashMap<>();
    private final Map<Long, List<Float>> vetores = new LinkedHashMap<>();
    private int dimensaoCriada;

    public int dimensaoCriada() {
        return dimensaoCriada;
    }

    @Override
    public void criarColecao(int dimensaoVetor) {
        produtos.clear();
        vetores.clear();
        dimensaoCriada = dimensaoVetor;
    }

    @Override
    public void inserir(List<Produto> novos, List<List<Float>> novosVetores) {
        for (int i = 0; i < novos.size(); i++) {
            produtos.put(novos.get(i).id(), novos.get(i));
            vetores.put(novos.get(i).id(), novosVetores.get(i));
        }
    }

    @Override
    public List<ResultadoBusca> buscar(List<Float> vetorConsulta, FiltroBusca filtro, int limite) {
        return produtos.values().stream()
                .filter(p -> !filtro.hasCategoria() || p.categoria().equals(filtro.categoria()))
                .filter(p -> !filtro.hasPrecoMaximo() || p.preco() <= filtro.precoMaximo())
                .map(p -> new ResultadoBusca(p, cosseno(vetorConsulta, vetores.get(p.id()))))
                .sorted(Comparator.comparingDouble(ResultadoBusca::score).reversed())
                .limit(limite)
                .toList();
    }

    @Override
    public Optional<Produto> buscarPorId(long idProduto) {
        return Optional.ofNullable(produtos.get(idProduto));
    }

    @Override
    public long contar() {
        return produtos.size();
    }

    @Override
    public void atualizarPrecoEEstoque(long idProduto, double novoPreco, long novoEstoque) {
        produtos.computeIfPresent(idProduto, (id, p) -> p.comPrecoEEstoque(novoPreco, novoEstoque));
    }

    @Override
    public void deletarPorId(long idProduto) {
        produtos.remove(idProduto);
        vetores.remove(idProduto);
    }

    @Override
    public void deletarPorCategoria(String categoria) {
        produtos.values().removeIf(p -> p.categoria().equals(categoria));
        vetores.keySet().retainAll(produtos.keySet());
    }

    private static float cosseno(List<Float> a, List<Float> b) {
        double produto = 0;
        double normaA = 0;
        double normaB = 0;
        for (int i = 0; i < a.size(); i++) {
            produto += a.get(i) * b.get(i);
            normaA += a.get(i) * a.get(i);
            normaB += b.get(i) * b.get(i);
        }
        return normaA == 0 || normaB == 0 ? 0f : (float) (produto / Math.sqrt(normaA * normaB));
    }
}

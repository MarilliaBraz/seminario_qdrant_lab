package br.edu.seminario.qdrant.business.produto.repository;

import br.edu.seminario.qdrant.business.produto.model.FiltroBusca;
import br.edu.seminario.qdrant.business.produto.model.Produto;
import br.edu.seminario.qdrant.business.produto.model.ResultadoBusca;
import java.util.List;
import java.util.Optional;

/** Armazenamento dos produtos como vetores: as operações de CRUD do laboratório. */
public interface ProdutoRepository {

    /** CREATE TABLE: recria a coleção com o tamanho de vetor informado. */
    void criarColecao(int dimensaoVetor);

    /** INSERT: cada produto vira um ponto (ID + vetor + payload). As listas andam em paralelo. */
    void inserir(List<Produto> produtos, List<List<Float>> vetores);

    /** SELECT ... WHERE: os vetores mais próximos da consulta, com filtros opcionais. */
    List<ResultadoBusca> buscar(List<Float> vetorConsulta, FiltroBusca filtro, int limite);

    Optional<Produto> buscarPorId(long idProduto);

    long contar();

    /** UPDATE: altera só o payload, sem recalcular o vetor. */
    void atualizarPrecoEEstoque(long idProduto, double novoPreco, long novoEstoque);

    void deletarPorId(long idProduto);

    void deletarPorCategoria(String categoria);
}

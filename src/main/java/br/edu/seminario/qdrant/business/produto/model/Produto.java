package br.edu.seminario.qdrant.business.produto.model;

/** Um produto da loja. No Qdrant ele vira um "ponto": ID + vetor + payload (os demais campos). */
public record Produto(long id, String nome, String descricao, String categoria, double preco, long estoque) {

    /** Texto que representa o significado do produto e será transformado em vetor. */
    public String textoParaEmbedding() {
        return nome + ". " + descricao;
    }

    /** Cópia do produto com novo preço e estoque (o original não muda). */
    public Produto comPrecoEEstoque(double novoPreco, long novoEstoque) {
        return new Produto(id, nome, descricao, categoria, novoPreco, novoEstoque);
    }
}

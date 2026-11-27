package br.edu.seminario.qdrant.business.produto.model;

/** Estado do produto antes e depois de um UPDATE. */
public record AtualizacaoProduto(Produto antes, Produto depois) { }

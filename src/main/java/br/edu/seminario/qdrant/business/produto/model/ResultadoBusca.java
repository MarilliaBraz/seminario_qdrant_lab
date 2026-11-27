package br.edu.seminario.qdrant.business.produto.model;

/** Resultado de uma busca semântica: o produto encontrado e o grau de semelhança (cosseno, de -1 a 1). */
public record ResultadoBusca(Produto produto, float score) { }

package br.edu.seminario.qdrant.business.produto.model;

import java.time.Duration;
import java.util.List;

/** O que aconteceu num INSERT em lote: os produtos, os vetores gerados e quanto tempo o modelo levou. */
public record LoteInserido(List<Produto> produtos, List<List<Float>> vetores, Duration tempoEmbeddings) { }

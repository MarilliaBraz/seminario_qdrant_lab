package br.edu.seminario.qdrant.core.embedding;

import java.util.List;

/** Transforma textos em vetores (embeddings) que representam o significado de cada texto. */
public interface GeradorEmbeddings {

    /** Prepara o modelo antes do primeiro uso (pode levar alguns segundos). */
    void carregar();

    List<Float> gerar(String texto);

    List<List<Float>> gerarVarios(List<String> textos);
}

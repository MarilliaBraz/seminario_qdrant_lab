package br.edu.seminario.qdrant.suporte;

import br.edu.seminario.qdrant.core.embedding.GeradorEmbeddings;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Gerador determinístico para testes: conta as letras de a a z do texto (26 dimensões).
 * Textos com palavras em comum ficam próximos, o que basta para testar a ordem dos resultados.
 */
public final class GeradorEmbeddingsFalso implements GeradorEmbeddings {

    public static final int DIMENSAO = 26;

    private boolean carregado;

    public boolean isCarregado() {
        return carregado;
    }

    @Override
    public void carregar() {
        carregado = true;
    }

    @Override
    public List<Float> gerar(String texto) {
        float[] contagem = new float[DIMENSAO];
        String normalizado = Normalizer.normalize(texto.toLowerCase(Locale.ROOT), Normalizer.Form.NFD);
        for (char letra : normalizado.toCharArray()) {
            if (letra >= 'a' && letra <= 'z') {
                contagem[letra - 'a']++;
            }
        }
        List<Float> vetor = new ArrayList<>(DIMENSAO);
        for (float valor : contagem) {
            vetor.add(valor);
        }
        return vetor;
    }

    @Override
    public List<List<Float>> gerarVarios(List<String> textos) {
        return textos.stream().map(this::gerar).toList();
    }
}

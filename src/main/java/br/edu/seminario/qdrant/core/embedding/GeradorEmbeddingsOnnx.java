package br.edu.seminario.qdrant.core.embedding;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.OnnxEmbeddingModel;
import dev.langchain4j.model.embedding.onnx.PoolingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Gera embeddings com o modelo multilíngue paraphrase-multilingual-MiniLM-L12-v2 em formato ONNX,
 * executado dentro da JVM. O modelo só é carregado no primeiro uso, porque leva alguns segundos.
 */
public final class GeradorEmbeddingsOnnx implements GeradorEmbeddings {

    private static final String ARQUIVO_MODELO = "model_optimized.onnx";
    private static final String ARQUIVO_TOKENIZER = "tokenizer.json";

    private final Path pastaModelo;
    private EmbeddingModel modelo;

    public GeradorEmbeddingsOnnx(Path pastaModelo) {
        this.pastaModelo = pastaModelo;
    }

    @Override
    public void carregar() {
        modelo();
    }

    @Override
    public List<Float> gerar(String texto) {
        return modelo().embed(texto).content().vectorAsList();
    }

    @Override
    public List<List<Float>> gerarVarios(List<String> textos) {
        List<TextSegment> segmentos = textos.stream().map(TextSegment::from).toList();
        return modelo().embedAll(segmentos).content().stream().map(Embedding::vectorAsList).toList();
    }

    private EmbeddingModel modelo() {
        if (modelo == null) {
            Path arquivoModelo = pastaModelo.resolve(ARQUIVO_MODELO);
            Path arquivoTokenizer = pastaModelo.resolve(ARQUIVO_TOKENIZER);
            if (!Files.exists(arquivoModelo) || !Files.exists(arquivoTokenizer)) {
                throw new IllegalStateException("Modelo não encontrado em " + pastaModelo.toAbsolutePath()
                        + ". Rode ./baixar_modelo.sh antes da demo.");
            }
            modelo = new OnnxEmbeddingModel(arquivoModelo, arquivoTokenizer, PoolingMode.MEAN);
        }
        return modelo;
    }
}

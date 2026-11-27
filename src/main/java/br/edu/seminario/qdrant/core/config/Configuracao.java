package br.edu.seminario.qdrant.core.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/**
 * Parâmetros do laboratório, lidos de {@code application.properties} e sobrescritos por variáveis de ambiente.
 *
 * <p>Cada chave vira uma variável de ambiente em maiúsculas, com "." e "-" trocados por "_"
 * (ex.: {@code qdrant.porta-grpc} → {@code QDRANT_PORTA_GRPC}).
 */
public record Configuracao(
        String qdrantHost,
        int qdrantPortaGrpc,
        String urlPainel,
        String colecao,
        int hnswM,
        int hnswEfConstruct,
        Path pastaModelo,
        int dimensaoVetor,
        Path arquivoProdutos,
        int limitePadraoBusca,
        double descontoPadrao,
        long baixaEstoquePadrao) {

    private static final String ARQUIVO = "/application.properties";

    /** Carrega a configuração usando as variáveis de ambiente do processo. */
    public static Configuracao carregar() {
        return carregar(System.getenv());
    }

    /** Carrega a configuração usando o mapa de ambiente informado (permite testar sem alterar o processo). */
    public static Configuracao carregar(Map<String, String> ambiente) {
        Leitor leitor = new Leitor(lerArquivo(), ambiente);
        return new Configuracao(
                leitor.texto("qdrant.host"),
                leitor.inteiro("qdrant.porta-grpc"),
                leitor.texto("qdrant.url-painel"),
                leitor.texto("qdrant.colecao"),
                leitor.inteiro("qdrant.hnsw.m"),
                leitor.inteiro("qdrant.hnsw.ef-construct"),
                Path.of(leitor.texto("modelo.pasta")),
                leitor.inteiro("modelo.dimensao"),
                Path.of(leitor.texto("dados.arquivo-produtos")),
                leitor.inteiro("busca.limite-padrao"),
                leitor.decimal("atualizacao.desconto-padrao"),
                leitor.inteiro("atualizacao.baixa-estoque-padrao"));
    }

    /** Nome da variável de ambiente que sobrescreve a chave (ex.: {@code qdrant.host} → {@code QDRANT_HOST}). */
    public static String paraVariavelAmbiente(String chave) {
        return chave.toUpperCase(Locale.ROOT).replace('.', '_').replace('-', '_');
    }

    private static Properties lerArquivo() {
        Properties propriedades = new Properties();
        try (InputStream entrada = Configuracao.class.getResourceAsStream(ARQUIVO)) {
            if (entrada == null) {
                throw new IllegalStateException("Arquivo de configuração não encontrado: " + ARQUIVO);
            }
            propriedades.load(new InputStreamReader(entrada, StandardCharsets.UTF_8));
            return propriedades;
        } catch (IOException erro) {
            throw new IllegalStateException("Não foi possível ler " + ARQUIVO, erro);
        }
    }

    /** Resolve cada chave: primeiro a variável de ambiente, depois o arquivo. */
    private record Leitor(Properties arquivo, Map<String, String> ambiente) {

        String texto(String chave) {
            String valor = ambiente.get(paraVariavelAmbiente(chave));
            if (valor == null || valor.isBlank()) {
                valor = arquivo.getProperty(chave);
            }
            if (valor == null || valor.isBlank()) {
                throw new IllegalStateException("Parâmetro obrigatório ausente: " + chave);
            }
            return valor.trim();
        }

        int inteiro(String chave) {
            try {
                return Integer.parseInt(texto(chave));
            } catch (NumberFormatException erro) {
                throw new IllegalStateException("Parâmetro " + chave + " precisa ser um número inteiro", erro);
            }
        }

        double decimal(String chave) {
            try {
                return Double.parseDouble(texto(chave));
            } catch (NumberFormatException erro) {
                throw new IllegalStateException("Parâmetro " + chave + " precisa ser um número", erro);
            }
        }
    }
}

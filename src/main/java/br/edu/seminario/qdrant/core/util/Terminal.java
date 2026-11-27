package br.edu.seminario.qdrant.core.util;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * Saída da aplicação no terminal. É a interface com o usuário deste laboratório (não é log de depuração),
 * por isso escreve num {@link PrintStream} recebido no construtor: em UTF-8 na execução e em memória nos testes.
 */
public final class Terminal {

    private static final String LINHA = "=".repeat(74);
    private static final String RECUO = "  ";

    private final PrintStream saida;
    private final PrintStream erros;

    public Terminal(PrintStream saida, PrintStream erros) {
        this.saida = saida;
        this.erros = erros;
    }

    /** Terminal do processo, sempre em UTF-8 para os acentos aparecerem certos. */
    public static Terminal padrao() {
        return new Terminal(
                new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8),
                new PrintStream(new FileOutputStream(FileDescriptor.err), true, StandardCharsets.UTF_8));
    }

    public void titulo(String texto) {
        saida.printf("%n%s%n%s%s%n%s%n", LINHA, RECUO, texto, LINHA);
    }

    public void info(String texto) {
        saida.println(RECUO + texto);
    }

    public void mensagem(String texto) {
        saida.println(texto);
    }

    /** Pergunta sem quebrar a linha, para a resposta ficar na frente do texto. */
    public void pergunta(String texto) {
        saida.print(texto);
        saida.flush();
    }

    public void erro(String texto) {
        erros.println("[ERRO] " + texto);
    }
}

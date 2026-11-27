package br.edu.seminario.qdrant.core.util;

import java.util.Optional;

/** Leitura dos argumentos da linha de comando (opções no formato {@code --nome valor}). */
public final class Argumentos {

    private Argumentos() { }

    /** Valor da opção {@code nome}, se ela foi informada com um valor logo depois. */
    public static Optional<String> opcao(String[] argumentos, String nome) {
        for (int i = 0; i < argumentos.length - 1; i++) {
            if (argumentos[i].equals(nome)) {
                return Optional.of(argumentos[i + 1]);
            }
        }
        return Optional.empty();
    }

    /** Converte texto em número, aceitando vírgula ou ponto como separador decimal. */
    public static double numero(String texto) {
        try {
            return Double.parseDouble(texto.trim().replace(',', '.'));
        } catch (NumberFormatException erro) {
            throw new IllegalArgumentException("Valor numérico inválido: " + texto, erro);
        }
    }

    /** Converte texto em número inteiro (ID, estoque, limite). */
    public static long inteiro(String texto) {
        double valor = numero(texto);
        if (valor != Math.rint(valor)) {
            throw new IllegalArgumentException("Esperado um número inteiro: " + texto);
        }
        return (long) valor;
    }
}

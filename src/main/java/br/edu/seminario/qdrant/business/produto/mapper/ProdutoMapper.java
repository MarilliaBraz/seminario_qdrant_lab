package br.edu.seminario.qdrant.business.produto.mapper;

import static io.qdrant.client.ValueFactory.value;

import br.edu.seminario.qdrant.business.produto.model.Produto;
import io.qdrant.client.grpc.JsonWithInt.Value;
import java.util.Map;

/** Converte o produto para o payload JSON do Qdrant e de volta. O ID fica fora do payload: é o ID do ponto. */
public final class ProdutoMapper {

    public static final String CAMPO_NOME = "nome";
    public static final String CAMPO_DESCRICAO = "descricao";
    public static final String CAMPO_CATEGORIA = "categoria";
    public static final String CAMPO_PRECO = "preco";
    public static final String CAMPO_ESTOQUE = "estoque";

    private ProdutoMapper() { }

    public static Map<String, Value> paraPayload(Produto produto) {
        return Map.of(
                CAMPO_NOME, value(produto.nome()),
                CAMPO_DESCRICAO, value(produto.descricao()),
                CAMPO_CATEGORIA, value(produto.categoria()),
                CAMPO_PRECO, value(produto.preco()),
                CAMPO_ESTOQUE, value(produto.estoque()));
    }

    public static Produto dePayload(long id, Map<String, Value> payload) {
        return new Produto(
                id,
                campo(payload, CAMPO_NOME).getStringValue(),
                campo(payload, CAMPO_DESCRICAO).getStringValue(),
                campo(payload, CAMPO_CATEGORIA).getStringValue(),
                campo(payload, CAMPO_PRECO).getDoubleValue(),
                campo(payload, CAMPO_ESTOQUE).getIntegerValue());
    }

    private static Value campo(Map<String, Value> payload, String nome) {
        Value valor = payload.get(nome);
        if (valor == null) {
            throw new IllegalStateException("Payload sem o campo obrigatório '" + nome + "'");
        }
        return valor;
    }
}

package br.edu.seminario.qdrant.business.produto.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import br.edu.seminario.qdrant.business.produto.model.Produto;
import io.qdrant.client.grpc.JsonWithInt.Value;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ProdutoMapperTest {

    private static final Produto FONE = new Produto(1, "Fone Bluetooth Esportivo",
            "Fone sem fio à prova de suor", "eletronicos", 199.90, 35);

    @Test
    void payloadVoltaComoOMesmoProduto() {
        assertEquals(FONE, ProdutoMapper.dePayload(1, ProdutoMapper.paraPayload(FONE)));
    }

    @Test
    void idNaoVaiParaOPayload() {
        assertFalse(ProdutoMapper.paraPayload(FONE).containsKey("id"));
    }

    @Test
    void payloadSemCampoObrigatorioGeraErroClaro() {
        Map<String, Value> payload = new HashMap<>(ProdutoMapper.paraPayload(FONE));
        payload.remove(ProdutoMapper.CAMPO_PRECO);

        IllegalStateException erro = assertThrows(IllegalStateException.class, () -> ProdutoMapper.dePayload(1, payload));

        assertEquals("Payload sem o campo obrigatório 'preco'", erro.getMessage());
    }
}

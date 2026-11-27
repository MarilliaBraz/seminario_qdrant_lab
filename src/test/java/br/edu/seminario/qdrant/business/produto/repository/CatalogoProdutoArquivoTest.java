package br.edu.seminario.qdrant.business.produto.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.edu.seminario.qdrant.business.produto.model.Produto;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CatalogoProdutoArquivoTest {

    @TempDir
    Path pasta;

    @Test
    void leOsTrintaProdutosDoCatalogoDoLaboratorio() {
        List<Produto> produtos = new CatalogoProdutoArquivo(Path.of("dados", "produtos.json")).ler();

        assertEquals(30, produtos.size());
        assertEquals(new Produto(1, "Fone Bluetooth Esportivo",
                "Fone sem fio à prova de suor, ideal para correr e treinar ouvindo música",
                "eletronicos", 199.90, 35), produtos.get(0));
        assertEquals(3, produtos.stream().filter(p -> p.categoria().equals("pets")).count());
    }

    @Test
    void catalogoVazioGeraErroClaro() throws IOException {
        Path arquivo = Files.writeString(pasta.resolve("vazio.json"), "[]");

        IllegalStateException erro = assertThrows(IllegalStateException.class, () -> new CatalogoProdutoArquivo(arquivo).ler());

        assertTrue(erro.getMessage().endsWith("está vazio"));
    }

    @Test
    void arquivoInexistenteGeraErroClaro() {
        Path arquivo = pasta.resolve("nao-existe.json");

        IllegalStateException erro = assertThrows(IllegalStateException.class, () -> new CatalogoProdutoArquivo(arquivo).ler());

        assertTrue(erro.getMessage().startsWith("Não foi possível ler"));
    }
}

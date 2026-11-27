package br.edu.seminario.qdrant.core.config;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import java.util.concurrent.ExecutionException;

/** Cria o cliente gRPC do Qdrant e confirma que o servidor responde antes de seguir. */
public final class QdrantClienteFactory {

    private QdrantClienteFactory() { }

    public static QdrantClient conectar(String host, int portaGrpc) {
        QdrantClient cliente = new QdrantClient(QdrantGrpcClient.newBuilder(host, portaGrpc, false).build());
        try {
            cliente.healthCheckAsync().get();
            return cliente;
        } catch (InterruptedException erro) {
            cliente.close();
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Conexão com o Qdrant interrompida", erro);
        } catch (ExecutionException erro) {
            cliente.close();
            throw new IllegalStateException("Qdrant indisponível em " + host + ":" + portaGrpc
                    + ". Rode 'docker compose up -d'.", erro);
        }
    }
}

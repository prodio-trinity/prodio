package com.prodio.stat.embedding.application;

import com.prodio.catalog.ClientMemoEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class ClientEmbeddingListener {

    private final ClientEmbeddingRepository clientEmbeddingRepository;
    private final ClientEmbeddingWriter clientEmbeddingWriter;

    @ApplicationModuleListener
    void handle(ClientMemoEvent event) {
        MeaningfulTextEmbedder.upsertIfMeaningful(clientEmbeddingRepository, clientEmbeddingWriter,
                event.clientId(), event.memo(), () -> ClientEmbeddingTextBuilder.from(event));
    }
}

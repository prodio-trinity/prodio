package com.prodio.stat.embedding.application;

import com.prodio.stat.application.AiClient;
import org.springframework.stereotype.Component;

@Component
class OrderEmbeddingWriter extends AbstractEmbeddingWriter {

    OrderEmbeddingWriter(OrderEmbeddingRepository repository, AiClient aiClient) {
        super(repository, aiClient);
    }
}

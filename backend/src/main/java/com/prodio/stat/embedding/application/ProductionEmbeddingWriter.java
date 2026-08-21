package com.prodio.stat.embedding.application;

import com.prodio.stat.application.AiClient;
import org.springframework.stereotype.Component;

@Component
class ProductionEmbeddingWriter extends AbstractEmbeddingWriter {

    ProductionEmbeddingWriter(ProductionEmbeddingRepository repository, AiClient aiClient) {
        super(repository, aiClient);
    }
}

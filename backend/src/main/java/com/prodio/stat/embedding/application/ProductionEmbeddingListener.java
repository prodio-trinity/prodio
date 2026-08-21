package com.prodio.stat.embedding.application;

import com.prodio.production.event.ProductionMemo;
import com.prodio.stat.application.OrderStatViewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class ProductionEmbeddingListener {

    private final OrderStatViewRepository orderStatViewRepository;
    private final ProductionEmbeddingRepository productionEmbeddingRepository;
    private final ProductionEmbeddingWriter productionEmbeddingWriter;

    @ApplicationModuleListener
    void handle(ProductionMemo event) {
        long orderId = event.orderId();
        MeaningfulTextEmbedder.upsertIfMeaningful(productionEmbeddingRepository, productionEmbeddingWriter,
                orderId, event.memo(), () -> ProductionEmbeddingTextBuilder.from(orderId,
                        orderStatViewRepository.findAllByOrderId(orderId), event.memo()));
    }
}

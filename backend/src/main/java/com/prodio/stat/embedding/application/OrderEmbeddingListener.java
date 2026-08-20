package com.prodio.stat.embedding.application;

import com.prodio.order.OrderCancelledEvent;
import com.prodio.order.OrderCreatedEvent;
import com.prodio.order.OrderUpdatedEvent;
import com.prodio.stat.application.OrderStatViewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
class OrderEmbeddingListener {

    private final OrderEmbeddingRepository orderEmbeddingRepository;
    private final OrderStatViewRepository orderStatViewRepository;
    private final OrderEmbeddingWriter orderEmbeddingWriter;

    @ApplicationModuleListener
    void handle(OrderCreatedEvent event) {
        MeaningfulTextEmbedder.upsertIfMeaningful(orderEmbeddingRepository, orderEmbeddingWriter,
                event.orderId(), event.note(), () -> OrderEmbeddingTextBuilder.from(event));
    }

    @ApplicationModuleListener
    void handle(OrderUpdatedEvent event) {
        MeaningfulTextEmbedder.upsertIfMeaningful(orderEmbeddingRepository, orderEmbeddingWriter,
                event.orderId(), event.note(), () -> OrderEmbeddingTextBuilder.from(event));
    }

    @ApplicationModuleListener
    void handle(OrderCancelledEvent event) {
        Optional<String> existingText = orderEmbeddingRepository.findText(event.orderId());
        String reasonLine = "[취소사유] " + event.cancellationReason();
        // 이벤트 재발행(republish-outstanding-events-on-restart) 시 이 취소사유가 중복 append되지 않도록 가드.
        // 문구 존재 여부가 아니라 이번 취소사유 그대로 끝나는지를 봐서, 고객이 남긴 note에 우연히
        // "[취소사유]"라는 텍스트가 들어있어도 오탐하지 않는다.
        if (existingText.map(text -> text.endsWith(reasonLine)).orElse(false)) {
            return;
        }

        String text = existingText.isPresent()
                ? OrderEmbeddingTextBuilder.appendCancellationReason(existingText.get(), event.cancellationReason())
                : OrderEmbeddingTextBuilder.cancelledWithoutNote(event.orderId(),
                        orderStatViewRepository.findAllByOrderId(event.orderId()), event.cancellationReason());

        orderEmbeddingWriter.upsert(event.orderId(), text);
    }
}

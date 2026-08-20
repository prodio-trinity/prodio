package com.prodio.stat.embedding.application;

import com.prodio.order.OrderCancelledEvent;
import com.prodio.order.OrderCreatedEvent;
import com.prodio.order.OrderUpdatedEvent;
import com.prodio.stat.application.AiClient;
import com.prodio.stat.application.OrderStatViewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
class OrderEmbeddingListener {

    private final OrderEmbeddingRepository orderEmbeddingRepository;
    private final OrderStatViewRepository orderStatViewRepository;
    private final AiClient aiClient;

    @ApplicationModuleListener
    void handle(OrderCreatedEvent event) {
        upsertIfMeaningful(event.orderId(), event.note(), () -> OrderEmbeddingTextBuilder.from(event));
    }

    @ApplicationModuleListener
    void handle(OrderUpdatedEvent event) {
        upsertIfMeaningful(event.orderId(), event.note(), () -> OrderEmbeddingTextBuilder.from(event));
    }

    @ApplicationModuleListener
    void handle(OrderCancelledEvent event) {
        Optional<String> existingText = orderEmbeddingRepository.findNoteText(event.orderId());
        // 이벤트 재발행(republish-outstanding-events-on-restart) 시 취소사유가 중복 append되지 않도록 가드
        if (existingText.map(text -> text.contains("[취소사유]")).orElse(false)) {
            return;
        }

        String text = existingText.isPresent()
                ? OrderEmbeddingTextBuilder.appendCancellationReason(existingText.get(), event.cancellationReason())
                : OrderEmbeddingTextBuilder.cancelledWithoutNote(
                        orderStatViewRepository.findAllByOrderId(event.orderId()), event.cancellationReason());
        upsert(event.orderId(), text);
    }

    /** note가 비어 있으면 임베딩할 자유 텍스트가 없어 스킵하고, 기존 저장값과 같으면 재임베딩을 생략한다. */
    private void upsertIfMeaningful(long orderId, String note, Supplier<String> textSupplier) {
        if (note == null || note.isBlank()) {
            return;
        }

        String text = textSupplier.get();
        if (orderEmbeddingRepository.findNoteText(orderId).map(text::equals).orElse(false)) {
            return;
        }

        upsert(orderId, text);
    }

    private void upsert(long orderId, String text) {
        orderEmbeddingRepository.upsert(orderId, text, aiClient.embed(text));
    }
}

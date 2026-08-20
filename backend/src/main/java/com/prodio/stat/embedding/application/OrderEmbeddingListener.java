package com.prodio.stat.embedding.application;

import com.prodio.order.OrderCancelledEvent;
import com.prodio.order.OrderCreatedEvent;
import com.prodio.order.OrderUpdatedEvent;
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
    private final OrderEmbeddingWriter orderEmbeddingWriter;

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

    /** note가 비어 있으면 임베딩할 자유 텍스트가 없어 스킵하고, 기존 저장값과 같으면 재임베딩을 생략한다. */
    private void upsertIfMeaningful(long orderId, String note, Supplier<String> textSupplier) {
        if (note == null || note.isBlank()) {
            return;
        }

        String text = textSupplier.get();
        if (orderEmbeddingRepository.findNoteText(orderId).map(text::equals).orElse(false)) {
            return;
        }

        orderEmbeddingWriter.upsert(orderId, text);
    }
}

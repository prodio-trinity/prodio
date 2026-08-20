package com.prodio.stat.embedding.application;

import com.prodio.order.OrderCreatedEvent;
import com.prodio.order.OrderItemEventData;
import com.prodio.order.OrderUpdatedEvent;
import com.prodio.stat.domain.OrderStatView;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * OrderCreated/Updated는 매번 최신 이벤트 데이터로 처음부터 다시 조합하고(replace),
 * 취소 사유는 기존 텍스트에 이어붙인다(append).
 *
 * note가 비어 있어서 기존 텍스트 자체가 없던 주문이 취소되면,
 * stat 자체 read model인 OrderStatView에서 품목/거래처명을 새로 조회해 문맥을 채운다.
 */
final class OrderEmbeddingTextBuilder {

    private OrderEmbeddingTextBuilder() {}

    static String from(OrderCreatedEvent event) {
        return build(event.orderName(), event.clientName(), itemSummaryFrom(event.items()), event.note());
    }

    static String from(OrderUpdatedEvent event) {
        return build(event.orderName(), event.clientName(), itemSummaryFrom(event.items()), event.note());
    }

    static String appendCancellationReason(String existingText, String cancellationReason) {
        String reasonLine = "[취소사유] " + cancellationReason;

        return (existingText == null || existingText.isBlank())
                ? reasonLine
                : existingText + "\n" + reasonLine;
    }

    static String cancelledWithoutNote(List<OrderStatView> views, String cancellationReason) {
        String clientName = views.get(0).clientName();
        String itemSummary = views.stream()
                .map(view -> view.productName() + " " + view.quantity() + "개")
                .collect(Collectors.joining(", "));

        return build(null, clientName, itemSummary, "[취소사유] " + cancellationReason);
    }

    private static String itemSummaryFrom(List<OrderItemEventData> items) {
        return items.stream()
                .map(item -> item.productName() + " " + item.quantity() + "개")
                .collect(Collectors.joining(", "));
    }

    private static String build(String orderName, String clientName, String itemSummary, String note) {
        String context = Stream.of(orderName, itemSummary, clientName + " 주문")
                .filter(part -> part != null && !part.isBlank())
                .collect(Collectors.joining(", "));

        return "[" + context + "] " + note;
    }
}

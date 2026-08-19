package com.prodio.stat.domain;

import java.time.OffsetDateTime;

/**
 * order/production 모듈이 발행하는 이벤트를 구독해 채우는 조회 전용 read model.
 * 주문이 다품목(cart)을 지원하므로 "품목 1개 = row 1개"다 — 같은 주문에 속한 row들은
 * orderId가 같고 productId로 구분되며, status 등 주문 단위 정보는 모든 row에 동일하게 갱신된다.
 * 각 필드는 최초 생성 이후 서로 다른 이벤트가 도착할 때마다 부분적으로 갱신된다.
 */
public record OrderStatView(
        Long id,
        long orderId,
        long clientId,
        String clientName,
        long productId,
        String productName,
        int quantity,
        long lineAmount,
        OrderViewStatus status,
        boolean paymentConfirmed,
        OffsetDateTime orderCreatedAt,
        OffsetDateTime productionStartedAt,
        OffsetDateTime shippedAt,
        OffsetDateTime completedAt,
        String cancellationReason,
        OffsetDateTime cancelledAt
) {
    public static OrderStatView create(long orderId, long clientId, String clientName,
            long productId, String productName, int quantity, long lineAmount,
            OffsetDateTime orderCreatedAt
    ) {
        return new OrderStatView(
            null, orderId, clientId,
            clientName, productId, productName,
            quantity, lineAmount,
            OrderViewStatus.PENDING, false,
            orderCreatedAt, null, null, null,
            null, null
        );
    }
}

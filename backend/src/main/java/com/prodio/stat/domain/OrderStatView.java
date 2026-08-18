package com.prodio.stat.domain;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * order/production 모듈이 발행하는 이벤트를 구독해 채우는 조회 전용 read model.
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
        long totalAmount,
        LocalDate dueDate,
        OrderViewStatus status,
        boolean paymentConfirmed,
        Boolean onTime,
        OffsetDateTime orderCreatedAt,
        OffsetDateTime productionStartedAt,
        OffsetDateTime shippedAt,
        OffsetDateTime completedAt
) {
    public static OrderStatView create(long orderId, long clientId, String clientName,
            long productId, String productName, int quantity, long totalAmount,
            LocalDate dueDate, OffsetDateTime orderCreatedAt
    ) {
        return new OrderStatView(
            null, orderId, clientId,
            clientName, productId, productName,
                quantity, totalAmount, dueDate,
            OrderViewStatus.PENDING, false, null,
            orderCreatedAt, null, null, null
        );
    }
}

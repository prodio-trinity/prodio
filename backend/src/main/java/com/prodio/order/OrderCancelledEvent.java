package com.prodio.order;

import com.prodio.order.domain.Order;

import java.time.OffsetDateTime;

public record OrderCancelledEvent(long orderId, String cancellationReason,
        OffsetDateTime cancelledAt) {
    public static OrderCancelledEvent from(Order order) {
        return new OrderCancelledEvent(order.id(), order.cancellationReason(), order.updatedAt());
    }
}

package com.prodio.order;

import com.prodio.order.domain.Order;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record OrderUpdatedEvent(long orderId, long clientId, String clientName,
        long productId, String productName, int quantity, long totalAmount,
        LocalDate dueDate, OffsetDateTime updatedAt) {
    public static OrderUpdatedEvent from(Order order) {
        return new OrderUpdatedEvent(order.id(), order.clientId(), order.clientNameSnapshot(),
                order.productId(), order.productNameSnapshot(), order.quantity(),
                order.totalAmount(), order.dueDate(), order.updatedAt());
    }
}

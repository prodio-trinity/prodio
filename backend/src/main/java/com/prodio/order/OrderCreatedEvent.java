package com.prodio.order;

import com.prodio.order.domain.Order;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record OrderCreatedEvent(long orderId, long clientId, String clientName,
        String clientPhone, long productId, String productName, int quantity,
        long totalAmount, LocalDate dueDate, OffsetDateTime createdAt) {
    public static OrderCreatedEvent from(Order order) {
        return new OrderCreatedEvent(order.id(), order.clientId(), order.clientNameSnapshot(),
                order.clientPhoneSnapshot(), order.productId(), order.productNameSnapshot(),
                order.quantity(), order.totalAmount(), order.dueDate(), order.createdAt());
    }
}

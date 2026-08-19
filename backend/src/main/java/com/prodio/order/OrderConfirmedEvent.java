package com.prodio.order;

import com.prodio.order.domain.Order;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record OrderConfirmedEvent(long orderId, long clientId, String clientName,
        String clientPhone, long productId, String productName, int quantity,
        long totalAmount, LocalDate dueDate, String deliveryAddress, String note,
        OffsetDateTime createdAt, OffsetDateTime confirmedAt) {
    public static OrderConfirmedEvent from(Order order) {
        return new OrderConfirmedEvent(order.id(), order.clientId(), order.clientNameSnapshot(),
                order.clientPhoneSnapshot(), order.productId(), order.productNameSnapshot(),
                order.quantity(), order.totalAmount(), order.dueDate(), order.deliveryAddress(),
                order.note(), order.createdAt(), order.updatedAt());
    }
}

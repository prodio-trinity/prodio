package com.prodio.order;

import com.prodio.order.domain.Order;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public record OrderConfirmedEvent(long orderId, long clientId, String clientName,
        String clientPhone, List<OrderItemEventData> items,
        long totalAmount, LocalDate dueDate, OrderDeliveryEventData delivery, String note,
        OffsetDateTime createdAt, OffsetDateTime confirmedAt) {
    public static OrderConfirmedEvent from(Order order) {
        return new OrderConfirmedEvent(order.id(), order.clientId(), order.clientNameSnapshot(),
                order.clientPhoneSnapshot(), order.items().stream().map(OrderItemEventData::from).toList(),
                order.totalAmount(), order.dueDate(), OrderDeliveryEventData.from(order.delivery()),
                order.note(), order.createdAt(), order.updatedAt());
    }
}

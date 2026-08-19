package com.prodio.order;

import com.prodio.order.domain.Order;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public record OrderCreatedEvent(long orderId, long clientId, String clientName,
        String clientPhone, List<OrderItemEventData> items,
        long totalAmount, LocalDate dueDate, OrderDeliveryEventData delivery,
        OffsetDateTime createdAt) {
    public static OrderCreatedEvent from(Order order) {
        return new OrderCreatedEvent(order.id(), order.clientId(), order.clientNameSnapshot(),
                order.clientPhoneSnapshot(), order.items().stream().map(OrderItemEventData::from).toList(),
                order.totalAmount(), order.dueDate(), OrderDeliveryEventData.from(order.delivery()),
                order.createdAt());
    }
}

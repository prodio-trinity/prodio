package com.prodio.order;

import com.prodio.order.domain.Order;

import java.time.OffsetDateTime;
import java.util.List;

public record OrderCreatedEvent(long orderId, long clientId, String clientName,
        String clientContact, String orderName, List<OrderItemEventData> items,
        boolean vatIncluded, long totalAmount, OrderDeliveryEventData delivery, String note,
        OffsetDateTime createdAt) {
    public static OrderCreatedEvent from(Order order) {
        return new OrderCreatedEvent(order.id(), order.clientId(), order.clientNameSnapshot(),
                order.clientContactSnapshot(), order.orderName(),
                order.items().stream().map(OrderItemEventData::from).toList(),
                order.vatIncluded(), order.totalAmount(), OrderDeliveryEventData.from(order.delivery()),
                order.note(),
                order.createdAt());
    }

}

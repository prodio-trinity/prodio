package com.prodio.order;

import com.prodio.order.domain.Order;

import java.time.OffsetDateTime;
import java.util.List;

public record OrderConfirmedEvent(long orderId, long clientId, String clientName,
        String clientContact, String orderName, List<OrderItemEventData> items,
        boolean vatIncluded, long totalAmount, OrderDeliveryEventData delivery, String note,
        OffsetDateTime createdAt, OffsetDateTime confirmedAt) {
    public static OrderConfirmedEvent from(Order order) {
        return new OrderConfirmedEvent(order.id(), order.clientId(), order.clientNameSnapshot(),
                order.clientContactSnapshot(), order.orderName(),
                order.items().stream().map(OrderItemEventData::from).toList(),
                order.vatIncluded(), order.totalAmount(), OrderDeliveryEventData.from(order.delivery()),
                order.note(), order.createdAt(), order.updatedAt());
    }
}

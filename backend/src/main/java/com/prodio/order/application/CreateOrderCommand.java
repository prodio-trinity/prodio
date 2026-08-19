package com.prodio.order.application;

import java.util.List;

public record CreateOrderCommand(long clientId, String orderName, List<OrderItemCommand> items,
        boolean vatIncluded, DeliveryCommand delivery,
        String note, long createdBy) {
}

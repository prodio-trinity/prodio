package com.prodio.order.application;

import java.util.List;

public record UpdateOrderCommand(String orderName, List<OrderItemCommand> items, boolean vatIncluded,
        DeliveryCommand delivery, String note) {
}

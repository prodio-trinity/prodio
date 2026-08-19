package com.prodio.order.application;

import java.time.LocalDate;
import java.util.List;

public record UpdateOrderCommand(List<OrderItemCommand> items, boolean vatIncluded,
        LocalDate dueDate, DeliveryCommand delivery, String note) {
}

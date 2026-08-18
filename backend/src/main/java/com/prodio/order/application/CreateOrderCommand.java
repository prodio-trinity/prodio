package com.prodio.order.application;

import java.time.LocalDate;

public record CreateOrderCommand(long clientId, long productId, int quantity,
        boolean vatIncluded, LocalDate dueDate, String deliveryAddress,
        String note, long createdBy) {
}

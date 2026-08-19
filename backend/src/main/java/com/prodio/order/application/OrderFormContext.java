package com.prodio.order.application;

import java.util.List;

public record OrderFormContext(OrderClientContext client, List<OrderProductContext> products) {
    public OrderFormContext {
        products = List.copyOf(products);
    }
}

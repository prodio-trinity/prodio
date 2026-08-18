package com.prodio.order.application;

import com.prodio.order.domain.Order;

import java.util.List;

public record OrderPage(List<Order> orders, int page, int size, long totalElements) {
    public OrderPage {
        orders = List.copyOf(orders);
    }
}

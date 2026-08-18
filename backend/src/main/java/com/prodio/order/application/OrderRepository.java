package com.prodio.order.application;

import com.prodio.order.domain.Order;
import com.prodio.order.domain.OrderStatus;

import java.util.Optional;

public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(long id);
    Optional<Order> findByIdForUpdate(long id);
    OrderPage findAll(OrderStatus status, String query, int page, int size);
}

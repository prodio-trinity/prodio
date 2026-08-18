package com.prodio.stat.application;

import com.prodio.stat.domain.OrderStatView;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface OrderStatViewRepository {
    OrderStatView create(OrderStatView view);
    void markProductionStarted(long orderId, OffsetDateTime startedAt);
    void markShipped(long orderId, OffsetDateTime shippedAt);
    void markCompleted(long orderId, OffsetDateTime completedAt, boolean onTime);
    void confirmPayment(long orderId);
    Optional<OrderStatView> findByOrderId(long orderId);
}

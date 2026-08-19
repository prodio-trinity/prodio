package com.prodio.stat.application;

import com.prodio.stat.domain.OrderStatView;

import java.time.OffsetDateTime;
import java.util.List;

public interface OrderStatViewRepository {
    OrderStatView create(OrderStatView view);
    void markProductionStarted(long orderId, OffsetDateTime startedAt);
    void markShipped(long orderId, OffsetDateTime shippedAt);
    void markCompleted(long orderId, OffsetDateTime completedAt);
    void markCancelled(long orderId, String cancellationReason, OffsetDateTime cancelledAt);
    void confirmPayment(long orderId);
    void deleteAllByOrderId(long orderId);
    List<OrderStatView> findAllByOrderId(long orderId);
}

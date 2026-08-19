package com.prodio.stat.application;

import com.prodio.order.OrderCancelledEvent;
import com.prodio.order.OrderConfirmedEvent;
import com.prodio.order.OrderCreatedEvent;
import com.prodio.order.OrderItemEventData;
import com.prodio.order.OrderUpdatedEvent;
import com.prodio.production.event.OrderCompleted;
import com.prodio.production.event.OrderShipped;
import com.prodio.stat.domain.OrderStatView;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class StatListener {

    private final OrderStatViewRepository repository;

    @ApplicationModuleListener
    public void handle(OrderCreatedEvent event) {
        for (OrderItemEventData item : event.items()) {
            repository.create(OrderStatView.create(event.orderId(), event.clientId(), event.clientName(),
                    item.productId(), item.productName(), item.quantity(), item.lineAmount(),
                    event.createdAt()));
        }
    }

    @ApplicationModuleListener
    public void handle(OrderConfirmedEvent event) {
        repository.markProductionStarted(event.orderId(), event.confirmedAt());
    }

    @ApplicationModuleListener
    public void handle(OrderShipped event) {
        repository.markShipped(event.orderId(), event.shippedAt());
    }

    @ApplicationModuleListener
    public void handle(OrderCompleted event) {
        repository.markCompleted(event.orderId(), event.completedAt());
    }

    @ApplicationModuleListener
    public void handle(OrderCancelledEvent event) {
        repository.markCancelled(event.orderId(), event.cancellationReason(), event.cancelledAt());
    }

    @ApplicationModuleListener
    public void handle(OrderUpdatedEvent event) {
        List<OrderStatView> existing = repository.findAllByOrderId(event.orderId());
        if (existing.isEmpty()) {
            throw new IllegalStateException("OrderStatView를 찾을 수 없습니다. orderId=" + event.orderId());
        }
        OffsetDateTime orderCreatedAt = existing.get(0).orderCreatedAt();

        repository.deleteAllByOrderId(event.orderId());
        for (OrderItemEventData item : event.items()) {
            repository.create(OrderStatView.create(event.orderId(), event.clientId(), event.clientName(),
                    item.productId(), item.productName(), item.quantity(), item.lineAmount(),
                    orderCreatedAt));
        }
    }
}

package com.prodio.stat.application;

import com.prodio.order.OrderCancelledEvent;
import com.prodio.order.OrderConfirmedEvent;
import com.prodio.order.OrderCreatedEvent;
import com.prodio.order.OrderItemEventData;
import com.prodio.production.event.OrderCompleted;
import com.prodio.production.event.OrderShipped;
import com.prodio.stat.domain.OrderStatView;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

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
                    event.dueDate(), event.createdAt()));
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
        List<OrderStatView> views = repository.findAllByOrderId(event.orderId());
        if (views.isEmpty()) {
            throw new IllegalStateException("OrderStatView를 찾을 수 없습니다. orderId=" + event.orderId());
        }
        boolean onTime = !event.completedAt().toLocalDate().isAfter(views.get(0).dueDate());

        repository.markCompleted(event.orderId(), event.completedAt(), onTime);
    }

    @ApplicationModuleListener
    public void handle(OrderCancelledEvent event) {
        repository.markCancelled(event.orderId(), event.cancellationReason(), event.cancelledAt());
    }
}

package com.prodio.stat.application;

import com.prodio.stat.SampleOrderCancelledEvent;
import com.prodio.stat.SampleOrderCompletedEvent;
import com.prodio.stat.SampleOrderCreatedEvent;
import com.prodio.stat.SampleOrderShippedEvent;
import com.prodio.stat.SampleOrderStartedEvent;
import com.prodio.stat.domain.OrderStatView;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StatListener {

    private final OrderStatViewRepository repository;

    @ApplicationModuleListener
    public void handle(SampleOrderCreatedEvent event) {
        repository.create(OrderStatView.create(event.orderId(), event.clientId(), event.clientName(),
                event.productId(), event.productName(), event.quantity(), event.totalAmount(),
                event.dueDate(), event.createdAt()));
    }

    @ApplicationModuleListener
    public void handle(SampleOrderStartedEvent event) {
        repository.markProductionStarted(event.orderId(), event.startedAt());
    }

    @ApplicationModuleListener
    public void handle(SampleOrderShippedEvent event) {
        repository.markShipped(event.orderId(), event.shippedAt());
    }

    @ApplicationModuleListener
    public void handle(SampleOrderCompletedEvent event) {
        OrderStatView view = repository.findByOrderId(event.orderId())
                .orElseThrow(() -> new IllegalStateException("OrderStatView를 찾을 수 없습니다. orderId=" + event.orderId()));
        boolean onTime = !event.completedAt().toLocalDate().isAfter(view.dueDate());

        repository.markCompleted(event.orderId(), event.completedAt(), onTime);
    }

    @ApplicationModuleListener
    public void handle(SampleOrderCancelledEvent event) {
        repository.markCancelled(event.orderId(), event.cancellationReason(), event.cancelledAt());
    }
}

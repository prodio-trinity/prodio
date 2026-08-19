package com.prodio.stat.application;

import com.prodio.stat.SampleOrderCancelledEvent;
import com.prodio.stat.SampleOrderCompletedEvent;
import com.prodio.stat.SampleOrderCreatedEvent;
import com.prodio.stat.SampleOrderItem;
import com.prodio.stat.SampleOrderShippedEvent;
import com.prodio.stat.SampleOrderStartedEvent;
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
    public void handle(SampleOrderCreatedEvent event) {
        for (SampleOrderItem item : event.items()) {
            repository.create(OrderStatView.create(event.orderId(), event.clientId(), event.clientName(),
                    item.productId(), item.productName(), item.quantity(), item.lineAmount(),
                    event.dueDate(), event.createdAt()));
        }
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
        List<OrderStatView> views = repository.findAllByOrderId(event.orderId());
        if (views.isEmpty()) {
            throw new IllegalStateException("OrderStatView를 찾을 수 없습니다. orderId=" + event.orderId());
        }
        boolean onTime = !event.completedAt().toLocalDate().isAfter(views.get(0).dueDate());

        repository.markCompleted(event.orderId(), event.completedAt(), onTime);
    }

    @ApplicationModuleListener
    public void handle(SampleOrderCancelledEvent event) {
        repository.markCancelled(event.orderId(), event.cancellationReason(), event.cancelledAt());
    }
}

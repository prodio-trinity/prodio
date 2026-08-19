package com.prodio.stat.infrastructure.persistence;

import com.prodio.stat.application.OrderStatViewRepository;
import com.prodio.stat.domain.OrderStatView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
class JpaOrderStatViewRepository implements OrderStatViewRepository {
    private final OrderStatViewJpaRepository orderStatViews;

    @Override
    public OrderStatView create(OrderStatView view) {
        return orderStatViews.save(OrderStatViewEntity.from(view)).toDomain();
    }

    @Override
    public void markProductionStarted(long orderId, OffsetDateTime startedAt) {
        findEntity(orderId).markProductionStarted(startedAt);
    }

    @Override
    public void markShipped(long orderId, OffsetDateTime shippedAt) {
        findEntity(orderId).markShipped(shippedAt);
    }

    @Override
    public void markCompleted(long orderId, OffsetDateTime completedAt, boolean onTime) {
        findEntity(orderId).markCompleted(completedAt, onTime);
    }

    @Override
    public void markCancelled(long orderId, String cancellationReason, OffsetDateTime cancelledAt) {
        findEntity(orderId).markCancelled(cancellationReason, cancelledAt);
    }

    @Override
    public void confirmPayment(long orderId) {
        findEntity(orderId).confirmPayment();
    }

    @Override
    public Optional<OrderStatView> findByOrderId(long orderId) {
        return orderStatViews.findByOrderId(orderId).map(OrderStatViewEntity::toDomain);
    }

    /** 호출부(@ApplicationModuleListener)가 연 트랜잭션 안에서 실행되므로, 조회한 영속 엔티티를
     * 변경해두면 별도 save() 없이 커밋 시점에 반영된다(JPA dirty checking). */
    private OrderStatViewEntity findEntity(long orderId) {
        return orderStatViews.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalStateException("OrderStatView를 찾을 수 없습니다. orderId=" + orderId));
    }
}

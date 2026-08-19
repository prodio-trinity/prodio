package com.prodio.stat.infrastructure.persistence;

import com.prodio.stat.application.OrderStatViewRepository;
import com.prodio.stat.domain.OrderStatView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

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
        findEntities(orderId).forEach(entity -> entity.markProductionStarted(startedAt));
    }

    @Override
    public void markShipped(long orderId, OffsetDateTime shippedAt) {
        findEntities(orderId).forEach(entity -> entity.markShipped(shippedAt));
    }

    @Override
    public void markCompleted(long orderId, OffsetDateTime completedAt, boolean onTime) {
        findEntities(orderId).forEach(entity -> entity.markCompleted(completedAt, onTime));
    }

    @Override
    public void markCancelled(long orderId, String cancellationReason, OffsetDateTime cancelledAt) {
        findEntities(orderId).forEach(entity -> entity.markCancelled(cancellationReason, cancelledAt));
    }

    @Override
    public void confirmPayment(long orderId) {
        findEntities(orderId).forEach(OrderStatViewEntity::confirmPayment);
    }

    @Override
    public List<OrderStatView> findAllByOrderId(long orderId) {
        return orderStatViews.findAllByOrderId(orderId).stream().map(OrderStatViewEntity::toDomain).toList();
    }

    /** 호출부(@ApplicationModuleListener)가 연 트랜잭션 안에서 실행되므로, 조회한 영속 엔티티를
     * 변경해두면 별도 save() 없이 커밋 시점에 반영된다(JPA dirty checking).
     * 한 주문에 품목마다 row가 있어서, 상태 전이는 그 주문에 속한 row 전부에 적용한다. */
    private List<OrderStatViewEntity> findEntities(long orderId) {
        List<OrderStatViewEntity> entities = orderStatViews.findAllByOrderId(orderId);
        if (entities.isEmpty()) {
            throw new IllegalStateException("OrderStatView를 찾을 수 없습니다. orderId=" + orderId);
        }
        return entities;
    }
}

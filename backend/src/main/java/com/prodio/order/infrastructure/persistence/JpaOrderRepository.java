package com.prodio.order.infrastructure.persistence;

import com.prodio.order.application.OrderPage;
import com.prodio.order.application.OrderRepository;
import com.prodio.order.domain.Order;
import com.prodio.order.domain.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.Locale;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
class JpaOrderRepository implements OrderRepository {
    private final SpringDataOrderRepository orders;

    @Override
    public Order save(Order order) {
        OrderEntity entity;
        if (order.id() == 0) {
            entity = OrderEntity.from(order);
        } else {
            entity = orders.findById(order.id()).orElseThrow();
            entity.apply(order);
        }
        return orders.save(entity).toDomain();
    }

    @Override
    public Optional<Order> findById(long id) {
        return orders.findById(id).map(OrderEntity::toDomain);
    }

    @Override
    public Optional<Order> findByIdForUpdate(long id) {
        return orders.findByIdForUpdate(id).map(OrderEntity::toDomain);
    }

    @Override
    public OrderPage findAll(OrderStatus status, String query, int page, int size) {
        Specification<OrderEntity> specification = Specification.unrestricted();
        if (status != null) {
            specification = specification.and((root, criteria, builder) ->
                    builder.equal(root.get("status"), status));
        }
        if (query != null && !query.isBlank()) {
            String pattern = "%" + query.toLowerCase(Locale.ROOT) + "%";
            specification = specification.and((root, criteria, builder) -> builder.or(
                    builder.like(builder.lower(root.get("clientNameSnapshot")), pattern),
                    builder.like(builder.lower(root.get("productNameSnapshot")), pattern)));
        }
        var result = orders.findAll(specification,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return new OrderPage(result.getContent().stream().map(OrderEntity::toDomain).toList(),
                page, size, result.getTotalElements());
    }
}

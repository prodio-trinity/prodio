package com.prodio.order.infrastructure.persistence;

import com.prodio.order.application.OrderPage;
import com.prodio.order.application.OrderRepository;
import com.prodio.order.domain.Order;
import com.prodio.order.domain.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.JoinType;
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
        return search(null, status, query, page, size);
    }

    @Override
    public OrderPage findAllByClientId(long clientId, OrderStatus status, String query, int page, int size) {
        return search(clientId, status, query, page, size);
    }

    private OrderPage search(Long clientId, OrderStatus status, String query, int page, int size) {
        Specification<OrderEntity> specification = Specification.unrestricted();
        if (clientId != null) {
            specification = specification.and((root, criteria, builder) ->
                    builder.equal(root.get("clientId"), clientId));
        }
        if (status != null) {
            specification = specification.and((root, criteria, builder) ->
                    builder.equal(root.get("status"), status));
        }
        if (query != null && !query.isBlank()) {
            String pattern = "%" + query.toLowerCase(Locale.ROOT) + "%";
            specification = specification.and((root, criteria, builder) -> {
                criteria.distinct(true);
                var items = root.join("items", JoinType.LEFT);
                return builder.or(
                        builder.like(builder.lower(root.get("clientNameSnapshot")), pattern),
                        builder.like(builder.lower(items.get("productNameSnapshot")), pattern));
            });
        }
        var result = orders.findAll(specification,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return new OrderPage(result.getContent().stream().map(OrderEntity::toDomain).toList(),
                page, size, result.getTotalElements());
    }
}

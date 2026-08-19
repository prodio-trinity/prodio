package com.prodio.order.infrastructure.persistence;

import com.prodio.order.domain.OrderItem;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_items")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class OrderItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;
    @Column(name = "product_id", nullable = false) private long productId;
    @Column(name = "product_name_snapshot", nullable = false) private String productNameSnapshot;
    @Column(name = "unit_price_snapshot", nullable = false) private long unitPriceSnapshot;
    @Column(nullable = false) private int quantity;
    @Column(name = "line_amount", nullable = false) private long lineAmount;

    private OrderItemEntity(OrderEntity order, OrderItem item) {
        this.order = order;
        productId = item.productId();
        productNameSnapshot = item.productNameSnapshot();
        unitPriceSnapshot = item.unitPriceSnapshot();
        quantity = item.quantity();
        lineAmount = item.lineAmount();
    }

    static OrderItemEntity from(OrderEntity order, OrderItem item) {
        return new OrderItemEntity(order, item);
    }

    OrderItem toDomain() {
        return new OrderItem(productId, productNameSnapshot, unitPriceSnapshot, quantity, lineAmount);
    }
}

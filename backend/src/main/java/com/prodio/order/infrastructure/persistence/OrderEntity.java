package com.prodio.order.infrastructure.persistence;

import com.prodio.order.domain.Order;
import com.prodio.order.domain.OrderStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Getter
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class OrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "client_id", nullable = false) private long clientId;
    @Column(name = "client_name_snapshot", nullable = false) private String clientNameSnapshot;
    @Column(name = "client_phone_snapshot") private String clientPhoneSnapshot;
    @Column(name = "product_id", nullable = false) private long productId;
    @Column(name = "product_name_snapshot", nullable = false) private String productNameSnapshot;
    @Column(name = "unit_price_snapshot", nullable = false) private long unitPriceSnapshot;
    @Column(nullable = false) private int quantity;
    @Column(name = "vat_included", nullable = false) private boolean vatIncluded;
    @Column(name = "total_amount", nullable = false) private long totalAmount;
    @Column(name = "due_date", nullable = false) private LocalDate dueDate;
    @Column(name = "delivery_address") private String deliveryAddress;
    @Column private String note;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false) private OrderStatus status;
    @Column(name = "payment_confirmed", nullable = false) private boolean paymentConfirmed;
    @Column(name = "created_by", nullable = false) private long createdBy;
    @Column(name = "created_at", nullable = false) private OffsetDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private OffsetDateTime updatedAt;

    private OrderEntity(Order order) { apply(order); }

    static OrderEntity from(Order order) {
        return new OrderEntity(order);
    }

    void apply(Order order) {
        clientId = order.clientId();
        clientNameSnapshot = order.clientNameSnapshot();
        clientPhoneSnapshot = order.clientPhoneSnapshot();
        productId = order.productId();
        productNameSnapshot = order.productNameSnapshot();
        unitPriceSnapshot = order.unitPriceSnapshot();
        quantity = order.quantity();
        vatIncluded = order.vatIncluded();
        totalAmount = order.totalAmount();
        dueDate = order.dueDate();
        deliveryAddress = order.deliveryAddress();
        note = order.note();
        status = order.status();
        paymentConfirmed = order.paymentConfirmed();
        createdBy = order.createdBy();
        createdAt = order.createdAt();
        updatedAt = order.updatedAt();
    }

    Order toDomain() {
        return Order.reconstitute(id, clientId, clientNameSnapshot, clientPhoneSnapshot,
                productId, productNameSnapshot, unitPriceSnapshot, quantity, vatIncluded,
                totalAmount, dueDate, deliveryAddress, note, status, paymentConfirmed,
                createdBy, createdAt, updatedAt);
    }
}

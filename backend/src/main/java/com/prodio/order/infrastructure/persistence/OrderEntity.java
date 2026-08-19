package com.prodio.order.infrastructure.persistence;

import com.prodio.order.domain.Order;
import com.prodio.order.domain.OrderStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

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
    @Column(name = "client_contact_snapshot") private String clientContactSnapshot;
    @Column(name = "order_name", length = 200) private String orderName;
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<OrderItemEntity> items = new ArrayList<>();
    @Column(name = "vat_included", nullable = false) private boolean vatIncluded;
    @Column(name = "total_amount", nullable = false) private long totalAmount;
    @Column(name = "delivery_address") private String deliveryAddress;
    @Column(name = "delivery_address_id") private Long deliveryAddressId;
    @Column(name = "delivery_name_snapshot", nullable = false) private String deliveryNameSnapshot;
    @Column(name = "delivery_recipient_name_snapshot", nullable = false) private String deliveryRecipientNameSnapshot;
    @Column(name = "delivery_recipient_phone_snapshot", nullable = false) private String deliveryRecipientPhoneSnapshot;
    @Column(name = "delivery_postal_code_snapshot", nullable = false) private String deliveryPostalCodeSnapshot;
    @Column(name = "delivery_address_detail_snapshot", nullable = false) private String deliveryAddressDetailSnapshot;
    @Column private String note;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false) private OrderStatus status;
    @Column(name = "cancellation_reason") private String cancellationReason;
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
        clientContactSnapshot = order.clientContactSnapshot();
        orderName = order.orderName();
        items.clear();
        items.addAll(order.items().stream().map(item -> OrderItemEntity.from(this, item)).toList());
        vatIncluded = order.vatIncluded();
        totalAmount = order.totalAmount();
        deliveryAddress = order.deliveryAddress();
        deliveryAddressId = order.delivery().addressId();
        deliveryNameSnapshot = order.delivery().name();
        deliveryRecipientNameSnapshot = order.delivery().recipientName();
        deliveryRecipientPhoneSnapshot = order.delivery().recipientPhone();
        deliveryPostalCodeSnapshot = order.delivery().postalCode();
        deliveryAddressDetailSnapshot = order.delivery().addressLine2();
        note = order.note();
        status = order.status();
        cancellationReason = order.cancellationReason();
        createdBy = order.createdBy();
        createdAt = order.createdAt();
        updatedAt = order.updatedAt();
    }

    Order toDomain() {
        return Order.reconstitute(id, clientId, clientNameSnapshot, clientContactSnapshot,
                orderName, items.stream().map(OrderItemEntity::toDomain).toList(), vatIncluded,
                totalAmount, new com.prodio.order.domain.DeliverySnapshot(
                        deliveryAddressId, deliveryNameSnapshot, deliveryRecipientNameSnapshot,
                        deliveryRecipientPhoneSnapshot, deliveryPostalCodeSnapshot,
                        deliveryAddress, deliveryAddressDetailSnapshot),
                note, status, cancellationReason,
                createdBy, createdAt, updatedAt);
    }
}

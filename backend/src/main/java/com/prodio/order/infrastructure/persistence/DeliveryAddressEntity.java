package com.prodio.order.infrastructure.persistence;

import com.prodio.order.domain.DeliveryAddress;
import com.prodio.order.domain.DeliverySnapshot;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "order_delivery_addresses")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class DeliveryAddressEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "client_id", nullable = false) private long clientId;
    @Column(nullable = false) private String name;
    @Column(name = "recipient_name", nullable = false) private String recipientName;
    @Column(name = "recipient_phone", nullable = false) private String recipientPhone;
    @Column(name = "postal_code", nullable = false) private String postalCode;
    @Column(name = "address_line1", nullable = false) private String addressLine1;
    @Column(name = "address_line2", nullable = false) private String addressLine2;
    @Column(name = "created_at", nullable = false) private OffsetDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private OffsetDateTime updatedAt;

    private DeliveryAddressEntity(DeliveryAddress address) { apply(address); }

    static DeliveryAddressEntity from(DeliveryAddress address) { return new DeliveryAddressEntity(address); }

    void apply(DeliveryAddress address) {
        clientId = address.clientId();
        DeliverySnapshot snapshot = address.snapshot();
        name = snapshot.name();
        recipientName = snapshot.recipientName();
        recipientPhone = snapshot.recipientPhone();
        postalCode = snapshot.postalCode();
        addressLine1 = snapshot.addressLine1();
        addressLine2 = snapshot.addressLine2();
        createdAt = address.createdAt();
        updatedAt = address.updatedAt();
    }

    DeliveryAddress toDomain() {
        return DeliveryAddress.reconstitute(id, clientId,
                new DeliverySnapshot(id, name, recipientName, recipientPhone,
                        postalCode, addressLine1, addressLine2), createdAt, updatedAt);
    }
}

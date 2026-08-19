package com.prodio.order.domain;

import java.time.OffsetDateTime;
import java.util.Objects;

public final class DeliveryAddress {
    private final long id;
    private final long clientId;
    private String name;
    private String recipientName;
    private String recipientPhone;
    private String postalCode;
    private String addressLine1;
    private String addressLine2;
    private final OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    private DeliveryAddress(long id, long clientId, DeliverySnapshot snapshot,
            OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        if (id < 0 || clientId <= 0) throw new IllegalArgumentException("배송지 식별자가 올바르지 않습니다.");
        this.id = id;
        this.clientId = clientId;
        apply(snapshot);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static DeliveryAddress create(long clientId, DeliverySnapshot snapshot, OffsetDateTime now) {
        return new DeliveryAddress(0, clientId, snapshot, now, now);
    }

    public static DeliveryAddress reconstitute(long id, long clientId, DeliverySnapshot snapshot,
            OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        return new DeliveryAddress(id, clientId, snapshot, createdAt, updatedAt);
    }

    public void update(DeliverySnapshot snapshot, OffsetDateTime now) {
        apply(snapshot);
        updatedAt = Objects.requireNonNull(now);
    }

    private void apply(DeliverySnapshot snapshot) {
        DeliverySnapshot value = Objects.requireNonNull(snapshot);
        name = value.name();
        recipientName = value.recipientName();
        recipientPhone = value.recipientPhone();
        postalCode = value.postalCode();
        addressLine1 = value.addressLine1();
        addressLine2 = value.addressLine2();
    }

    public DeliverySnapshot snapshot() {
        return new DeliverySnapshot(id == 0 ? null : id, name, recipientName,
                recipientPhone, postalCode, addressLine1, addressLine2);
    }

    public long id() { return id; }
    public long clientId() { return clientId; }
    public OffsetDateTime createdAt() { return createdAt; }
    public OffsetDateTime updatedAt() { return updatedAt; }
}

package com.prodio.order.application;

import com.prodio.order.domain.DeliverySnapshot;

public record DeliveryCommand(Long addressId, String name, String recipientName,
        String recipientPhone, String postalCode, String addressLine1, String addressLine2) {
    public DeliverySnapshot toSnapshot() {
        return new DeliverySnapshot(addressId, name, recipientName, recipientPhone,
                postalCode, addressLine1, addressLine2);
    }
}

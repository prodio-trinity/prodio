package com.prodio.order;

import com.prodio.order.domain.DeliverySnapshot;

public record OrderDeliveryEventData(String name, String recipientName,
        String recipientPhone, String postalCode, String addressLine1, String addressLine2) {
    static OrderDeliveryEventData from(DeliverySnapshot delivery) {
        return new OrderDeliveryEventData(delivery.name(), delivery.recipientName(),
                delivery.recipientPhone(), delivery.postalCode(), delivery.addressLine1(),
                delivery.addressLine2());
    }
}

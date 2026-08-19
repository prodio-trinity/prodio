package com.prodio.order.application;

import com.prodio.order.domain.DeliverySnapshot;

import java.util.List;

public record DeliveryContext(long clientId, String clientName,
        DeliverySnapshot headquarters, DeliverySnapshot recent,
        List<DeliverySnapshot> savedAddresses) {
}

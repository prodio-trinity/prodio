package com.prodio.order.application;

import com.prodio.order.domain.DeliveryAddress;

import java.util.List;
import java.util.Optional;

public interface DeliveryAddressRepository {
    DeliveryAddress save(DeliveryAddress address);
    Optional<DeliveryAddress> findById(long id);
    List<DeliveryAddress> findAllByClientId(long clientId);
    void delete(DeliveryAddress address);
}

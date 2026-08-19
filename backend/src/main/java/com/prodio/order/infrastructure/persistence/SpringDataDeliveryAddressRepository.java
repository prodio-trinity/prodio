package com.prodio.order.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface SpringDataDeliveryAddressRepository extends JpaRepository<DeliveryAddressEntity, Long> {
    List<DeliveryAddressEntity> findAllByClientIdOrderByUpdatedAtDesc(long clientId);
}

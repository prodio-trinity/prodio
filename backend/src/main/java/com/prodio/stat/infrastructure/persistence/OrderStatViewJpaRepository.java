package com.prodio.stat.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface OrderStatViewJpaRepository extends JpaRepository<OrderStatViewEntity, Long> {
    Optional<OrderStatViewEntity> findByOrderId(long orderId);
}

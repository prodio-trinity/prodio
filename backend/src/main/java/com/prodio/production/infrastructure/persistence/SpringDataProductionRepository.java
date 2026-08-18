package com.prodio.production.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataProductionRepository extends JpaRepository<ProductionRecordEntity, Long> {
    boolean existsByOrderId(Long orderId);
}

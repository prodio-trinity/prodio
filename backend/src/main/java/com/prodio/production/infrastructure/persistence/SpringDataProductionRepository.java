package com.prodio.production.infrastructure.persistence;

import com.prodio.production.application.ProductionStatus;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SpringDataProductionRepository extends JpaRepository<ProductionRecordEntity, Long> {
    boolean existsByOrderId(Long orderId);

    Optional<ProductionRecordEntity> findByOrderId(Long orderId);

    @Query("""
            SELECT e FROM ProductionRecordEntity e
            WHERE :status IS NULL OR e.productionStatus = :status
            """)
    Page<ProductionRecordEntity> search(@Param("status") ProductionStatus status, Pageable pageable);
}

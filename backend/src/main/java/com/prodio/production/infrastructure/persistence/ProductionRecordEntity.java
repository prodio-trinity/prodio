package com.prodio.production.infrastructure.persistence;

import com.prodio.production.application.ProductionStatus;
import com.prodio.production.domain.ProductionRecord;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "production_records")
class ProductionRecordEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProductionStatus productionStatus;

    @Column(name = "memo")
    private String memo;

    @Column(name = "phone")
    private String phone;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    static ProductionRecordEntity from(ProductionRecord domain){
        ProductionRecordEntity entity = new ProductionRecordEntity();
        entity.id = domain.id();
        entity.orderId = domain.orderId();
        entity.productionStatus = domain.status();
        entity.memo = domain.memo();
        entity.phone = domain.phone();
        entity.startedAt = domain.startedAt();
        entity.shippedAt = domain.shippedAt();
        entity.completedAt = domain.completedAt();
        return entity;
    }
    ProductionRecord toDomain() {
        return new ProductionRecord(
                id,
                orderId,
                productionStatus,
                memo,
                phone,
                startedAt,
                shippedAt,
                completedAt
        );
    }
}

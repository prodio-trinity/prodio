package com.prodio.production.domain;

import com.prodio.production.application.ProductionStatus;
import com.prodio.production.exception.ProductionErrorCode;
import com.prodio.production.exception.ProductionException;
import java.time.LocalDateTime;

public record ProductionRecord(
        Long id,
        Long orderId,
        Long clientId,
        ProductionStatus status,
        String memo,
        String phone,
        LocalDateTime startedAt,
        LocalDateTime shippedAt,
        LocalDateTime completedAt
) {
    public static ProductionRecord create(Long orderId, Long clientId, String phone) {
        return new ProductionRecord(
                null,
                orderId,
                clientId,
                ProductionStatus.IN_PRODUCTION,
                null,
                phone,
                LocalDateTime.now(),
                null,
                null
        );
    }

    public ProductionRecord markShipped() {
        if (status != ProductionStatus.IN_PRODUCTION) {
            throw new ProductionException(ProductionErrorCode.INVALID_PRODUCTION_STATUS);
        }
        return new ProductionRecord(id, orderId, clientId, ProductionStatus.IN_DELIVERY, memo, phone,
                startedAt, LocalDateTime.now(), completedAt);
    }
    public ProductionRecord markCompleted() {
        if (status != ProductionStatus.IN_DELIVERY) {
            throw new ProductionException(ProductionErrorCode.INVALID_PRODUCTION_STATUS);
        }
        return new ProductionRecord(id, orderId, clientId, ProductionStatus.COMPLETED, memo, phone,
                startedAt, shippedAt, LocalDateTime.now());
    }
}

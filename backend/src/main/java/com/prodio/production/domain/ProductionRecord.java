package com.prodio.production.domain;

import com.prodio.production.application.ProductionStatus;
import java.time.LocalDateTime;

public record ProductionRecord(
        Long id,
        Long orderId,
        ProductionStatus status,
        String memo,
        String phone,
        LocalDateTime startedAt,
        LocalDateTime shippedAt,
        LocalDateTime completedAt
) {
    public static ProductionRecord create(Long orderId, String phone) {
        return new ProductionRecord(
                null,
                orderId,
                ProductionStatus.IN_PRODUCTION,
                null,
                phone,
                LocalDateTime.now(),
                null,
                null
        );
    }

}

package com.prodio.production.presentation;

import com.prodio.production.domain.ProductionRecord;
import java.time.LocalDateTime;

record ProductionResponse(String id, String orderId, String status, String memo,
        String phone, LocalDateTime startedAt, LocalDateTime shippedAt, LocalDateTime completedAt) {
    static ProductionResponse from(ProductionRecord record) {
        return new ProductionResponse(String.valueOf(record.id()), String.valueOf(record.orderId()),
                record.status().name(), record.memo(), record.phone(),
                record.startedAt(), record.shippedAt(), record.completedAt());
    }
}

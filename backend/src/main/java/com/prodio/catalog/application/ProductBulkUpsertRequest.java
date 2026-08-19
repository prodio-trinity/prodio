package com.prodio.catalog.application;

import java.math.BigDecimal;

public record ProductBulkUpsertRequest(
        Long id,
        String productName,
        String categoryCode,
        BigDecimal unitPrice,
        String unit,
        String description,
        String memo,
        boolean isActive
) {
}
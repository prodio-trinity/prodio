package com.prodio.catalog.application;

import java.math.BigDecimal;

public record ProductBulkUpsertRequest(
        Long id,
        String productName,
        String categoryCode,
        BigDecimal unitPrice,
        String unit,
        String memo,
        Boolean isActive
) {
}
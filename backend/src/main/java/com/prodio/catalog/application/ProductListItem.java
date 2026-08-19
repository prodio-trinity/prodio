package com.prodio.catalog.application;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductListItem(
        long id,
        String productCode,
        String productName,
        Long subCategoryId,
        String subCategoryName,
        String topCategory,
        String topCategoryDisplayName,
        BigDecimal unitPrice,
        String unit,
        String description,
        String memo,
        boolean active,
        Instant createdAt
) {
}
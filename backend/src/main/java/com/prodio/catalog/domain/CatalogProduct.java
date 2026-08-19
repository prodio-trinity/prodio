package com.prodio.catalog.domain;

import java.math.BigDecimal;

public record CatalogProduct(
        Long id,
        String productCode,
        String productName,
        Long subCategoryId,
        BigDecimal unitPrice,
        Unit unit,
        String description,
        String memo,
        boolean active
) {
    public CatalogProduct {
        if (productName == null || productName.isBlank()) {
            throw new IllegalArgumentException("productName은 비어 있을 수 없습니다.");
        }
        if (unitPrice == null || unitPrice.signum() < 0) {
            throw new IllegalArgumentException("unitPrice는 0 이상이어야 합니다.");
        }
    }

    public static CatalogProduct register(String productCode, String productName, Long subCategoryId,
            BigDecimal unitPrice, Unit unit, String description, String memo) {
        return new CatalogProduct(
                null,
                productCode,
                productName,
                subCategoryId,
                unitPrice,
                unit,
                description,
                memo,
                true
        );
    }

    /** id/productCode는 그대로 두고 편집 가능한 필드만 업데이트. */
    public CatalogProduct update(String productName, Long subCategoryId, BigDecimal unitPrice, Unit unit,
            String description, String memo, boolean active) {
        return new CatalogProduct(
                id,
                productCode,
                productName,
                subCategoryId,
                unitPrice,
                unit,
                description,
                memo,
                active
        );
    }
}
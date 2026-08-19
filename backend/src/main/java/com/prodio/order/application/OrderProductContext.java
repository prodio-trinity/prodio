package com.prodio.order.application;

public record OrderProductContext(long productId, String productCode, String name,
        long subCategoryId, String unit, String description, String memo, long unitPrice) {
}

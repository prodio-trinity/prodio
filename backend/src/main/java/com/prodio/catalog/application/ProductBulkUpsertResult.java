package com.prodio.catalog.application;

public record ProductBulkUpsertResult(int index, boolean success, Long id, String productCode, String reason) {
    static ProductBulkUpsertResult success(int index, Long id, String productCode) {
        return new ProductBulkUpsertResult(index, true, id, productCode, null);
    }

    static ProductBulkUpsertResult failure(int index, String reason) {
        return new ProductBulkUpsertResult(index, false, null, null, reason);
    }
}
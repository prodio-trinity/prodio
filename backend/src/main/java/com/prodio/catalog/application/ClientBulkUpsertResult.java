package com.prodio.catalog.application;

public record ClientBulkUpsertResult(int index, boolean success, Long id, String clientCode, String reason) {
    static ClientBulkUpsertResult success(int index, Long id, String clientCode) {
        return new ClientBulkUpsertResult(index, true, id, clientCode, null);
    }

    static ClientBulkUpsertResult failure(int index, String reason) {
        return new ClientBulkUpsertResult(index, false, null, null, reason);
    }
}
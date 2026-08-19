package com.prodio.catalog.application;

public record ClientBulkUpsertRequest(
        Long id,
        String companyName,
        String ceoName,
        String businessRegNo,
        String phone,
        String address,
        String managerName,
        String memo,
        boolean isActive
) {
}
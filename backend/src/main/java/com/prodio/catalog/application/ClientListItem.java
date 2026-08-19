package com.prodio.catalog.application;

import java.time.Instant;

public record ClientListItem(
        long id,
        String clientCode,
        String companyName,
        String ceoName,
        String businessRegNo,
        String phone,
        String address,
        String managerName,
        String memo,
        boolean active,
        boolean linkedToAccount,
        Instant createdAt
) {
}
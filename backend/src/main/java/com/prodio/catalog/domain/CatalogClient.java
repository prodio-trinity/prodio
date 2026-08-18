package com.prodio.catalog.domain;

public record CatalogClient(
        Long id,
        String clientCode,
        String companyName,
        String ceoName,
        String businessRegNo,
        String phone,
        String address,
        String managerName,
        Long userId,
        String memo,
        boolean active
) {
    public CatalogClient {
        if (companyName == null || companyName.isBlank()) {
            throw new IllegalArgumentException("companyName은 비어 있을 수 없습니다.");
        }
    }

    public static CatalogClient register(String clientCode, String companyName, String ceoName,
            String businessRegNo, String phone, String address, String managerName,
            Long userId, String memo) {
        return new CatalogClient(
                null,
                clientCode,
                companyName,
                ceoName,
                businessRegNo,
                phone,
                address,
                managerName,
                userId,
                memo,
                true
        );
    }
}
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
            throw new IllegalArgumentException("회사명은 비어 있을 수 없습니다.");
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

    /** id/clientCode/userId는 그대로 두고 그리드에서 편집 가능한 필드만 업데이트. */
    public CatalogClient update(String companyName, String ceoName, String businessRegNo,
            String phone, String address, String managerName, String memo, boolean active) {
        return new CatalogClient(
                id,
                clientCode,
                companyName,
                ceoName,
                businessRegNo,
                phone,
                address,
                managerName,
                userId,
                memo,
                active
        );
    }

    /** 거래처 등록 신청 승인 — userId만 연결 */
    public CatalogClient linkUser(Long userId) {
        return new CatalogClient(
                id,
                clientCode,
                companyName,
                ceoName,
                businessRegNo,
                phone,
                address,
                managerName,
                userId,
                memo,
                active
        );
    }
}
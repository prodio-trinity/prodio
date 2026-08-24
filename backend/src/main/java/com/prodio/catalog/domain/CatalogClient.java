package com.prodio.catalog.domain;

import java.util.regex.Pattern;

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
    private static final Pattern BUSINESS_REG_NO_PATTERN = Pattern.compile("^\\d{3}-\\d{2}-\\d{5}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{2,3}-\\d{3,4}-\\d{4}$");

    public CatalogClient {
        if (companyName == null || companyName.isBlank()) {
            throw new IllegalArgumentException("회사명은 비어 있을 수 없습니다.");
        }
        if (businessRegNo != null && !businessRegNo.isBlank()
                && !BUSINESS_REG_NO_PATTERN.matcher(businessRegNo).matches()) {
            throw new IllegalArgumentException("사업자등록번호 형식이 올바르지 않습니다. 000-00-00000 형식으로 입력해주세요.");
        }
        if (phone != null && !phone.isBlank() && !PHONE_PATTERN.matcher(phone).matches()) {
            throw new IllegalArgumentException("전화번호 형식이 올바르지 않습니다. 하이픈(-)을 포함하여 입력해주세요.");
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
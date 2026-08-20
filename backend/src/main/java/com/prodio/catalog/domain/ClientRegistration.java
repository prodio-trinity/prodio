package com.prodio.catalog.domain;

import java.time.Instant;

public record ClientRegistration(
        Long id,
        long userId,
        String companyName,
        String ceoName,
        String businessRegNo,
        String phone,
        String address,
        String managerName,
        RegistrationStatus status,
        String rejectReason,
        Instant createdAt,
        Instant reviewedAt
) {
    public ClientRegistration {
        if (companyName == null || companyName.isBlank()) {
            throw new IllegalArgumentException("companyName은 비어 있을 수 없습니다.");
        }
        if (businessRegNo == null || businessRegNo.isBlank()) {
            throw new IllegalArgumentException("businessRegNo는 비어 있을 수 없습니다.");
        }
    }

    /** 거래처 등록 신청 */
    public static ClientRegistration submit(long userId, String companyName, String ceoName,
            String businessRegNo, String phone, String address, String managerName) {
        return new ClientRegistration(null, userId, companyName, ceoName, businessRegNo,
                phone, address, managerName, RegistrationStatus.PENDING, null, Instant.now(), null);
    }

    /** 재신청 — 기존 행을 덮어쓰고 상태를 PENDING으로 리셋 */
    public ClientRegistration resubmit(String companyName, String ceoName, String businessRegNo,
            String phone, String address, String managerName) {
        return new ClientRegistration(id, userId, companyName, ceoName, businessRegNo,
                phone, address, managerName, RegistrationStatus.PENDING, null, createdAt, null);
    }

    public ClientRegistration approve() {
        return new ClientRegistration(id, userId, companyName, ceoName, businessRegNo,
                phone, address, managerName, RegistrationStatus.APPROVED, null, createdAt, Instant.now());
    }

    public ClientRegistration reject(String reason) {
        return new ClientRegistration(id, userId, companyName, ceoName, businessRegNo,
                phone, address, managerName, RegistrationStatus.REJECTED, reason, createdAt, Instant.now());
    }
}
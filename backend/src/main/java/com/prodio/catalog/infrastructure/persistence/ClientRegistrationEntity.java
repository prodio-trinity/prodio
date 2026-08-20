package com.prodio.catalog.infrastructure.persistence;

import com.prodio.catalog.domain.ClientRegistration;
import com.prodio.catalog.domain.RegistrationStatus;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.Instant;

@Entity
@Getter
@Table(name = "catalog_client_registration_requests")
class ClientRegistrationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private long userId;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "ceo_name")
    private String ceoName;

    @Column(name = "business_reg_no")
    private String businessRegNo;

    @Column
    private String phone;

    @Column
    private String address;

    @Column(name = "manager_name")
    private String managerName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RegistrationStatus status;

    @Column(name = "reject_reason")
    private String rejectReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    static ClientRegistrationEntity from(ClientRegistration domain) {
        ClientRegistrationEntity entity = new ClientRegistrationEntity();
        entity.userId = domain.userId();
        entity.companyName = domain.companyName();
        entity.ceoName = domain.ceoName();
        entity.businessRegNo = domain.businessRegNo();
        entity.phone = domain.phone();
        entity.address = domain.address();
        entity.managerName = domain.managerName();
        entity.status = domain.status();
        entity.rejectReason = domain.rejectReason();
        entity.createdAt = domain.createdAt();
        entity.reviewedAt = domain.reviewedAt();
        return entity;
    }
    
    void update(ClientRegistration domain) {
        this.companyName = domain.companyName();
        this.ceoName = domain.ceoName();
        this.businessRegNo = domain.businessRegNo();
        this.phone = domain.phone();
        this.address = domain.address();
        this.managerName = domain.managerName();
        this.status = domain.status();
        this.rejectReason = domain.rejectReason();
        this.reviewedAt = domain.reviewedAt();
    }

    ClientRegistration toDomain() {
        return new ClientRegistration(id, userId, companyName, ceoName, businessRegNo, phone, address,
                managerName, status, rejectReason, createdAt, reviewedAt);
    }
}
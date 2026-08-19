package com.prodio.catalog.infrastructure.persistence;

import com.prodio.catalog.domain.CatalogClient;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.Instant;

@Entity
@Getter
@Table(name = "catalog_clients")
class CatalogClientEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_code", nullable = false, unique = true)
    private String clientCode;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "ceo_name")
    private String ceoName;

    @Column(name = "business_reg_no", unique = true)
    private String businessRegNo;

    @Column
    private String phone;

    @Column
    private String address;

    @Column(name = "manager_name")
    private String managerName;

    @Column(name = "user_id", unique = true)
    private Long userId;

    @Column
    private String memo;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    static CatalogClientEntity from(CatalogClient domain) {
        CatalogClientEntity entity = new CatalogClientEntity();
        entity.clientCode = domain.clientCode();
        entity.companyName = domain.companyName();
        entity.ceoName = domain.ceoName();
        entity.businessRegNo = domain.businessRegNo();
        entity.phone = domain.phone();
        entity.address = domain.address();
        entity.managerName = domain.managerName();
        entity.userId = domain.userId();
        entity.memo = domain.memo();
        entity.active = domain.active();
        entity.createdAt = Instant.now();
        entity.updatedAt = Instant.now();
        return entity;
    }

    void assignClientCode(String clientCode) {
        this.clientCode = clientCode;
    }

    /** 그리드에서 편집 가능한 필드만 갱신 */
    void update(CatalogClient domain) {
        this.companyName = domain.companyName();
        this.ceoName = domain.ceoName();
        this.businessRegNo = domain.businessRegNo();
        this.phone = domain.phone();
        this.address = domain.address();
        this.managerName = domain.managerName();
        this.memo = domain.memo();
        this.active = domain.active();
        this.updatedAt = Instant.now();
    }

    CatalogClient toDomain() {
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

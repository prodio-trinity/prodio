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
        return entity;
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

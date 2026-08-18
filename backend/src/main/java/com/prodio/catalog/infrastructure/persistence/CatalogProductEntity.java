package com.prodio.catalog.infrastructure.persistence;

import com.prodio.catalog.domain.CatalogProduct;
import com.prodio.catalog.domain.Unit;
import jakarta.persistence.*;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Getter
@Table(name = "catalog_products")
class CatalogProductEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_code", nullable = false, unique = true)
    private String productCode;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "sub_category_id", nullable = false)
    private Long subCategoryId;

    @Column(name = "unit_price", nullable = false, precision = 14, scale = 2)
    private BigDecimal unitPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Unit unit;

    @Column
    private String description;

    @Column
    private String memo;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    static CatalogProductEntity from(CatalogProduct domain) {
        CatalogProductEntity entity = new CatalogProductEntity();
        entity.productCode = domain.productCode();
        entity.productName = domain.productName();
        entity.subCategoryId = domain.subCategoryId();
        entity.unitPrice = domain.unitPrice();
        entity.unit = domain.unit();
        entity.description = domain.description();
        entity.memo = domain.memo();
        entity.active = domain.active();
        return entity;
    }

    CatalogProduct toDomain() {
        return new CatalogProduct(
                id,
                productCode,
                productName,
                subCategoryId,
                unitPrice,
                unit,
                description,
                memo,
                active
        );
    }
}
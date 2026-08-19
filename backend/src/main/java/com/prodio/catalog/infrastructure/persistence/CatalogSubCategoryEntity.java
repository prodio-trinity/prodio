package com.prodio.catalog.infrastructure.persistence;

import com.prodio.catalog.domain.CatalogSubCategory;
import com.prodio.catalog.domain.TopCategory;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.Instant;

@Entity
@Getter
@Table(name = "catalog_sub_categories")
class CatalogSubCategoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sub_category_code", nullable = false, unique = true)
    private String subCategoryCode;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "top_category", nullable = false)
    private TopCategory topCategory;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    static CatalogSubCategoryEntity from(CatalogSubCategory domain) {
        CatalogSubCategoryEntity entity = new CatalogSubCategoryEntity();
        entity.subCategoryCode = domain.subCategoryCode();
        entity.name = domain.name();
        entity.topCategory = domain.topCategory();
        entity.active = domain.active();
        entity.createdAt = Instant.now();
        entity.updatedAt = Instant.now();
        return entity;
    }

    CatalogSubCategory toDomain() {
        return new CatalogSubCategory(id, subCategoryCode, name, topCategory, active);
    }
}
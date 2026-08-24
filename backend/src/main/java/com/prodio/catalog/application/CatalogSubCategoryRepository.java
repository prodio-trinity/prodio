package com.prodio.catalog.application;

import com.prodio.catalog.domain.CatalogSubCategory;

import java.util.List;
import java.util.Optional;

public interface CatalogSubCategoryRepository {
    Optional<Long> findIdByCode(String subCategoryCode);

    Optional<CatalogSubCategory> findById(Long id);

    List<CatalogSubCategory> findAll(Boolean isActive);

    CatalogSubCategory save(CatalogSubCategory subCategory);

    Optional<CatalogSubCategory> update(Long id, String name, boolean active);
}
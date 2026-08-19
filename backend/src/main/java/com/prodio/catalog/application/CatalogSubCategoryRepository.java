package com.prodio.catalog.application;

import com.prodio.catalog.domain.CatalogSubCategory;

import java.util.List;
import java.util.Optional;

public interface CatalogSubCategoryRepository {
    Optional<Long> findIdByCode(String subCategoryCode);

    List<CatalogSubCategory> findAll(Boolean isActive);

    CatalogSubCategory save(CatalogSubCategory subCategory);
}
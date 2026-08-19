package com.prodio.catalog.application;

import java.util.Optional;

public interface CatalogSubCategoryQueryRepository {
    Optional<Long> findIdByCode(String subCategoryCode);
}
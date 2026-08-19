package com.prodio.catalog.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

interface SpringDataCatalogSubCategoryRepository extends JpaRepository<CatalogSubCategoryEntity, Long> {
    Optional<CatalogSubCategoryEntity> findBySubCategoryCode(String subCategoryCode);

    List<CatalogSubCategoryEntity> findByActiveTrue();
}
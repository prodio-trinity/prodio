package com.prodio.catalog.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataCatalogSubCategoryRepository extends JpaRepository<CatalogSubCategoryEntity, Long> {
}
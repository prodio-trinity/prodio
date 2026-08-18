package com.prodio.catalog.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataCatalogProductRepository extends JpaRepository<CatalogProductEntity, Long> {
}

package com.prodio.catalog.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataCatalogProductRepository extends JpaRepository<CatalogProductEntity, Long> {
    List<CatalogProductEntity> findAllByActiveTrueOrderByProductNameAsc();
}

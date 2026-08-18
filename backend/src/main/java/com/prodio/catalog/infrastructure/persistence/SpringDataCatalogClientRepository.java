package com.prodio.catalog.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataCatalogClientRepository extends JpaRepository<CatalogClientEntity, Long> {
    Optional<CatalogClientEntity> findByUserId(Long userId);
}

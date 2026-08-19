package com.prodio.catalog.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SpringDataCatalogClientRepository extends JpaRepository<CatalogClientEntity, Long> {
    Optional<CatalogClientEntity> findByUserId(Long userId);

    @Query("""
        SELECT c FROM CatalogClientEntity c
        WHERE (:isActive IS NULL OR c.active = :isActive)
          AND (:keyword IS NULL
               OR LOWER(c.companyName) LIKE CONCAT('%', LOWER(:keyword), '%')
               OR LOWER(COALESCE(c.ceoName, '')) LIKE CONCAT('%', LOWER(:keyword), '%')
               OR LOWER(COALESCE(c.phone, '')) LIKE CONCAT('%', LOWER(:keyword), '%')
               OR LOWER(COALESCE(c.address, '')) LIKE CONCAT('%', LOWER(:keyword), '%')
               OR LOWER(COALESCE(c.managerName, '')) LIKE CONCAT('%', LOWER(:keyword), '%'))
        """)
    Page<CatalogClientEntity> findClients(
            @Param("keyword") String keyword,
            @Param("isActive") Boolean isActive,
            Pageable pageable);

    @Query("""
        SELECT c FROM CatalogClientEntity c
        WHERE c.active = true
          AND (:keyword IS NULL
               OR LOWER(c.companyName) LIKE CONCAT('%', LOWER(:keyword), '%')
               OR LOWER(COALESCE(c.ceoName, '')) LIKE CONCAT('%', LOWER(:keyword), '%')
               OR LOWER(COALESCE(c.phone, '')) LIKE CONCAT('%', LOWER(:keyword), '%')
               OR LOWER(COALESCE(c.address, '')) LIKE CONCAT('%', LOWER(:keyword), '%')
               OR LOWER(COALESCE(c.managerName, '')) LIKE CONCAT('%', LOWER(:keyword), '%'))
        ORDER BY c.companyName
        """)
    List<CatalogClientEntity> findActiveClients(@Param("keyword") String keyword, Pageable pageable);

}
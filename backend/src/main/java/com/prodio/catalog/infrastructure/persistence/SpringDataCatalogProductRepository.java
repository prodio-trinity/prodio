package com.prodio.catalog.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SpringDataCatalogProductRepository extends JpaRepository<CatalogProductEntity, Long> {
    List<CatalogProductEntity> findAllByActiveTrueOrderByProductNameAsc();

    @Query("""
        SELECT p FROM CatalogProductEntity p
        WHERE (:isActive IS NULL OR p.active = :isActive)
          AND (:categoryId IS NULL OR p.subCategoryId = :categoryId)
          AND (:keyword IS NULL
               OR LOWER(p.productName) LIKE CONCAT('%', LOWER(:keyword), '%') ESCAPE '\\'
               OR LOWER(p.productCode) LIKE CONCAT('%', LOWER(:keyword), '%') ESCAPE '\\')
        """)
    Page<CatalogProductEntity> findProducts(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("isActive") Boolean isActive,
            Pageable pageable);

    @Query(value = "SELECT nextval(:seqName)", nativeQuery = true)
    Long nextSequenceValue(@Param("seqName") String seqName);
}
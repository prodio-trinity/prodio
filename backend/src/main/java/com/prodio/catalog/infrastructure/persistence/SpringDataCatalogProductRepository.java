package com.prodio.catalog.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SpringDataCatalogProductRepository extends JpaRepository<CatalogProductEntity, Long> {
    List<CatalogProductEntity> findAllByActiveTrueOrderByProductNameAsc();

    // :keyword를 CAST(... AS string)로 감싸는 이유:
    // null을 바인딩할 때 타입을 못 정하면 pgjdbc가 bytea로 잘못 추론해서 LOWER(bytea) 함수 에러가 남
    @Query("""
        SELECT p FROM CatalogProductEntity p
        WHERE (:isActive IS NULL OR p.active = :isActive)
          AND (:categoryId IS NULL OR p.subCategoryId = :categoryId)
          AND (:keyword IS NULL
               OR LOWER(p.productName) LIKE CONCAT('%', LOWER(CAST(:keyword AS string)), '%') ESCAPE '\\'
               OR LOWER(p.productCode) LIKE CONCAT('%', LOWER(CAST(:keyword AS string)), '%') ESCAPE '\\')
        """)
    Page<CatalogProductEntity> findProducts(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("isActive") Boolean isActive,
            Pageable pageable);

    @Query(value = "SELECT nextval(:seqName)", nativeQuery = true)
    Long nextSequenceValue(@Param("seqName") String seqName);
}
package com.prodio.catalog.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SpringDataCatalogClientRepository extends JpaRepository<CatalogClientEntity, Long> {

    String KEYWORD_FILTER = """
            (:isActive IS NULL OR c.active = :isActive)
              AND (:keyword IS NULL
                   OR LOWER(c.companyName) LIKE CONCAT('%', LOWER(:keyword), '%') ESCAPE '\\'
                   OR LOWER(COALESCE(c.ceoName, '')) LIKE CONCAT('%', LOWER(:keyword), '%') ESCAPE '\\'
                   OR LOWER(COALESCE(c.phone, '')) LIKE CONCAT('%', LOWER(:keyword), '%') ESCAPE '\\'
                   OR LOWER(COALESCE(c.address, '')) LIKE CONCAT('%', LOWER(:keyword), '%') ESCAPE '\\'
                   OR LOWER(COALESCE(c.managerName, '')) LIKE CONCAT('%', LOWER(:keyword), '%') ESCAPE '\\')
            """;

    Optional<CatalogClientEntity> findByUserId(Long userId);
    
    // 목록 화면 — Page (총 개수 필요, COUNT 쿼리 발생해도 됨)
    @Query("SELECT c FROM CatalogClientEntity c WHERE " + KEYWORD_FILTER)
    Page<CatalogClientEntity> findClients(
            @Param("keyword") String keyword,
            @Param("isActive") Boolean isActive,
            Pageable pageable);
    
    @Query("SELECT c FROM CatalogClientEntity c WHERE " + KEYWORD_FILTER)
    List<CatalogClientEntity> findClientsForAutocomplete(
            @Param("keyword") String keyword,
            @Param("isActive") Boolean isActive,
            Pageable pageable);

    Optional<CatalogClientEntity> findByBusinessRegNo(String businessRegNo);

    @Query(value = "SELECT nextval('catalog_client_code_seq')", nativeQuery = true)
    Long nextClientCodeSequence();

}
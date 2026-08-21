package com.prodio.catalog.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SpringDataCatalogClientRepository extends JpaRepository<CatalogClientEntity, Long> {

    // :keyword를 CAST(... AS string)로 감싸는 이유: 
    // null을 바인딩할 때 타입을 못 정하면 pgjdbc가 bytea로 잘못 추론해서 LOWER(bytea) 함수 에러가 남
    String KEYWORD_FILTER = """
            (:isActive IS NULL OR c.active = :isActive)
              AND (:keyword IS NULL
                   OR LOWER(c.companyName) LIKE CONCAT('%', LOWER(CAST(:keyword AS string)), '%') ESCAPE '\\'
                   OR LOWER(COALESCE(c.ceoName, '')) LIKE CONCAT('%', LOWER(CAST(:keyword AS string)), '%') ESCAPE '\\'
                   OR LOWER(COALESCE(c.phone, '')) LIKE CONCAT('%', LOWER(CAST(:keyword AS string)), '%') ESCAPE '\\'
                   OR LOWER(COALESCE(c.address, '')) LIKE CONCAT('%', LOWER(CAST(:keyword AS string)), '%') ESCAPE '\\'
                   OR LOWER(COALESCE(c.managerName, '')) LIKE CONCAT('%', LOWER(CAST(:keyword AS string)), '%') ESCAPE '\\')
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

    @Query("SELECT c FROM CatalogClientEntity c WHERE " + KEYWORD_FILTER)
    List<CatalogClientEntity> findClientsForExport(
            @Param("keyword") String keyword,
            @Param("isActive") Boolean isActive,
            Sort sort);

    Optional<CatalogClientEntity> findByBusinessRegNo(String businessRegNo);

    @Query("SELECT c.businessRegNo AS businessRegNo, c.id AS id "
            + "FROM CatalogClientEntity c "
            + "WHERE c.businessRegNo IN :businessRegNos")
    List<BusinessRegNoIdView> findIdsByBusinessRegNoIn(@Param("businessRegNos") Collection<String> businessRegNos);

    interface BusinessRegNoIdView {
        String getBusinessRegNo();
        Long getId();
    }

    @Query(value = "SELECT nextval('catalog_client_code_seq')", nativeQuery = true)
    Long nextClientCodeSequence();

}
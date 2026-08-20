package com.prodio.catalog.application;

import com.prodio.catalog.domain.CatalogClient;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface CatalogClientRepository {
    Optional<CatalogClient> findById(Long id);

    Optional<CatalogClient> findByBusinessRegNo(String businessRegNo);

    /** businessRegNo -> id. 엑셀 일괄 업로드 사 N+1 방지 위함. */
    Map<String, Long> findIdsByBusinessRegNoIn(Collection<String> businessRegNos);

    CatalogClient save(CatalogClient client);
}
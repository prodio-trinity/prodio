package com.prodio.catalog.application;

import com.prodio.catalog.domain.CatalogClient;

import java.util.Optional;

public interface CatalogClientRepository {
    Optional<CatalogClient> findById(Long id);

    Optional<CatalogClient> findByBusinessRegNo(String businessRegNo);

    CatalogClient save(CatalogClient client);
}
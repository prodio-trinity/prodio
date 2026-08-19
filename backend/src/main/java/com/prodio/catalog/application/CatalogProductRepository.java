package com.prodio.catalog.application;

import com.prodio.catalog.domain.CatalogProduct;

import java.util.Optional;

public interface CatalogProductRepository {
    Optional<CatalogProduct> findById(Long id);

    CatalogProduct save(CatalogProduct product);
}
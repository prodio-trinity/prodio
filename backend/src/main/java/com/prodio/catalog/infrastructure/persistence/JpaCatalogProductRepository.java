package com.prodio.catalog.infrastructure.persistence;

import com.prodio.catalog.application.CatalogProductRepository;
import com.prodio.catalog.domain.CatalogProduct;
import com.prodio.catalog.exception.CatalogErrorCode;
import com.prodio.catalog.exception.CatalogException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
class JpaCatalogProductRepository implements CatalogProductRepository {
    private final SpringDataCatalogProductRepository springDataCatalogProductRepository;
    private final ProductCodeGenerator productCodeGenerator;

    @Override
    public Optional<CatalogProduct> findById(Long id) {
        return springDataCatalogProductRepository.findById(id).map(CatalogProductEntity::toDomain);
    }

    @Override
    public CatalogProduct save(CatalogProduct product) {
        CatalogProductEntity entity = (product.id() == null) ? insertEntity(product) : updateEntity(product);
        CatalogProductEntity saved = springDataCatalogProductRepository.save(entity);
        springDataCatalogProductRepository.flush();
        return saved.toDomain();
    }

    private CatalogProductEntity insertEntity(CatalogProduct product) {
        CatalogProductEntity entity = CatalogProductEntity.from(product);
        entity.assignProductCode(productCodeGenerator.next(product.subCategoryId()));
        return entity;
    }

    private CatalogProductEntity updateEntity(CatalogProduct product) {
        CatalogProductEntity entity = springDataCatalogProductRepository.findById(product.id())
                .orElseThrow(() -> new CatalogException(CatalogErrorCode.PRODUCT_NOT_FOUND));
        entity.update(product);
        return entity;
    }
}
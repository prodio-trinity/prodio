package com.prodio.catalog.application;

import com.prodio.catalog.domain.CatalogProduct;
import com.prodio.catalog.domain.Unit;
import com.prodio.catalog.exception.CatalogErrorCode;
import com.prodio.catalog.exception.CatalogException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class CatalogProductRowUpsertService {
    private final CatalogProductRepository productRepository;
    private final CatalogSubCategoryRepository subCategoryRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CatalogProduct upsertOne(ProductBulkUpsertRequest req) {
        Long subCategoryId = resolveSubCategoryId(req.categoryCode());
        CatalogProduct product = (req.id() == null)
                ? buildNewProduct(req, subCategoryId)
                : buildUpdatedProduct(req, subCategoryId);
        return productRepository.save(product);
    }

    private Long resolveSubCategoryId(String categoryCode) {
        return subCategoryRepository.findIdByCode(categoryCode)
                .orElseThrow(() -> new CatalogException(CatalogErrorCode.CATEGORY_NOT_FOUND));
    }

    private CatalogProduct buildNewProduct(ProductBulkUpsertRequest req, Long subCategoryId) {
        return new CatalogProduct(null, null, req.productName(), subCategoryId,
                req.unitPrice(), Unit.from(req.unit()), req.description(), req.memo(), req.isActive());
    }

    private CatalogProduct buildUpdatedProduct(ProductBulkUpsertRequest req, Long subCategoryId) {
        CatalogProduct existing = productRepository.findById(req.id())
                .orElseThrow(() -> new CatalogException(CatalogErrorCode.PRODUCT_NOT_FOUND));
        return existing.update(req.productName(), subCategoryId, req.unitPrice(), Unit.from(req.unit()),
                req.description(), req.memo(), req.isActive());
    }
}
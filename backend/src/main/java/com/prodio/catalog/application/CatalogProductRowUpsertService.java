package com.prodio.catalog.application;

import com.prodio.catalog.domain.CatalogProduct;
import com.prodio.catalog.domain.CatalogSubCategory;
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
        CatalogSubCategory category = resolveCategory(req.categoryCode());
        CatalogProduct product = (req.id() == null)
                ? buildNewProduct(req, category)
                : buildUpdatedProduct(req, category);
        return productRepository.save(product);
    }

    private CatalogSubCategory resolveCategory(String categoryCode) {
        Long id = subCategoryRepository.findIdByCode(categoryCode)
                .orElseThrow(() -> new CatalogException(CatalogErrorCode.CATEGORY_NOT_FOUND));
        return subCategoryRepository.findById(id)
                .orElseThrow(() -> new CatalogException(CatalogErrorCode.CATEGORY_NOT_FOUND));
    }

    private CatalogProduct buildNewProduct(ProductBulkUpsertRequest req, CatalogSubCategory category) {
        boolean active = req.isActive() == null || req.isActive();
        return new CatalogProduct(null, null, req.productName(), category.id(),
                req.unitPrice(), Unit.from(req.unit()), req.description(), req.memo(), active);
    }

    /**
     * 대분류 변경 금지 — 소분류 변경은 같은 대분류 내에서만 허용.
     */
    private CatalogProduct buildUpdatedProduct(ProductBulkUpsertRequest req, CatalogSubCategory category) {
        CatalogProduct existing = productRepository.findById(req.id())
                .orElseThrow(() -> new CatalogException(CatalogErrorCode.PRODUCT_NOT_FOUND));
        CatalogSubCategory existingCategory = subCategoryRepository.findById(existing.subCategoryId())
                .orElseThrow(() -> new CatalogException(CatalogErrorCode.CATEGORY_NOT_FOUND));
        if (existingCategory.topCategory() != category.topCategory()) {
            throw new CatalogException(CatalogErrorCode.TOP_CATEGORY_CHANGE_NOT_ALLOWED);
        }

        boolean active = req.isActive() == null ? existing.active() : req.isActive();
        return existing.update(req.productName(), category.id(), req.unitPrice(), Unit.from(req.unit()),
                req.description(), req.memo(), active);
    }
}

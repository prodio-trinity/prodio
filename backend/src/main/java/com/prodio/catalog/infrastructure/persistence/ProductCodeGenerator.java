package com.prodio.catalog.infrastructure.persistence;

import com.prodio.catalog.domain.TopCategory;
import com.prodio.catalog.exception.CatalogErrorCode;
import com.prodio.catalog.exception.CatalogException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class ProductCodeGenerator {
    private final SpringDataCatalogProductRepository springDataCatalogProductRepository;
    private final SpringDataCatalogSubCategoryRepository springDataCatalogSubCategoryRepository;

    String next(Long subCategoryId) {
        CatalogSubCategoryEntity subCategory = springDataCatalogSubCategoryRepository.findById(subCategoryId)
                .orElseThrow(() -> new CatalogException(CatalogErrorCode.CATEGORY_NOT_FOUND));
        TopCategory top = subCategory.getTopCategory();
        Long seq = springDataCatalogProductRepository.nextSequenceValue(top.sequenceName());
        return top.codePrefix() + "-" + String.format("%06d", seq);
    }
}
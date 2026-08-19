package com.prodio.catalog.application;

import com.prodio.catalog.domain.CatalogSubCategory;
import com.prodio.catalog.domain.TopCategory;
import com.prodio.catalog.exception.CatalogErrorCode;
import com.prodio.catalog.exception.CatalogException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogCategoryService {
    private final CatalogSubCategoryRepository subCategoryRepository;

    public List<CatalogSubCategory> getSubCategories(Boolean isActive) {
        return subCategoryRepository.findAll(isActive);
    }

    @Transactional
    public CatalogSubCategory createSubCategory(String parentCode, String subCategoryCode, String subCategoryName) {
        TopCategory topCategory = parseTopCategory(parentCode);
        String normalizedCode = subCategoryCode.trim().toUpperCase();
        if (subCategoryRepository.findIdByCode(normalizedCode).isPresent()) {
            throw new CatalogException(CatalogErrorCode.DUPLICATE_SUB_CATEGORY_CODE);
        }
        CatalogSubCategory subCategory = CatalogSubCategory.register(normalizedCode, subCategoryName, topCategory);
        return subCategoryRepository.save(subCategory);
    }

    @Transactional
    public CatalogSubCategory updateSubCategory(Long id, String subCategoryName, boolean isActive) {
        return subCategoryRepository.update(id, subCategoryName, isActive)
                .orElseThrow(() -> new CatalogException(CatalogErrorCode.CATEGORY_NOT_FOUND));
    }

    private TopCategory parseTopCategory(String parentCode) {
        try {
            return TopCategory.valueOf(parentCode);
        } catch (IllegalArgumentException e) {
            throw new CatalogException(CatalogErrorCode.INVALID_TOP_CATEGORY);
        }
    }
}
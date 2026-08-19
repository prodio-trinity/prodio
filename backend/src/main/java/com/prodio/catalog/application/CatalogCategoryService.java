package com.prodio.catalog.application;

import com.prodio.catalog.domain.CatalogSubCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogCategoryService {
    private final CatalogSubCategoryQueryRepository subCategoryQueryRepository;

    public List<CatalogSubCategory> getSubCategories(Boolean isActive) {
        return subCategoryQueryRepository.findAll(isActive);
    }
}
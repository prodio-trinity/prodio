package com.prodio.catalog.domain;

public record CatalogSubCategory(
        Long id,
        String subCategoryCode,
        String name,
        TopCategory topCategory,
        boolean active
) {
}
package com.prodio.catalog.domain;

public record CatalogSubCategory(
        Long id,
        String subCategoryCode,
        String name,
        TopCategory topCategory,
        boolean active
) {
    public CatalogSubCategory {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name은 비어 있을 수 없습니다.");
        }
    }

    public static CatalogSubCategory register(String subCategoryCode, String name, TopCategory topCategory) {
        return new CatalogSubCategory(null, subCategoryCode, name, topCategory, true);
    }
}
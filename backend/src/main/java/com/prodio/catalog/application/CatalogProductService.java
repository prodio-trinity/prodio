package com.prodio.catalog.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CatalogProductService {
    private final CatalogProductQueryRepository queryRepository;

    public Page<ProductListItem> getProductList(String keyword, Long categoryId, Boolean isActive,
            int page, int size, String sort) {
        return queryRepository.findProducts(normalize(keyword), categoryId, isActive, page, size, sort);
    }

    private static String normalize(String keyword) {
        if (keyword == null) return null;
        String trimmed = keyword.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
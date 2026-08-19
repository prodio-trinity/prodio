package com.prodio.catalog.application;

import org.springframework.data.domain.Page;

public interface CatalogProductQueryRepository {
    /** keyword는 productName/productCode 중 하나라도 매칭되면 포함 */
    Page<ProductListItem> findProducts(String keyword, Long categoryId, Boolean isActive,
            int page, int size, String sort);
}
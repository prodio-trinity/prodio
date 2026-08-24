package com.prodio.catalog.application;

import org.springframework.data.domain.Page;

import java.util.List;

public interface CatalogProductQueryRepository {
    /** keyword는 productName/productCode 중 하나라도 매칭되면 포함 */
    Page<ProductListItem> findProducts(String keyword, Long categoryId, Boolean isActive,
            int page, int size, String sort);

    /** 엑셀 내보내기 */
    List<ProductListItem> findAllForExport(String keyword, Long categoryId, Boolean isActive, String sort);
}
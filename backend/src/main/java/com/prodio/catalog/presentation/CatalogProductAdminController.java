package com.prodio.catalog.presentation;

import com.prodio.catalog.application.CatalogProductService;
import com.prodio.catalog.application.ProductListItem;
import com.prodio.shared.ApiResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
@Validated
class CatalogProductAdminController {
    private final CatalogProductService catalogProductService;

    @GetMapping
    ApiResponse<ProductPageResponse> getAdminProductList(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "") String sort) {
        Page<ProductListItem> result = catalogProductService
                .getProductList(keyword, categoryId, isActive, page, size, sort);
        return ApiResponse.success(ProductPageResponse.from(result));
    }

    record ProductListItemResponse(long id, String productCode, String productName, Long subCategoryId,
            String subCategoryName, String topCategory, String topCategoryDisplayName, BigDecimal unitPrice,
            String unit, String description, String memo, boolean isActive, Instant createdAt) {
        static ProductListItemResponse from(ProductListItem item) {
            return new ProductListItemResponse(item.id(), item.productCode(), item.productName(),
                    item.subCategoryId(), item.subCategoryName(), item.topCategory(),
                    item.topCategoryDisplayName(), item.unitPrice(), item.unit(), item.description(),
                    item.memo(), item.active(), item.createdAt());
        }
    }

    record ProductPageResponse(List<ProductListItemResponse> products, int page, int size,
            long totalElements, int totalPages) {
        static ProductPageResponse from(Page<ProductListItem> page) {
            List<ProductListItemResponse> products = page.getContent().stream()
                    .map(ProductListItemResponse::from).toList();
            return new ProductPageResponse(products, page.getNumber(), page.getSize(),
                    page.getTotalElements(), page.getTotalPages());
        }
    }
}
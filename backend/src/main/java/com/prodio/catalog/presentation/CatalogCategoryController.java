package com.prodio.catalog.presentation;

import com.prodio.catalog.application.CatalogCategoryService;
import com.prodio.catalog.domain.CatalogSubCategory;
import com.prodio.catalog.domain.TopCategory;
import com.prodio.shared.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/catalog/categories")
@RequiredArgsConstructor
class CatalogCategoryController {
    private final CatalogCategoryService catalogCategoryService;

    @GetMapping
    ApiResponse<CategoryListResponse> getCategories(@RequestParam(required = false) Boolean isActive) {
        List<TopCategoryResponse> topCategories = List.of(TopCategory.values()).stream()
                .map(top -> new TopCategoryResponse(top.name(), top.displayName()))
                .toList();
        List<CatalogSubCategory> subCategories = catalogCategoryService.getSubCategories(isActive);
        return ApiResponse.success(new CategoryListResponse(topCategories, subCategories));
    }

    record TopCategoryResponse(String code, String displayName) {
    }

    record CategoryListResponse(List<TopCategoryResponse> topCategories,
                                List<CatalogSubCategory> subCategories) {
    }
}
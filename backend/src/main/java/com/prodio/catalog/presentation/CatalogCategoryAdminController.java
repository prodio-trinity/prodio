package com.prodio.catalog.presentation;

import com.prodio.catalog.application.CatalogCategoryService;
import com.prodio.catalog.domain.CatalogSubCategory;
import com.prodio.shared.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/catalog/categories")
@RequiredArgsConstructor
class CatalogCategoryAdminController {
    private final CatalogCategoryService catalogCategoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ApiResponse<SubCategoryResponse> create(@Valid @RequestBody CreateSubCategoryRequest request) {
        CatalogSubCategory created = catalogCategoryService.createSubCategory(
                request.parentCode(), request.subCategoryCode(), request.subCategoryName());
        return ApiResponse.success(SubCategoryResponse.from(created));
    }

    record CreateSubCategoryRequest(
            @NotBlank String parentCode,
            @NotBlank String subCategoryCode,
            @NotBlank String subCategoryName) {
    }

    record SubCategoryResponse(Long id, String subCategoryCode, String name, String topCategory, boolean isActive) {
        static SubCategoryResponse from(CatalogSubCategory domain) {
            return new SubCategoryResponse(domain.id(), domain.subCategoryCode(), domain.name(),
                    domain.topCategory().name(), domain.active());
        }
    }
}
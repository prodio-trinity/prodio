package com.prodio.catalog.presentation;

import com.prodio.catalog.application.CatalogCategoryService;
import com.prodio.catalog.domain.CatalogSubCategory;
import com.prodio.shared.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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

    @PutMapping("/{id}")
    ApiResponse<SubCategoryResponse> update(@PathVariable Long id, 
                                            @Valid @RequestBody UpdateSubCategoryRequest request) {
        CatalogSubCategory updated = catalogCategoryService.updateSubCategory(
                id, request.subCategoryName(), request.isActive());
        return ApiResponse.success(SubCategoryResponse.from(updated));
    }

    record CreateSubCategoryRequest(
            @NotBlank String parentCode,
            @NotBlank String subCategoryCode,
            @NotBlank String subCategoryName) {
    }

    record UpdateSubCategoryRequest(
            @NotBlank String subCategoryName,
            @NotNull Boolean isActive) {
    }

    record SubCategoryResponse(Long id, String subCategoryCode, String name, String topCategory, boolean isActive) {
        static SubCategoryResponse from(CatalogSubCategory domain) {
            return new SubCategoryResponse(domain.id(), domain.subCategoryCode(), domain.name(),
                    domain.topCategory().name(), domain.active());
        }
    }
}
package com.prodio.catalog.infrastructure.persistence;

import com.prodio.catalog.application.CatalogProductQueryRepository;
import com.prodio.catalog.application.ProductListItem;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
class JpaCatalogProductQueryRepository implements CatalogProductQueryRepository {
    private static final Set<String> SORTABLE_FIELDS = Set.of("productName", "productCode", "createdAt");

    private final SpringDataCatalogProductRepository springDataCatalogProductRepository;
    private final SpringDataCatalogSubCategoryRepository springDataCatalogSubCategoryRepository;

    @Override
    public Page<ProductListItem> findProducts(String keyword, Long categoryId, Boolean isActive,
            int page, int size, String sort) {
        Pageable pageable = PageRequest.of(page, size, parseSort(sort));
        Page<CatalogProductEntity> products = springDataCatalogProductRepository
                .findProducts(escapeLike(keyword), categoryId, isActive, pageable);

        Map<Long, CatalogSubCategoryEntity> subCategoriesById = subCategoriesById(products);
        return products.map(entity -> toListItem(entity, subCategoriesById.get(entity.getSubCategoryId())));
    }

    private Map<Long, CatalogSubCategoryEntity> subCategoriesById(Page<CatalogProductEntity> products) {
        Set<Long> subCategoryIds = products.getContent().stream()
                .map(CatalogProductEntity::getSubCategoryId)
                .collect(Collectors.toSet());
        return springDataCatalogSubCategoryRepository.findAllById(subCategoryIds).stream()
                .collect(Collectors.toMap(CatalogSubCategoryEntity::getId, e -> e));
    }

    private static String escapeLike(String value) {
        if (value == null) return null;
        return value.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }

    private static Sort parseSort(String sort) {
        if (sort == null || sort.isBlank())
            return Sort.by(Sort.Direction.ASC, "productCode");

        String[] parts = sort.split(",", 2);
        String property = parts[0].trim();

        if (!SORTABLE_FIELDS.contains(property))
            return Sort.by(Sort.Direction.ASC, "productCode");

        Sort.Direction direction = parts.length > 1 && "desc".equalsIgnoreCase(parts[1].trim())
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        return Sort.by(direction, property);
    }

    private static ProductListItem toListItem(CatalogProductEntity entity, CatalogSubCategoryEntity subCategory) {
        return new ProductListItem(
                entity.getId(),
                entity.getProductCode(),
                entity.getProductName(),
                entity.getSubCategoryId(),
                subCategory == null ? null : subCategory.getName(),
                subCategory == null ? null : subCategory.getTopCategory().name(),
                subCategory == null ? null : subCategory.getTopCategory().displayName(),
                entity.getUnitPrice(),
                entity.getUnit().name(),
                entity.getDescription(),
                entity.getMemo(),
                entity.isActive(),
                entity.getCreatedAt()
        );
    }
}
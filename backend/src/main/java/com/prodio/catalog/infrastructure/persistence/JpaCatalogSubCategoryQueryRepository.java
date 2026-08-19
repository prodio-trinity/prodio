package com.prodio.catalog.infrastructure.persistence;

import com.prodio.catalog.application.CatalogSubCategoryQueryRepository;
import com.prodio.catalog.domain.CatalogSubCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
class JpaCatalogSubCategoryQueryRepository implements CatalogSubCategoryQueryRepository {
    private final SpringDataCatalogSubCategoryRepository springDataCatalogSubCategoryRepository;

    @Override
    public Optional<Long> findIdByCode(String subCategoryCode) {
        return springDataCatalogSubCategoryRepository.findBySubCategoryCode(subCategoryCode)
                .map(CatalogSubCategoryEntity::getId);
    }

    @Override
    public List<CatalogSubCategory> findAll(Boolean isActive) {
        List<CatalogSubCategoryEntity> entities = Boolean.TRUE.equals(isActive)
                ? springDataCatalogSubCategoryRepository.findByActiveTrue()
                : springDataCatalogSubCategoryRepository.findAll();
        return entities.stream().map(CatalogSubCategoryEntity::toDomain)
                .toList();
    }
}

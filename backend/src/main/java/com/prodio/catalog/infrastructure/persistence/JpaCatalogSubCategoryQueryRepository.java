package com.prodio.catalog.infrastructure.persistence;

import com.prodio.catalog.application.CatalogSubCategoryQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

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
}

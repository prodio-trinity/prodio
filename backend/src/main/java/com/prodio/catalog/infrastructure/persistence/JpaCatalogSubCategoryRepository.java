package com.prodio.catalog.infrastructure.persistence;

import com.prodio.catalog.application.CatalogSubCategoryRepository;
import com.prodio.catalog.domain.CatalogSubCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
class JpaCatalogSubCategoryRepository implements CatalogSubCategoryRepository {
    private final SpringDataCatalogSubCategoryRepository springDataCatalogSubCategoryRepository;

    @Override
    public Optional<Long> findIdByCode(String subCategoryCode) {
        return springDataCatalogSubCategoryRepository.findBySubCategoryCode(subCategoryCode)
                .map(CatalogSubCategoryEntity::getId);
    }

    @Override
    public Optional<CatalogSubCategory> findById(Long id) {
        return springDataCatalogSubCategoryRepository.findById(id)
                .map(CatalogSubCategoryEntity::toDomain);
    }

    @Override
    public List<CatalogSubCategory> findAll(Boolean isActive) {
        List<CatalogSubCategoryEntity> entities = Boolean.TRUE.equals(isActive)
                ? springDataCatalogSubCategoryRepository.findByActiveTrue()
                : springDataCatalogSubCategoryRepository.findAll();
        return entities.stream().map(CatalogSubCategoryEntity::toDomain)
                .toList();
    }

    @Override
    public CatalogSubCategory save(CatalogSubCategory subCategory) {
        CatalogSubCategoryEntity entity = CatalogSubCategoryEntity.from(subCategory);
        return springDataCatalogSubCategoryRepository.save(entity).toDomain();
    }

    @Override
    public Optional<CatalogSubCategory> update(Long id, String name, boolean active) {
        Optional<CatalogSubCategoryEntity> found = springDataCatalogSubCategoryRepository.findById(id);
        
        if (found.isEmpty()) {
            return Optional.empty();
        }
        
        CatalogSubCategoryEntity entity = found.get();
        CatalogSubCategory updated = entity.toDomain().update(name, active);
        entity.update(updated);
        return Optional.of(updated);
    }
}
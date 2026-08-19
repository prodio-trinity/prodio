package com.prodio.catalog.infrastructure.persistence;

import com.prodio.catalog.application.CatalogClientRepository;
import com.prodio.catalog.domain.CatalogClient;
import com.prodio.catalog.exception.CatalogErrorCode;
import com.prodio.catalog.exception.CatalogException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
class JpaCatalogClientRepository implements CatalogClientRepository {
    private final SpringDataCatalogClientRepository springDataCatalogClientRepository;
    private final ClientCodeGenerator clientCodeGenerator;

    @Override
    public Optional<CatalogClient> findById(Long id) {
        return springDataCatalogClientRepository.findById(id).map(CatalogClientEntity::toDomain);
    }

    @Override
    public Optional<CatalogClient> findByBusinessRegNo(String businessRegNo) {
        return springDataCatalogClientRepository.findByBusinessRegNo(businessRegNo)
                .map(CatalogClientEntity::toDomain);
    }

    @Override
    public CatalogClient save(CatalogClient client) {
        CatalogClientEntity entity = (client.id() == null) ? insertEntity(client) : updateEntity(client);
        CatalogClientEntity saved = springDataCatalogClientRepository.save(entity);
        springDataCatalogClientRepository.flush();
        return saved.toDomain();
    }

    private CatalogClientEntity insertEntity(CatalogClient client) {
        CatalogClientEntity entity = CatalogClientEntity.from(client);
        entity.assignClientCode(clientCodeGenerator.next());
        return entity;
    }

    private CatalogClientEntity updateEntity(CatalogClient client) {
        CatalogClientEntity entity = springDataCatalogClientRepository.findById(client.id())
                .orElseThrow(() -> new CatalogException(CatalogErrorCode.CLIENT_NOT_FOUND));
        entity.update(client);
        return entity;
    }
}
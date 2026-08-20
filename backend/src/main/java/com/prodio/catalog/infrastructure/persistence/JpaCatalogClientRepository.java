package com.prodio.catalog.infrastructure.persistence;

import com.prodio.catalog.application.CatalogClientRepository;
import com.prodio.catalog.domain.CatalogClient;
import com.prodio.catalog.exception.CatalogErrorCode;
import com.prodio.catalog.exception.CatalogException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
    public Optional<CatalogClient> findByUserId(Long userId) {
        return springDataCatalogClientRepository.findByUserId(userId).map(CatalogClientEntity::toDomain);
    }

    @Override
    public Map<String, Long> findIdsByBusinessRegNoIn(Collection<String> businessRegNos) {
        if (businessRegNos.isEmpty()) return Map.of();
        return springDataCatalogClientRepository.findIdsByBusinessRegNoIn(businessRegNos).stream()
                .collect(Collectors.toMap(SpringDataCatalogClientRepository.BusinessRegNoIdView::getBusinessRegNo,
                        SpringDataCatalogClientRepository.BusinessRegNoIdView::getId));
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
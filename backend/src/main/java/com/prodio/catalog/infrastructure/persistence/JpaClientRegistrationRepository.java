package com.prodio.catalog.infrastructure.persistence;

import com.prodio.catalog.application.ClientRegistrationRepository;
import com.prodio.catalog.domain.ClientRegistration;
import com.prodio.catalog.domain.RegistrationStatus;
import com.prodio.catalog.exception.CatalogErrorCode;
import com.prodio.catalog.exception.CatalogException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
class JpaClientRegistrationRepository implements ClientRegistrationRepository {
    private final SpringDataClientRegistrationRepository springDataRepository;

    @Override
    public Optional<ClientRegistration> findById(long id) {
        return springDataRepository.findById(id).map(ClientRegistrationEntity::toDomain);
    }

    @Override
    public Optional<ClientRegistration> findByUserId(long userId) {
        return springDataRepository.findByUserId(userId).map(ClientRegistrationEntity::toDomain);
    }

    @Override
    public List<ClientRegistration> findAll(RegistrationStatus status) {
        return springDataRepository.findAll(status).stream()
                .map(ClientRegistrationEntity::toDomain)
                .toList();
    }

    @Override
    public ClientRegistration save(ClientRegistration request) {
        ClientRegistrationEntity entity = (request.id() == null)
                ? insertEntity(request)
                : updateEntity(request);
        return springDataRepository.save(entity).toDomain();
    }

    private ClientRegistrationEntity insertEntity(ClientRegistration request) {
        return ClientRegistrationEntity.from(request);
    }

    private ClientRegistrationEntity updateEntity(ClientRegistration request) {
        ClientRegistrationEntity entity = springDataRepository.findById(request.id())
                .orElseThrow(() -> new CatalogException(CatalogErrorCode.REGISTRATION_REQUEST_NOT_FOUND));
        entity.update(request);
        return entity;
    }
}
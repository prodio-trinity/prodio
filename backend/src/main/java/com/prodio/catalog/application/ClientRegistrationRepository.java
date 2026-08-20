package com.prodio.catalog.application;

import com.prodio.catalog.domain.ClientRegistration;
import com.prodio.catalog.domain.RegistrationStatus;

import java.util.List;
import java.util.Optional;

public interface ClientRegistrationRepository {
    Optional<ClientRegistration> findById(long id);

    Optional<ClientRegistration> findByUserId(long userId);

    /** status가 null이면 전체 조회. */
    List<ClientRegistration> findAll(RegistrationStatus status);

    ClientRegistration save(ClientRegistration request);
}
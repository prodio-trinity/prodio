package com.prodio.catalog.infrastructure.persistence;

import com.prodio.catalog.domain.RegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

interface SpringDataClientRegistrationRepository extends JpaRepository<ClientRegistrationEntity, Long> {
    Optional<ClientRegistrationEntity> findByUserId(long userId);

    @Query("SELECT r FROM ClientRegistrationEntity r WHERE :status IS NULL OR r.status = :status")
    List<ClientRegistrationEntity> findAll(@Param("status") RegistrationStatus status);
}
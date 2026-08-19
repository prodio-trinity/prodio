package com.prodio.stat.infrastructure.persistence;

import com.prodio.stat.domain.QueryType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface AiQueryLogJpaRepository extends JpaRepository<AiQueryLogEntity, UUID> {
    Page<AiQueryLogEntity> findByRequestedByAndQueryTypeOrderByRequestedAtDesc(
            long requestedBy, QueryType queryType, Pageable pageable);
}

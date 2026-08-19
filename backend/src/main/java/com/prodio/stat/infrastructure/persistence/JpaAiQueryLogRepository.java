package com.prodio.stat.infrastructure.persistence;

import com.prodio.stat.application.AiQueryLogPage;
import com.prodio.stat.application.AiQueryLogRepository;
import com.prodio.stat.domain.AiQueryLog;
import com.prodio.stat.domain.QueryType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class JpaAiQueryLogRepository implements AiQueryLogRepository {
    private final AiQueryLogJpaRepository aiQueryLogs;

    @Override
    public AiQueryLog save(AiQueryLog log) {
        return aiQueryLogs.save(AiQueryLogEntity.from(log)).toDomain();
    }

    @Override
    public AiQueryLogPage findPage(QueryType queryType, int page, int size) {
        Page<AiQueryLogEntity> result = aiQueryLogs.findByQueryTypeOrderByRequestedAtDesc(
                queryType, PageRequest.of(page, size));

        return new AiQueryLogPage(result.getContent().stream().map(AiQueryLogEntity::toDomain).toList(),
                page, size, result.getTotalElements());
    }
}

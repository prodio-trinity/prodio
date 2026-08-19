package com.prodio.stat.infrastructure.persistence;

import com.prodio.stat.domain.AiQueryLog;
import com.prodio.stat.domain.QueryType;
import com.prodio.stat.domain.SourceType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Getter
@Table(name = "statistics_ai_query_log")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class AiQueryLogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "query_type", nullable = false, length = 20)
    private QueryType queryType;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", length = 20)
    private SourceType sourceType;

    @Column(nullable = false, length = 500)
    private String question;

    @Column(nullable = false)
    private String response;

    @Column(name = "requested_at", nullable = false)
    private OffsetDateTime requestedAt;

    private AiQueryLogEntity(AiQueryLog log) {
        queryType = log.queryType();
        sourceType = log.sourceType();
        question = log.question();
        response = log.response();
        requestedAt = OffsetDateTime.now();
    }

    static AiQueryLogEntity from(AiQueryLog log) {
        return new AiQueryLogEntity(log);
    }

    AiQueryLog toDomain() {
        return new AiQueryLog(id, queryType, sourceType, question, response, requestedAt);
    }
}

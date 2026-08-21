package com.prodio.stat.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AiQueryLog(
        UUID id,
        QueryType queryType,
        SourceType sourceType,
        long requestedBy,
        String question,
        String response,
        OffsetDateTime requestedAt
) {
    public static AiQueryLog summary(long requestedBy, String question, String response) {
        return new AiQueryLog(
            null, QueryType.STATS_SUMMARY, null,
            requestedBy, question, response, null
        );
    }

    public static AiQueryLog ragQa(long requestedBy, SourceType sourceType, String question, String response) {
        return new AiQueryLog(
            null, QueryType.RAG_QA, sourceType,
            requestedBy, question, response, null
        );
    }
}

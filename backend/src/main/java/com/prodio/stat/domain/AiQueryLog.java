package com.prodio.stat.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AiQueryLog(
        UUID id,
        QueryType queryType,
        SourceType sourceType,
        String question,
        String response,
        OffsetDateTime requestedAt
) {
    public static AiQueryLog summary(String question, String response) {
        return new AiQueryLog(
            null, QueryType.STATS_SUMMARY, null,
            question, response, null
        );
    }
}

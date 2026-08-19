package com.prodio.stat.infrastructure.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "prodio.ai.gemini")
public record GeminiProperties(
        String apiKey,
        String baseUrl,
        String embeddingModel,
        String chatModel,
        int connectTimeoutMillis,
        int readTimeoutMillis,
        int maxRetries,
        long initialBackoffMillis
) {}

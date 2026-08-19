package com.prodio.stat.infrastructure.ai;

import com.prodio.infra.exception.InfraErrorCode;
import com.prodio.infra.exception.InfraException;
import com.prodio.stat.application.AiClient;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Component
class GeminiClient implements AiClient {
    private final RestClient geminiRestClient;
    private final GeminiProperties properties;
    private final RetryExecutor retryExecutor;

    GeminiClient(RestClient geminiRestClient, GeminiProperties properties) {
        this.geminiRestClient = geminiRestClient;
        this.properties = properties;
        this.retryExecutor = new RetryExecutor(properties.maxRetries(), properties.initialBackoffMillis());
    }

    @Override
    public float[] embed(String text) {
        try {
            return retryExecutor.execute(() -> callEmbed(text), this::isRetryable);
        } catch (RestClientException exception) {
            throw new InfraException(InfraErrorCode.AI_REQUEST_FAILED, exception);
        }
    }

    private float[] callEmbed(String text) {
        EmbedRequest request = new EmbedRequest("models/" + properties.embeddingModel(),
                new EmbedRequest.Content(List.of(new EmbedRequest.Part(text))));

        EmbedResponse response = geminiRestClient.post()
                .uri("/v1beta/models/{model}:embedContent", properties.embeddingModel())
                .body(request)
                .retrieve()
                .body(EmbedResponse.class);

        if (response == null || response.embedding() == null) {
            throw new InfraException(InfraErrorCode.AI_REQUEST_FAILED);
        }

        List<Float> values = response.embedding().values();
        float[] result = new float[values.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = values.get(i);
        }
        
        return result;
    }

    /** 5xx, 타임아웃/연결 실패, 429(rate limit)는 재시도 대상. 그 외 4xx(API 키 오류 등)는 즉시 실패. */
    private boolean isRetryable(RuntimeException exception) {
        return exception instanceof HttpServerErrorException
                || exception instanceof ResourceAccessException
                || exception instanceof HttpClientErrorException.TooManyRequests;
    }

    private record EmbedRequest(String model, Content content) {
        record Content(List<Part> parts) {}
        record Part(String text) {}
    }

    private record EmbedResponse(Embedding embedding) {
        record Embedding(List<Float> values) {}
    }
}

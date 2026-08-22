package com.prodio.stat.infrastructure.ai;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.prodio.infra.exception.InfraErrorCode;
import com.prodio.infra.exception.InfraException;
import com.prodio.stat.application.AiClient;
import com.prodio.stat.application.ToolCall;
import com.prodio.stat.application.ToolParam;
import com.prodio.stat.application.ToolSpec;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
class GeminiClient implements AiClient {

    private static final int MAX_TOOL_TURNS = 4;
    /** statistics_*_embeddings 테이블의 pgvector 컬럼이 vector(768)로 고정돼 있어 임베딩 차원도 여기 맞춘다. */
    private static final int EMBEDDING_DIMENSIONS = 768;

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
                new EmbedRequest.Content(List.of(new EmbedRequest.Part(text))), EMBEDDING_DIMENSIONS);

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

    @Override
    public String generateText(String prompt) {
        try {
            return retryExecutor.execute(() -> callGenerate(prompt), this::isRetryable);
        } catch (RestClientException exception) {
            throw new InfraException(InfraErrorCode.AI_REQUEST_FAILED, exception);
        }
    }

    private String callGenerate(String prompt) {
        GenerateRequest request = new GenerateRequest(
                List.of(new GenerateRequest.Content(List.of(new GenerateRequest.Part(prompt)))));

        GenerateResponse response = geminiRestClient.post()
                .uri("/v1beta/models/{model}:generateContent", properties.chatModel())
                .body(request)
                .retrieve()
                .body(GenerateResponse.class);

        if (response == null || response.candidates() == null || response.candidates().isEmpty()) {
            throw new InfraException(InfraErrorCode.AI_REQUEST_FAILED);
        }

        GenerateResponse.Content content = response.candidates().get(0).content();
        if (content == null || content.parts() == null || content.parts().isEmpty()) {
            throw new InfraException(InfraErrorCode.AI_REQUEST_FAILED);
        }

        return content.parts().get(0).text();
    }

    @Override
    public String ask(String question, List<ToolSpec> tools, Function<ToolCall, String> toolExecutor) {
        List<AskContent> contents = new ArrayList<>();
        contents.add(new AskContent("user", List.of(AskPart.text(question))));
        List<ToolsWrapper> toolsWrapper = List.of(new ToolsWrapper(toFunctionDeclarations(tools)));

        for (int turn = 0; turn < MAX_TOOL_TURNS; turn++) {
            AskContent modelContent = callAsk(contents, toolsWrapper);
            contents.add(new AskContent("model", modelContent.parts()));

            List<AskPart.FunctionCallPart> functionCalls = modelContent.parts().stream()
                    .map(AskPart::functionCall)
                    .filter(Objects::nonNull)
                    .toList();

            if (functionCalls.isEmpty()) {
                return extractText(modelContent);
            }

            List<AskPart> functionResponses = functionCalls.stream()
                    .map(functionCall -> executeTool(functionCall, toolExecutor))
                    .toList();
            contents.add(new AskContent("user", functionResponses));
        }

        throw new InfraException(InfraErrorCode.AI_REQUEST_FAILED);
    }

    private AskContent callAsk(List<AskContent> contents, List<ToolsWrapper> tools) {
        try {
            return retryExecutor.execute(() -> {
                AskRequest request = new AskRequest(contents, tools);
                AskResponse response = geminiRestClient.post()
                        .uri("/v1beta/models/{model}:generateContent", properties.chatModel())
                        .body(request)
                        .retrieve()
                        .body(AskResponse.class);

                if (response == null || response.candidates() == null || response.candidates().isEmpty()) {
                    throw new InfraException(InfraErrorCode.AI_REQUEST_FAILED);
                }

                AskContent content = response.candidates().get(0).content();
                if (content == null || content.parts() == null || content.parts().isEmpty()) {
                    throw new InfraException(InfraErrorCode.AI_REQUEST_FAILED);
                }

                return content;
            }, this::isRetryable);
        } catch (RestClientException exception) {
            throw new InfraException(InfraErrorCode.AI_REQUEST_FAILED, exception);
        }
    }

    private String extractText(AskContent content) {
        String text = content.parts().stream()
                .map(AskPart::text)
                .filter(part -> part != null && !part.isBlank())
                .collect(Collectors.joining());

        if (text.isBlank()) {
            throw new InfraException(InfraErrorCode.AI_REQUEST_FAILED);
        }

        return text;
    }

    private AskPart executeTool(AskPart.FunctionCallPart functionCall, Function<ToolCall, String> toolExecutor) {
        Map<String, String> args = functionCall.args() == null
                ? Map.of()
                : functionCall.args().entrySet().stream()
                        .filter(entry -> entry.getValue() != null)
                        .collect(Collectors.toMap(Map.Entry::getKey, entry -> String.valueOf(entry.getValue())));

        String result = toolExecutor.apply(new ToolCall(functionCall.name(), args));

        return AskPart.functionResponse(functionCall.name(), result);
    }

    private List<FunctionDeclaration> toFunctionDeclarations(List<ToolSpec> tools) {
        return tools.stream()
                .map(tool -> new FunctionDeclaration(tool.name(), tool.description(), toParametersSchema(tool.parameters())))
                .toList();
    }

    private ParametersSchema toParametersSchema(List<ToolParam> params) {
        Map<String, PropertySchema> properties = params.stream()
                .collect(Collectors.toMap(ToolParam::name, param -> new PropertySchema("STRING", param.description())));

        List<String> required = params.stream()
            .filter(ToolParam::required)
            .map(ToolParam::name)
            .toList();

        return new ParametersSchema("OBJECT", properties, required);
    }

    /** 5xx, 타임아웃/연결 실패, 429(rate limit)는 재시도 대상. 그 외 4xx(API 키 오류 등)는 즉시 실패. */
    private boolean isRetryable(RuntimeException exception) {
        return exception instanceof HttpServerErrorException
                || exception instanceof ResourceAccessException
                || exception instanceof HttpClientErrorException.TooManyRequests;
    }

    private record EmbedRequest(String model, Content content, int outputDimensionality) {
        record Content(List<Part> parts) {}
        record Part(String text) {}
    }

    private record EmbedResponse(Embedding embedding) {
        record Embedding(List<Float> values) {}
    }

    private record GenerateRequest(List<Content> contents) {
        record Content(List<Part> parts) {}
        record Part(String text) {}
    }

    private record GenerateResponse(List<Candidate> candidates) {
        record Candidate(Content content) {}
        record Content(List<Part> parts) {}
        record Part(String text) {}
    }

    private record AskRequest(List<AskContent> contents, List<ToolsWrapper> tools) {}

    private record AskContent(String role, List<AskPart> parts) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private record AskPart(String text, FunctionCallPart functionCall, FunctionResponsePart functionResponse) {
        static AskPart text(String text) {
            return new AskPart(text, null, null);
        }

        static AskPart functionResponse(String name, String result) {
            return new AskPart(null, null, new FunctionResponsePart(name, Map.of("result", result)));
        }

        record FunctionCallPart(String name, Map<String, Object> args) {}
        record FunctionResponsePart(String name, Map<String, Object> response) {}
    }

    private record AskResponse(List<Candidate> candidates) {
        record Candidate(AskContent content) {}
    }

    private record ToolsWrapper(List<FunctionDeclaration> functionDeclarations) {}
    private record FunctionDeclaration(String name, String description, ParametersSchema parameters) {}
    private record ParametersSchema(String type, Map<String, PropertySchema> properties, List<String> required) {}
    private record PropertySchema(String type, String description) {}
}

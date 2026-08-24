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

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
    private static final DateTimeFormatter TODAY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd(E)");

    private final RestClient geminiRestClient;
    private final GeminiProperties properties;
    private final RetryExecutor retryExecutor;
    private final Clock clock;

    GeminiClient(RestClient geminiRestClient, GeminiProperties properties, Clock clock) {
        this.geminiRestClient = geminiRestClient;
        this.properties = properties;
        this.retryExecutor = new RetryExecutor(properties.maxRetries(), properties.initialBackoffMillis());
        this.clock = clock;
    }

    @Override
    public float[] embed(String text) {
        try {
            return retryExecutor.execute(() -> callEmbed(text), this::isRetryable);
        } catch (RestClientException exception) {
            throw toInfraException(exception);
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
            throw toInfraException(exception);
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
        SystemInstruction systemInstruction = new SystemInstruction(List.of(AskPart.text(
                "오늘 날짜는 " + LocalDate.now(clock).format(TODAY_FORMAT) + "입니다. "
                        + "사용자가 '이번 달', '지난달', '올해', '이번 주'처럼 상대적인 날짜를 말하면 "
                        + "이 날짜를 기준으로 정확한 연도를 포함해 계산하세요. "
                        + "답변은 핵심 정보만 간결하게 전달하세요. queryOrderStats 결과에 품목별 분포처럼 "
                        + "긴 목록이 포함돼 있어도 전부 나열하지 말고, 질문에 필요한 만큼만 요약해서 답하세요. "
                        + "단, searchNotes 결과를 근거로 답할 때는 간결함을 이유로 근거를 생략하지 마세요 — "
                        + "어떤 주문/거래처인지만 말하고 왜 관련 있는지(파손/지연/사유 등 노트·메모의 구체적 "
                        + "문구)를 빼면 답이 안 됩니다. 관련된 노트/메모의 핵심 문구를 반드시 답변에 포함하세요. "
                        + "이 답변은 마크다운을 지원하지 않는 화면에 그대로 표시됩니다. **, *, #, |, - 같은 "
                        + "마크다운 기호를 절대 쓰지 말고 일반 텍스트로만 작성하세요. 항목을 나열할 때는 "
                        + "쉼표나 기호로 이어붙이지 말고, 항목마다 줄바꿈(\\n)으로 구분해 한 줄에 하나씩 쓰세요. "
                        + "도구 결과에 없는 값은 절대 만들어내지 마세요. 질문이 요구하는 값을 도구가 실제로 "
                        + "반환하지 않았다면, 다른 값을 대신 추정해서 확정된 답처럼 말하지 말고 해당 데이터가 "
                        + "없다고 명확히 답하세요.")));

        for (int turn = 0; turn < MAX_TOOL_TURNS; turn++) {
            AskContent modelContent = callAsk(contents, toolsWrapper, systemInstruction);
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

    private AskContent callAsk(List<AskContent> contents, List<ToolsWrapper> tools, SystemInstruction systemInstruction) {
        try {
            return retryExecutor.execute(() -> {
                AskRequest request = new AskRequest(systemInstruction, contents, tools);
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
            throw toInfraException(exception);
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

    /**
     * 재시도까지 소진한 뒤 최종적으로 실패한 원인을 구분해 서로 다른 InfraErrorCode로 매핑한다.
     * 429는 rate limit로, 그 외 4xx(잘못된 요청/스키마 오류 등)는 요청 자체 문제로, 나머지(5xx/타임아웃/
     * 응답 파싱 실패 등)는 일시적 장애로 안내해 프론트에서 사용자에게 다른 메시지를 보여줄 수 있게 한다.
     */
    private InfraException toInfraException(RestClientException exception) {
        if (exception instanceof HttpClientErrorException.TooManyRequests) {
            return new InfraException(InfraErrorCode.AI_RATE_LIMITED, exception);
        }
        if (exception instanceof HttpClientErrorException) {
            return new InfraException(InfraErrorCode.AI_REQUEST_INVALID, exception);
        }
        return new InfraException(InfraErrorCode.AI_REQUEST_FAILED, exception);
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

    private record AskRequest(SystemInstruction systemInstruction, List<AskContent> contents, List<ToolsWrapper> tools) {}

    private record SystemInstruction(List<AskPart> parts) {}

    private record AskContent(String role, List<AskPart> parts) {}

    /**
     * thinking 모델은 자신이 생성한 functionCall 파트의 thoughtSignature를 다음 턴에 그대로 돌려받지 못하면
     * "missing thought_signature" 400 에러를 낸다. 그래서 모델이 준 functionCall 파트를 다음 턴 contents에
     * 그대로 append할 때 이 값도 함께 보존해야 한다(우리가 직접 만드는 파트는 null로 둔다).
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private record AskPart(String text, FunctionCallPart functionCall, FunctionResponsePart functionResponse,
            String thoughtSignature) {
        static AskPart text(String text) {
            return new AskPart(text, null, null, null);
        }

        static AskPart functionResponse(String name, String result) {
            return new AskPart(null, null, new FunctionResponsePart(name, Map.of("result", result)), null);
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

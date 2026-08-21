package com.prodio.stat.application;

import java.util.List;
import java.util.function.Function;

public interface AiClient {
    float[] embed(String text);
    String generateText(String prompt);

    /**
     * tools 중 필요한 것을 Gemini가 판단해 호출하는 멀티턴 대화를 진행하고, 최종 자연어 답변을 반환한다.
     * 도구 호출 요청이 오면 toolExecutor로 실행한 뒤 그 결과를 다시 Gemini에 넘겨 대화를 이어간다.
     */
    String ask(String question, List<ToolSpec> tools, Function<ToolCall, String> toolExecutor);
}

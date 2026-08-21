package com.prodio.stat.application;

import java.util.Map;

/** Gemini가 실행을 요청한 함수 호출. */
public record ToolCall(String name, Map<String, String> args) {
}

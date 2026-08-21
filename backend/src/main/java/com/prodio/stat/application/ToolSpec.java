package com.prodio.stat.application;

import java.util.List;

/** Gemini에게 등록하는 함수(도구) 스펙. 파라미터는 전부 STRING 타입으로 취급한다. */
public record ToolSpec(String name, String description, List<ToolParam> parameters) {
}

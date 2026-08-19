package com.prodio.stat.application;

import com.prodio.stat.domain.AiQueryLog;

import java.util.List;

public record AiQueryLogPage(List<AiQueryLog> logs, int page, int size, long totalElements) {
    public AiQueryLogPage {
        logs = List.copyOf(logs);
    }
}

package com.prodio.statistics.exception;

import org.springframework.http.HttpStatus;

public enum StatisticsErrorCode {

    STATISTICS_NOT_FOUND(HttpStatus.NOT_FOUND, "조회 가능한 통계 데이터가 없습니다.");

    private final HttpStatus status;
    private final String message;

    StatisticsErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() { return status; }
    public String getMessage() { return message; }
}

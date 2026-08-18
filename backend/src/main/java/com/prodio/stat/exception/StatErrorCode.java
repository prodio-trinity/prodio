package com.prodio.stat.exception;

import org.springframework.http.HttpStatus;

public enum StatErrorCode {

    STAT_NOT_FOUND(HttpStatus.NOT_FOUND, "조회 가능한 통계 데이터가 없습니다."),
    STAT_INVALID_FILTER(HttpStatus.BAD_REQUEST, "from은 to보다 늦을 수 없습니다.");

    private final HttpStatus status;
    private final String message;

    StatErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() { return status; }
    public String getMessage() { return message; }
}

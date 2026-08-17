package com.prodio.production.exception;

import org.springframework.http.HttpStatus;

public enum ProductionErrorCode {

    PRODUCTION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 생산 기록입니다."),
    INVALID_PRODUCTION_STATUS(HttpStatus.BAD_REQUEST, "유효하지 않은 생산 상태입니다.");

    private final HttpStatus status;
    private final String message;

    ProductionErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() { return status; }
    public String getMessage() { return message; }
}

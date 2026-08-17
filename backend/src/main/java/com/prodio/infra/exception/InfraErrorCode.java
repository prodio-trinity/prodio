package com.prodio.infra.exception;

import org.springframework.http.HttpStatus;

public enum InfraErrorCode {

    SMS_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SMS 발송에 실패했습니다."),
    AI_REQUEST_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AI 요청 처리에 실패했습니다.");

    private final HttpStatus status;
    private final String message;

    InfraErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() { return status; }
    public String getMessage() { return message; }
}

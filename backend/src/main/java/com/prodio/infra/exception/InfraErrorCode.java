package com.prodio.infra.exception;

import org.springframework.http.HttpStatus;

public enum InfraErrorCode {

    SMS_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SMS 발송에 실패했습니다."),
    AI_REQUEST_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AI 요청 처리에 실패했습니다. 잠시 후 다시 시도해주세요."),
    AI_REQUEST_INVALID(HttpStatus.BAD_REQUEST, "AI 요청이 올바르지 않습니다. 질문을 바꿔서 다시 시도해주세요."),
    AI_RATE_LIMITED(HttpStatus.TOO_MANY_REQUESTS, "AI 요청이 많아 지금은 처리할 수 없습니다. 잠시 후 다시 시도해주세요.");

    private final HttpStatus status;
    private final String message;

    InfraErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() { return status; }
    public String getMessage() { return message; }
}

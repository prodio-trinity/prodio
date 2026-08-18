package com.prodio.order.exception;

import org.springframework.http.HttpStatus;

public enum OrderErrorCode {

    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 수주입니다."),
    CLIENT_NOT_FOUND(HttpStatus.BAD_REQUEST, "선택한 거래처를 찾을 수 없습니다."),
    CLIENT_ACCOUNT_NOT_LINKED(HttpStatus.FORBIDDEN, "로그인 계정에 연결된 거래처를 찾을 수 없습니다."),
    PRODUCT_NOT_FOUND(HttpStatus.BAD_REQUEST, "선택한 품목을 찾을 수 없습니다."),
    ORDER_CREATOR_NOT_FOUND(HttpStatus.UNAUTHORIZED, "수주 등록자를 확인할 수 없습니다."),
    INVALID_ORDER_REQUEST(HttpStatus.BAD_REQUEST, "수주 요청 값이 올바르지 않습니다."),
    INVALID_ORDER_STATUS(HttpStatus.BAD_REQUEST, "유효하지 않은 수주 상태입니다.");

    private final HttpStatus status;
    private final String message;

    OrderErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() { return status; }
    public String getMessage() { return message; }
}

package com.prodio.production.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ProductionErrorCode {
    ALREADY_EXISTS_ORDER(HttpStatus.BAD_REQUEST, "이미 등록된 주문 입니다."),
    PRODUCTION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 생산 기록입니다."),
    INVALID_PRODUCTION_STATUS(HttpStatus.BAD_REQUEST, "유효하지 않은 생산 상태입니다."),
    INVALID_PRODUCTION_REQUEST(HttpStatus.BAD_REQUEST, "생산 요청 값이 올바르지 않습니다.");


    private final HttpStatus status;
    private final String message;

    ProductionErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

}

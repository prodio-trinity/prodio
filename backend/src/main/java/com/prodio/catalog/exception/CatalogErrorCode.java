package com.prodio.catalog.exception;

import org.springframework.http.HttpStatus;

public enum CatalogErrorCode {

    CLIENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 클라이언트입니다."),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 제품입니다."),
    DUPLICATE_BUSINESS_REG_NO(HttpStatus.CONFLICT, "이미 등록된 사업자등록번호입니다."),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "분류코드를 찾을 수 없습니다."),
    INVALID_TOP_CATEGORY(HttpStatus.BAD_REQUEST, "존재하지 않는 대분류 코드입니다."),
    DUPLICATE_SUB_CATEGORY_CODE(HttpStatus.CONFLICT, "이미 존재하는 소분류 코드입니다.");

    private final HttpStatus status;
    private final String message;

    CatalogErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() { return status; }
    public String getMessage() { return message; }
}

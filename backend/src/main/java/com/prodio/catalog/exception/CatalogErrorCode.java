package com.prodio.catalog.exception;

import org.springframework.http.HttpStatus;

public enum CatalogErrorCode {

    CLIENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 클라이언트입니다."),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 제품입니다.");

    private final HttpStatus status;
    private final String message;

    CatalogErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() { return status; }
    public String getMessage() { return message; }
}

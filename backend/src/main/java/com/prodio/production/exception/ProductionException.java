package com.prodio.production.exception;

import lombok.Getter;

@Getter
public class ProductionException extends RuntimeException {

    private final ProductionErrorCode errorCode;

    public ProductionException(ProductionErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}

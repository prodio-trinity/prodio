package com.prodio.production.exception;

import com.prodio.order.exception.OrderErrorCode;
import lombok.Getter;

@Getter
public class ProductionException extends RuntimeException {

    private final ProductionErrorCode errorCode;

    public ProductionException(ProductionErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
    public ProductionException(ProductionErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}

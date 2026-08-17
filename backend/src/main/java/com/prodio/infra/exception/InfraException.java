package com.prodio.infra.exception;

import lombok.Getter;

@Getter
public class InfraException extends RuntimeException {

    private final InfraErrorCode errorCode;

    public InfraException(InfraErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}

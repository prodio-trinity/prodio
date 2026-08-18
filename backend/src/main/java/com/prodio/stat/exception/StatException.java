package com.prodio.stat.exception;

import lombok.Getter;

@Getter
public class StatException extends RuntimeException {

    private final StatErrorCode errorCode;

    public StatException(StatErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}

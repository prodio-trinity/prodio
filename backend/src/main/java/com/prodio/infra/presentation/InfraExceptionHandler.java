package com.prodio.infra.presentation;

import com.prodio.infra.exception.InfraException;
import com.prodio.shared.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class InfraExceptionHandler {

    @ExceptionHandler(InfraException.class)
    public ResponseEntity<ApiResponse<Void>> handleInfraException(InfraException e) {
        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(ApiResponse.fail(e.getMessage()));
    }
}

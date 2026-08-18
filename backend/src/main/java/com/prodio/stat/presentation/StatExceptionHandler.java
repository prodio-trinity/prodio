package com.prodio.stat.presentation;

import com.prodio.stat.exception.StatException;
import com.prodio.shared.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class StatExceptionHandler {

    @ExceptionHandler(StatException.class)
    public ResponseEntity<ApiResponse<Void>> handleStatException(StatException e) {
        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(ApiResponse.fail(e.getMessage()));
    }
}

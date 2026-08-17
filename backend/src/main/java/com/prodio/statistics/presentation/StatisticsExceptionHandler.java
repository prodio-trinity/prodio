package com.prodio.statistics.presentation;

import com.prodio.statistics.exception.StatisticsException;
import com.prodio.shared.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class StatisticsExceptionHandler {

    @ExceptionHandler(StatisticsException.class)
    public ResponseEntity<ApiResponse<Void>> handleStatisticsException(StatisticsException e) {
        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(ApiResponse.fail(e.getMessage()));
    }
}

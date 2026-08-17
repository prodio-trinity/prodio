package com.prodio.production.presentation;

import com.prodio.production.exception.ProductionException;
import com.prodio.shared.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ProductionExceptionHandler {

    @ExceptionHandler(ProductionException.class)
    public ResponseEntity<ApiResponse<Void>> handleProductionException(ProductionException e) {
        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(ApiResponse.fail(e.getMessage()));
    }
}

package com.prodio.shared;

import com.prodio.shared.error.BusinessException;
import com.prodio.shared.error.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
class ApiExceptionHandler {
    record ErrorResponse(boolean success, String errorCode, String message) {}

    @ExceptionHandler(BusinessException.class)
    ResponseEntity<ErrorResponse> handleBusinessException(BusinessException exception) {
        HttpStatus status = switch (exception.errorCode()) {
            case ADMIN_MEMBER_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case INVALID_REQUEST, ADMIN_ROLE_INVALID -> HttpStatus.BAD_REQUEST;
            case AUTH_EMAIL_ALREADY_EXISTS, ADMIN_SELF_DEMOTION -> HttpStatus.CONFLICT;
            case AUTH_INVALID_CREDENTIALS -> HttpStatus.UNAUTHORIZED;
        };
        return ResponseEntity.status(status).body(
                new ErrorResponse(false, exception.errorCode().name(), exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .findFirst()
                .orElse("입력값이 올바르지 않습니다.");
        return ResponseEntity.badRequest().body(
                new ErrorResponse(false, ErrorCode.INVALID_REQUEST.name(), message));
    }

    @ExceptionHandler(BadCredentialsException.class)
    ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new ErrorResponse(false, ErrorCode.AUTH_INVALID_CREDENTIALS.name(), "이메일 또는 비밀번호가 올바르지 않습니다."));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ErrorResponse> handleInvalidArgument(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(
                new ErrorResponse(false, ErrorCode.INVALID_REQUEST.name(), exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
        return ResponseEntity.badRequest().body(new ErrorResponse(false, ErrorCode.INVALID_REQUEST.name(),
                exception.getName() + " 값이 올바르지 않습니다."));
    }
}

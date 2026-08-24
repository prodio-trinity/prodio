package com.prodio.stat.exception;

import org.springframework.http.HttpStatus;

public enum StatErrorCode {

    STAT_NOT_FOUND(HttpStatus.NOT_FOUND, "조회 가능한 통계 데이터가 없습니다."),
    STAT_INVALID_FILTER(HttpStatus.BAD_REQUEST, "from은 to보다 늦을 수 없습니다."),
    STAT_DATE_RANGE_TOO_WIDE(HttpStatus.BAD_REQUEST, "조회 기간은 최대 1년(366일)까지 지정할 수 있습니다."),
    STAT_REQUESTER_NOT_FOUND(HttpStatus.NOT_FOUND, "요청자 정보를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String message;

    StatErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() { return status; }
    public String getMessage() { return message; }
}

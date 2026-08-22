package com.prodio.catalog.exception;

import org.springframework.http.HttpStatus;

public enum CatalogErrorCode {

    CLIENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 클라이언트입니다."),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 제품입니다."),
    DUPLICATE_BUSINESS_REG_NO(HttpStatus.CONFLICT, "이미 등록된 사업자등록번호입니다."),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "분류코드를 찾을 수 없습니다."),
    INVALID_TOP_CATEGORY(HttpStatus.BAD_REQUEST, "존재하지 않는 대분류 코드입니다."),
    DUPLICATE_SUB_CATEGORY_CODE(HttpStatus.CONFLICT, "이미 존재하는 소분류 코드입니다."),
    INVALID_EXCEL_FILE(HttpStatus.BAD_REQUEST, "엑셀 파일을 읽을 수 없습니다."),
    EXCEL_EXPORT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "엑셀 파일 생성 중 오류가 발생했습니다."),
    CLIENT_ALREADY_REGISTERED(HttpStatus.CONFLICT, "이미 등록된 거래처 계정입니다."),
    REGISTRATION_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "등록 신청을 찾을 수 없습니다."),
    REGISTRATION_ALREADY_LINKED(HttpStatus.CONFLICT, "이미 다른 계정에 연결된 거래처입니다."),
    CLIENT_ACCOUNT_NOT_FOUND(HttpStatus.UNAUTHORIZED, "계정 정보를 확인할 수 없습니다."),
    INVALID_REGISTRATION_STATUS(HttpStatus.BAD_REQUEST, "존재하지 않는 신청 상태입니다."),
    TOP_CATEGORY_CHANGE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "카테고리 변경은 같은 대분류 내에서만 가능합니다."),
    REGISTRATION_ALREADY_SUBMITTED(HttpStatus.CONFLICT, "이미 처리 중인 신청이 있습니다. 잠시 후 다시 시도해주세요.");

    private final HttpStatus status;
    private final String message;

    CatalogErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() { return status; }
    public String getMessage() { return message; }
}

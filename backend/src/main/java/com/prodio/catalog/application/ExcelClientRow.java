package com.prodio.catalog.application;

public record ExcelClientRow(
        int rowNumber,
        String companyName,
        String ceoName,
        String businessRegNo,
        String phone,
        String address,
        String managerName,
        String memo,
        String parseError
) {
    static ExcelClientRow of(int rowNumber, String companyName, String ceoName, String businessRegNo,
                             String phone, String address, String managerName, String memo) {
        return new ExcelClientRow(rowNumber, companyName, ceoName, businessRegNo, phone, address, managerName, memo, null);
    }

    static ExcelClientRow invalid(int rowNumber, String parseError) {
        return new ExcelClientRow(rowNumber, null, null, null, null, null, null, null, parseError);
    }

    boolean isValid() {
        return parseError == null;
    }
}
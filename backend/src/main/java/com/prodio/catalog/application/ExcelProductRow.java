package com.prodio.catalog.application;

import java.math.BigDecimal;

public record ExcelProductRow(
        int rowNumber,
        String productName,
        String categoryCode,
        BigDecimal unitPrice,
        String unit,
        String memo,
        String parseError
) {
    boolean isValid() {
        return parseError == null;
    }
}

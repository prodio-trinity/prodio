package com.prodio.catalog.application;

import java.util.List;

public record ExcelUploadResult(int totalRows, int successCount, int failCount, List<RowError> errors) {
    public record RowError(int row, String reason) {
    }
}
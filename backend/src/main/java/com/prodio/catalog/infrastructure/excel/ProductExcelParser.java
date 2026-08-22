package com.prodio.catalog.infrastructure.excel;

import com.prodio.catalog.application.ExcelProductRow;
import com.prodio.catalog.exception.CatalogErrorCode;
import com.prodio.catalog.exception.CatalogException;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.OldExcelFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 품목 엑셀 업로드 파싱. 신규 등록 전용
 * 컬럼 순서(A~E): 품목명*, 분류코드*, 단가*, 단위*, 비고.
 */
@Component
public class ProductExcelParser {
    private static final DataFormatter FORMATTER = new DataFormatter();

    public List<ExcelProductRow> parse(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CatalogException(CatalogErrorCode.INVALID_EXCEL_FILE);
        }

        String filename = file.getOriginalFilename();
        if (filename == null
                || (!filename.toLowerCase().endsWith(".xlsx")
                && !filename.toLowerCase().endsWith(".xls"))) {
            throw new CatalogException(CatalogErrorCode.INVALID_EXCEL_FILE);
        }

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            return parseRows(workbook.getSheetAt(0));
        } catch (IOException | EncryptedDocumentException | OldExcelFormatException e) {
            throw new CatalogException(CatalogErrorCode.INVALID_EXCEL_FILE);
        }
    }

    private List<ExcelProductRow> parseRows(Sheet sheet) {
        List<ExcelProductRow> rows = new ArrayList<>();

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            ExcelProductRow parsed = parseRow(sheet.getRow(i), i + 1);
            if (parsed != null) {
                rows.add(parsed);
            }
        }
        return rows;
    }

    private ExcelProductRow parseRow(Row row, int rowNumber) {
        if (row == null) return null;

        String productName = cell(row, 0);
        String categoryCode = cell(row, 1);
        String unitPriceRaw = cell(row, 2);
        String unit = cell(row, 3);
        String memo = cell(row, 4);

        if (productName == null && categoryCode == null && unitPriceRaw == null
                && unit == null && memo == null) {
            return null;
        }

        String parseError = validate(productName, categoryCode, unitPriceRaw, unit);
        BigDecimal unitPrice = parseError == null ? new BigDecimal(unitPriceRaw.trim()) : null;

        return new ExcelProductRow(rowNumber, productName, categoryCode, unitPrice, unit,
                memo, parseError);
    }

    private String validate(String productName, String categoryCode, String unitPriceRaw, String unit) {
        if (productName == null) return "품목명은 필수입니다.";
        if (categoryCode == null) return "분류코드는 필수입니다.";
        if (unitPriceRaw == null) return "단가는 필수입니다.";
        if (unit == null) return "단위는 필수입니다.";
        try {
            new BigDecimal(unitPriceRaw.trim());
        } catch (NumberFormatException e) {
            return "단가는 숫자여야 합니다.";
        }
        return null;
    }

    private String cell(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return null;
        String value = FORMATTER.formatCellValue(cell).trim();
        return value.isEmpty() ? null : value;
    }
}

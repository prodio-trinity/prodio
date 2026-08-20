package com.prodio.catalog.infrastructure.excel;

import com.prodio.catalog.application.ExcelClientRow;
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
import java.util.ArrayList;
import java.util.List;

/** 거래처 엑셀 업로드 파싱. 컬럼 순서(A~G): 회사명*, 대표자, 사업자등록번호, 연락처, 주소, 담당자, 비고. */
@Component
public class ClientExcelParser {
    private static final DataFormatter FORMATTER = new DataFormatter();

    public List<ExcelClientRow> parse(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CatalogException(CatalogErrorCode.INVALID_EXCEL_FILE);
        }

        // 확장자 검사
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

    private List<ExcelClientRow> parseRows(Sheet sheet) {
        List<ExcelClientRow> rows = new ArrayList<>();

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            ExcelClientRow parsed = parseRow(sheet.getRow(i), i + 1);
            if (parsed != null) {
                rows.add(parsed);
            }
        }
        return rows;
    }

    /** 회사명 누락이면 parseError 채워 반환. */
    private ExcelClientRow parseRow(Row row, int rowNumber) {
        if (row == null) return null;

        String companyName = cell(row, 0);
        String ceoName = cell(row, 1);
        String businessRegNo = cell(row, 2);
        String phone = cell(row, 3);
        String address = cell(row, 4);
        String managerName = cell(row, 5);
        String memo = cell(row, 6);

        if (companyName == null && ceoName == null && businessRegNo == null && phone == null
                && address == null && managerName == null && memo == null) {
            return null;
        }

        String parseError = (companyName == null) ? "회사명은 필수입니다." : null;
        return new ExcelClientRow(rowNumber, companyName, ceoName, businessRegNo, phone, address,
                managerName, memo, parseError);
    }

    private String cell(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return null;
        String value = FORMATTER.formatCellValue(cell).trim();
        return value.isEmpty() ? null : value;
    }
}
package com.prodio.catalog.infrastructure.excel;

import com.prodio.catalog.application.ProductListItem;
import com.prodio.catalog.exception.CatalogErrorCode;
import com.prodio.catalog.exception.CatalogException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** 품목 목록 엑셀 내보내기 */
@Component
public class ProductExcelWriter {
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final String[] HEADERS = {
            "품목코드", "품목명", "대분류", "소분류", "분류코드", "단위", "단가", "비고", "사용여부", "등록일"
    };
    private static final DateTimeFormatter CREATED_AT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public byte[] write(List<ProductListItem> products) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("품목");
            writeHeader(sheet);
            for (int i = 0; i < products.size(); i++) {
                writeRow(sheet.createRow(i + 1), products.get(i));
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new CatalogException(CatalogErrorCode.EXCEL_EXPORT_FAILED);
        }
    }

    private void writeHeader(Sheet sheet) {
        Row header = sheet.createRow(0);
        for (int i = 0; i < HEADERS.length; i++) {
            header.createCell(i).setCellValue(HEADERS[i]);
        }
    }

    private void writeRow(Row row, ProductListItem product) {
        int col = 0;
        setCell(row, col++, product.productCode());
        setCell(row, col++, product.productName());
        setCell(row, col++, product.topCategoryDisplayName());
        setCell(row, col++, product.subCategoryName());
        setCell(row, col++, product.subCategoryCode());
        setCell(row, col++, product.unit());
        setCell(row, col++, product.unitPrice() == null ? null : product.unitPrice().toPlainString());
        setCell(row, col++, product.memo());
        setCell(row, col++, product.active() ? "사용" : "미사용");
        setCell(row, col, product.createdAt() == null
                ? null
                : CREATED_AT_FORMAT.format(product.createdAt().atZone(KST)));
    }

    private void setCell(Row row, int col, String value) {
        Cell cell = row.createCell(col);
        if (value != null) cell.setCellValue(value);
    }
}
package com.prodio.catalog.infrastructure.excel;

import com.prodio.catalog.application.ClientListItem;
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

/** 거래처 목록 엑셀 내보내기 */
@Component
public class ClientExcelWriter {
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final String[] HEADERS = {
            "거래처코드", "회사명", "대표자", "연락처", "주소", "담당자",
            "사용여부", "계정연동여부", "사업자등록번호", "비고", "등록일"
    };
    private static final DateTimeFormatter CREATED_AT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public byte[] write(List<ClientListItem> clients) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("거래처");
            writeHeader(sheet);
            for (int i = 0; i < clients.size(); i++) {
                writeRow(sheet.createRow(i + 1), clients.get(i));
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray(); // 엑셀 파일 내용 byte[] 반환
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

    private void writeRow(Row row, ClientListItem client) {
        int col = 0;
        setCell(row, col++, client.clientCode());
        setCell(row, col++, client.companyName());
        setCell(row, col++, client.ceoName());
        setCell(row, col++, client.phone());
        setCell(row, col++, client.address());
        setCell(row, col++, client.managerName());
        setCell(row, col++, client.active() ? "사용" : "미사용");
        setCell(row, col++, client.linkedToAccount() ? "연동" : "미연동");
        setCell(row, col++, client.businessRegNo());
        setCell(row, col++, client.memo());
        setCell(row, col, client.createdAt() == null
                ? null
                : CREATED_AT_FORMAT.format(client.createdAt().atZone(KST)));
    }

    private void setCell(Row row, int col, String value) {
        Cell cell = row.createCell(col);
        if (value != null) cell.setCellValue(value);
    }
}
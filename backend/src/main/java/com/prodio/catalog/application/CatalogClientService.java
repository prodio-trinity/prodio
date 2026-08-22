package com.prodio.catalog.application;

import com.prodio.catalog.domain.CatalogClient;
import com.prodio.catalog.exception.CatalogException;
import com.prodio.catalog.infrastructure.excel.ClientExcelParser;
import com.prodio.catalog.infrastructure.excel.ClientExcelWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CatalogClientService {
    private final CatalogClientQueryRepository queryRepository;
    private final CatalogClientRepository clientRepository;
    private final CatalogClientRowUpsertService rowUpsertService;
    private final ClientExcelParser clientExcelParser;
    private final ClientExcelWriter clientExcelWriter;

    public Page<ClientListItem> getClientList(String keyword, Boolean isActive, int page, int size, String sort) {
        return queryRepository.findClients(normalize(keyword), isActive, page, size, sort);
    }

    public byte[] exportClients(String keyword, Boolean isActive, String sort) {
        List<ClientListItem> clients = queryRepository.findAllForExport(normalize(keyword), isActive, sort);
        return clientExcelWriter.write(clients);
    }

    public List<ClientAutocompleteItem> getAutocomplete(String keyword, int size) {
        return queryRepository.findClientsForAutocomplete(normalize(keyword), size);
    }

    public List<ClientBulkUpsertResult> upsertRows(List<ClientBulkUpsertRequest> requests) {
        List<ClientBulkUpsertResult> results = new ArrayList<>();
        for (int i = 0; i < requests.size(); i++) {
            results.add(upsertRow(i, requests.get(i)));
        }
        return results;
    }

    private ClientBulkUpsertResult upsertRow(int index, ClientBulkUpsertRequest req) {
        try {
            CatalogClient saved = rowUpsertService.upsertOne(req);
            return ClientBulkUpsertResult.success(index, saved.id(), saved.clientCode());
        } catch (CatalogException | IllegalArgumentException e) {
            return ClientBulkUpsertResult.failure(index, e.getMessage());
        } catch (DataIntegrityViolationException e) {
            return ClientBulkUpsertResult.failure(index, "저장 중 데이터 제약 조건 위반이 발생했습니다.");
        }
    }

    public ExcelUploadResult upsertRowsFromExcel(MultipartFile file) {
        List<ExcelClientRow> rows = clientExcelParser.parse(file);
        List<ExcelClientRow> validRows = rows.stream()
                .filter(ExcelClientRow::isValid)
                .toList();

        Set<String> duplicateRegNos = findDuplicateBusinessRegNos(validRows);
        List<ExcelClientRow> processableRows = validRows.stream()
                .filter(row -> row.businessRegNo() == null || !duplicateRegNos.contains(row.businessRegNo()))
                .toList();

        Set<String> businessRegNos = processableRows.stream()
                .map(ExcelClientRow::businessRegNo)
                .filter(regNo -> regNo != null)
                .collect(Collectors.toSet());
        Map<String, Long> existingIds = clientRepository.findIdsByBusinessRegNoIn(businessRegNos);

        List<ClientBulkUpsertRequest> requests = processableRows.stream()
                .map(row -> toBulkUpsertRequest(row, existingIds))
                .toList();
        List<ClientBulkUpsertResult> results = upsertRows(requests);

        List<ExcelUploadResult.RowError> errors = new ArrayList<>();
        errors.addAll(collectParseErrors(rows));
        errors.addAll(collectDuplicateErrors(validRows, duplicateRegNos));
        errors.addAll(collectUpsertErrors(processableRows, results));

        int totalRows = rows.size();
        int failCount = errors.size();
        return new ExcelUploadResult(totalRows, totalRows - failCount, failCount, errors);
    }

    private Set<String> findDuplicateBusinessRegNos(List<ExcelClientRow> rows) {
        Set<String> seen = new HashSet<>();
        Set<String> duplicates = new HashSet<>();

        for (ExcelClientRow row : rows) {
            String regNo = row.businessRegNo();
            if (regNo != null && !seen.add(regNo)) {
                duplicates.add(regNo);
            }
        }

        return duplicates;
    }

    private List<ExcelUploadResult.RowError> collectDuplicateErrors(
            List<ExcelClientRow> validRows, Set<String> duplicateRegNos) {
        List<ExcelUploadResult.RowError> errors = new ArrayList<>();
        for (ExcelClientRow row : validRows) {
            if (row.businessRegNo() != null && duplicateRegNos.contains(row.businessRegNo())) {
                errors.add(new ExcelUploadResult.RowError(row.rowNumber(),
                        "사업자등록번호가 파일 내에서 중복됩니다: " + row.businessRegNo()));
            }
        }
        return errors;
    }

    private List<ExcelUploadResult.RowError> collectParseErrors(List<ExcelClientRow> rows) {
        List<ExcelUploadResult.RowError> errors = new ArrayList<>();
        for (ExcelClientRow row : rows) {
            if (!row.isValid()) {
                errors.add(new ExcelUploadResult.RowError(row.rowNumber(), row.parseError()));
            }
        }
        return errors;
    }

    private List<ExcelUploadResult.RowError> collectUpsertErrors(
            List<ExcelClientRow> validRows, List<ClientBulkUpsertResult> results) {
        List<ExcelUploadResult.RowError> errors = new ArrayList<>();
        for (ClientBulkUpsertResult result : results) {
            if (!result.success()) {
                ExcelClientRow row = validRows.get(result.index());
                errors.add(new ExcelUploadResult.RowError(row.rowNumber(), result.reason()));
            }
        }
        return errors;
    }

    private ClientBulkUpsertRequest toBulkUpsertRequest(ExcelClientRow row, Map<String, Long> existingIds) {
        Long id = existingIds.get(row.businessRegNo());
        return new ClientBulkUpsertRequest(id, row.companyName(), row.ceoName(), row.businessRegNo(),
                row.phone(), row.address(), row.managerName(), row.memo(), null);
    }

    private static String normalize(String keyword) {
        if (keyword == null) return null;
        String trimmed = keyword.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
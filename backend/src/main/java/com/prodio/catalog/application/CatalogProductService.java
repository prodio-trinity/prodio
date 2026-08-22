package com.prodio.catalog.application;

import com.prodio.catalog.domain.CatalogProduct;
import com.prodio.catalog.exception.CatalogException;
import com.prodio.catalog.infrastructure.excel.ProductExcelParser;
import com.prodio.catalog.infrastructure.excel.ProductExcelWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogProductService {
    private final CatalogProductQueryRepository queryRepository;
    private final CatalogProductRowUpsertService rowUpsertService;
    private final ProductExcelParser productExcelParser;
    private final ProductExcelWriter productExcelWriter;

    public Page<ProductListItem> getProductList(String keyword, Long categoryId, Boolean isActive,
            int page, int size, String sort) {
        return queryRepository.findProducts(normalize(keyword), categoryId, isActive, page, size, sort);
    }

    public byte[] exportProducts(String keyword, Long categoryId, Boolean isActive, String sort) {
        List<ProductListItem> products = queryRepository.findAllForExport(normalize(keyword), categoryId, isActive, sort);
        return productExcelWriter.write(products);
    }

    public List<ProductBulkUpsertResult> upsertRows(List<ProductBulkUpsertRequest> requests) {
        List<ProductBulkUpsertResult> results = new ArrayList<>();
        for (int i = 0; i < requests.size(); i++) {
            results.add(upsertRow(i, requests.get(i)));
        }
        return results;
    }

    private ProductBulkUpsertResult upsertRow(int index, ProductBulkUpsertRequest req) {
        try {
            CatalogProduct saved = rowUpsertService.upsertOne(req);
            return ProductBulkUpsertResult.success(index, saved.id(), saved.productCode());
        } catch (CatalogException | IllegalArgumentException e) {
            return ProductBulkUpsertResult.failure(index, e.getMessage());
        } catch (DataIntegrityViolationException e) {
            return ProductBulkUpsertResult.failure(index, "저장 중 데이터 제약 조건 위반이 발생했습니다.");
        }
    }

    private static String normalize(String keyword) {
        if (keyword == null) return null;
        String trimmed = keyword.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public ExcelUploadResult upsertRowsFromExcel(MultipartFile file) {
        List<ExcelProductRow> rows = productExcelParser.parse(file);
        List<ExcelProductRow> validRows = rows.stream()
                .filter(ExcelProductRow::isValid)
                .toList();

        List<ProductBulkUpsertRequest> requests = validRows.stream()
                .map(row -> toBulkUpsertRequest(row))
                .toList();
        List<ProductBulkUpsertResult> results = upsertRows(requests);

        List<ExcelUploadResult.RowError> errors = new ArrayList<>();
        errors.addAll(collectParseErrors(rows));
        errors.addAll(collectUpsertErrors(validRows, results));

        int totalRows = rows.size();
        int failCount = errors.size();
        return new ExcelUploadResult(totalRows, totalRows - failCount, failCount, errors);
    }

    private List<ExcelUploadResult.RowError> collectParseErrors(List<ExcelProductRow> rows) {
        List<ExcelUploadResult.RowError> errors = new ArrayList<>();
        for (ExcelProductRow row : rows) {
            if (!row.isValid()) {
                errors.add(new ExcelUploadResult.RowError(row.rowNumber(), row.parseError()));
            }
        }
        return errors;
    }

    private List<ExcelUploadResult.RowError> collectUpsertErrors(
            List<ExcelProductRow> validRows, List<ProductBulkUpsertResult> results) {
        List<ExcelUploadResult.RowError> errors = new ArrayList<>();
        for (ProductBulkUpsertResult result : results) {
            if (!result.success()) {
                ExcelProductRow row = validRows.get(result.index());
                errors.add(new ExcelUploadResult.RowError(row.rowNumber(), result.reason()));
            }
        }
        return errors;
    }

    private ProductBulkUpsertRequest toBulkUpsertRequest(ExcelProductRow row) {
        return new ProductBulkUpsertRequest(null, row.productName(), row.categoryCode(), row.unitPrice(),
                row.unit(), row.memo(), null);
    }
}

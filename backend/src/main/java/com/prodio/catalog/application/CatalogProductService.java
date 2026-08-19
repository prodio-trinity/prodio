package com.prodio.catalog.application;

import com.prodio.catalog.domain.CatalogProduct;
import com.prodio.catalog.exception.CatalogException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogProductService {
    private final CatalogProductQueryRepository queryRepository;
    private final CatalogProductRowUpsertService rowUpsertService;

    public Page<ProductListItem> getProductList(String keyword, Long categoryId, Boolean isActive,
            int page, int size, String sort) {
        return queryRepository.findProducts(normalize(keyword), categoryId, isActive, page, size, sort);
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
}
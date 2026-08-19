package com.prodio.catalog.application;

import com.prodio.catalog.domain.CatalogClient;
import com.prodio.catalog.exception.CatalogException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogClientService {
    private final CatalogClientQueryRepository queryRepository;
    private final CatalogClientRowUpsertService rowUpsertService;

    public Page<ClientListItem> getClientList(String keyword, Boolean isActive, int page, int size, String sort) {
        return queryRepository.findClients(normalize(keyword), isActive, page, size, sort);
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

    private static String normalize(String keyword) {
        if (keyword == null) return null;
        String trimmed = keyword.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
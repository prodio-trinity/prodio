package com.prodio.catalog.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogClientService {
    private final CatalogClientQueryRepository queryRepository;

    public Page<ClientListItem> getClientList(String keyword, Boolean isActive, int page, int size, String sort) {
        return queryRepository.findClients(normalize(keyword), isActive, page, size, sort);
    }

    public List<ClientAutocompleteItem> getAutocomplete(String keyword, int size) {
        return queryRepository.findClientsForAutocomplete(normalize(keyword), size);
    }

    private static String normalize(String keyword) {
        if (keyword == null) return null;
        String trimmed = keyword.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
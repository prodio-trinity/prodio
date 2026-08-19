package com.prodio.catalog.infrastructure.persistence;

import com.prodio.catalog.application.CatalogClientQueryRepository;
import com.prodio.catalog.application.ClientAutocompleteItem;
import com.prodio.catalog.application.ClientListItem;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
class JpaCatalogClientQueryRepository implements CatalogClientQueryRepository {
    private static final Set<String> SORTABLE_FIELDS = Set.of("companyName", "createdAt");

    private final SpringDataCatalogClientRepository springDataCatalogClientRepository;

    @Override
    public Page<ClientListItem> findClients(String keyword, Boolean isActive, int page, int size, String sort) {
        Pageable pageable = PageRequest.of(page, size, parseSort(sort));
        return springDataCatalogClientRepository.findClients(keyword, isActive, pageable)
                .map(JpaCatalogClientQueryRepository::toListItem);
    }

    @Override
    public List<ClientAutocompleteItem> findClientsForAutocomplete(String keyword, int size) {
        return springDataCatalogClientRepository
                .findActiveClients(keyword, PageRequest.of(0, size)).stream()
                .map(JpaCatalogClientQueryRepository::toAutocompleteItem)
                .toList();
    }

    private static Sort parseSort(String sort) {
        if (sort == null || sort.isBlank())
            return Sort.by(Sort.Direction.ASC, "companyName");

        String[] parts = sort.split(",", 2);
        String property = parts[0].trim();

        if (!SORTABLE_FIELDS.contains(property))
            return Sort.by(Sort.Direction.ASC, "companyName");

        Sort.Direction direction = parts.length > 1 && "desc".equalsIgnoreCase(parts[1].trim())
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        return Sort.by(direction, property);
    }

    private static ClientListItem toListItem(CatalogClientEntity entity) {
        return new ClientListItem(
                entity.getId(),
                entity.getClientCode(),
                entity.getCompanyName(),
                entity.getCeoName(),
                entity.getBusinessRegNo(),
                entity.getPhone(),
                entity.getAddress(),
                entity.getManagerName(),
                entity.getMemo(),
                entity.isActive(),
                entity.getUserId() != null,
                entity.getCreatedAt()
        );
    }

    private static ClientAutocompleteItem toAutocompleteItem(CatalogClientEntity entity) {
        return new ClientAutocompleteItem(
                entity.getId(),
                entity.getCompanyName(),
                entity.getCeoName(),
                entity.getPhone(),
                entity.getAddress(),
                entity.getManagerName()
        );
    }
}
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
    private static final Set<String> SORTABLE_FIELDS = Set.of("companyName", "clientCode", "createdAt");

    private final SpringDataCatalogClientRepository springDataCatalogClientRepository;

    @Override
    public Page<ClientListItem> findClients(String keyword, Boolean isActive, int page, int size, String sort) {
        Pageable pageable = PageRequest.of(page, size, parseSort(sort));
        return springDataCatalogClientRepository.findClients(escapeLike(keyword), isActive, pageable)
                .map(JpaCatalogClientQueryRepository::toListItem);
    }

    @Override
    public List<ClientAutocompleteItem> findClientsForAutocomplete(String keyword, int size) {
        Pageable pageable = PageRequest.of(0, size, Sort.by(Sort.Direction.ASC, "companyName"));
        return springDataCatalogClientRepository
                .findClientsForAutocomplete(escapeLike(keyword), true, pageable).stream()
                .map(JpaCatalogClientQueryRepository::toAutocompleteItem)
                .toList();
    }

    private static String escapeLike(String value) {
        if (value == null) return null;
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    private static Sort parseSort(String sort) {
        if (sort == null || sort.isBlank())
            return Sort.by(Sort.Direction.ASC, "clientCode");

        String[] parts = sort.split(",", 2);
        String property = parts[0].trim();

        if (!SORTABLE_FIELDS.contains(property))
            return Sort.by(Sort.Direction.ASC, "clientCode");

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
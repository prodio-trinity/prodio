package com.prodio.catalog.application;

import org.springframework.data.domain.Page;

import java.util.List;

public interface CatalogClientQueryRepository {
    Page<ClientListItem> findClients(String keyword, Boolean isActive, int page, int size, String sort);

    /** keyword가 companyName/ceoName/phone/address/managerName 중 하나라도 매칭되면 포함. */
    List<ClientAutocompleteItem> findClientsForAutocomplete(String keyword, int size);
}
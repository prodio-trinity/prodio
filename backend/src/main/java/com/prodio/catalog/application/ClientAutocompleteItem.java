package com.prodio.catalog.application;

public record ClientAutocompleteItem(
        long id,
        String companyName,
        String ceoName,
        String phone,
        String address,
        String managerName
) {
}
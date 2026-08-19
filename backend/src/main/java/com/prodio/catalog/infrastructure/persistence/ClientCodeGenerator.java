package com.prodio.catalog.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class ClientCodeGenerator {
    private final SpringDataCatalogClientRepository springDataCatalogClientRepository;

    String next() {
        Long seq = springDataCatalogClientRepository.nextClientCodeSequence();
        return "CL-" + String.format("%06d", seq);
    }
}
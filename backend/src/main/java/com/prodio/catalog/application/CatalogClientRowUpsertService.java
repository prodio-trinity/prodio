package com.prodio.catalog.application;

import com.prodio.catalog.domain.CatalogClient;
import com.prodio.catalog.exception.CatalogErrorCode;
import com.prodio.catalog.exception.CatalogException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class CatalogClientRowUpsertService {
    private final CatalogClientRepository clientRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CatalogClient upsertOne(ClientBulkUpsertRequest req) {
        CatalogClient client = (req.id() == null) ? buildNewClient(req) : buildUpdatedClient(req);
        validateBusinessRegNoNotDuplicate(req.businessRegNo(), client.id());
        return clientRepository.save(client);
    }

    private CatalogClient buildNewClient(ClientBulkUpsertRequest req) {
        boolean active = req.isActive() == null ? true : req.isActive();
        return new CatalogClient(null, null, req.companyName(), req.ceoName(), req.businessRegNo(),
                req.phone(), req.address(), req.managerName(), null, req.memo(), active);
    }

    private CatalogClient buildUpdatedClient(ClientBulkUpsertRequest req) {
        CatalogClient client = clientRepository.findById(req.id())
                .orElseThrow(() -> new CatalogException(CatalogErrorCode.CLIENT_NOT_FOUND));
        boolean active = req.isActive() == null ? client.active() : req.isActive();
        return client.update(req.companyName(), req.ceoName(), req.businessRegNo(),
                req.phone(), req.address(), req.managerName(), req.memo(), active);
    }

    private void validateBusinessRegNoNotDuplicate(String businessRegNo, Long excludeId) {
        if (businessRegNo == null || businessRegNo.isBlank()) return;
        clientRepository.findByBusinessRegNo(businessRegNo)
                .filter(existing -> !existing.id().equals(excludeId)) // 자기자신은 제외
                .ifPresent(existing -> {
                    throw new CatalogException(CatalogErrorCode.DUPLICATE_BUSINESS_REG_NO);
                });
    }
}
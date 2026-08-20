package com.prodio.catalog.application;

import com.prodio.catalog.ClientMemoEvent;
import com.prodio.catalog.domain.CatalogClient;
import com.prodio.catalog.exception.CatalogErrorCode;
import com.prodio.catalog.exception.CatalogException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
class CatalogClientRowUpsertService {
    private final CatalogClientRepository clientRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CatalogClient upsertOne(ClientBulkUpsertRequest req) {
        // 기존 거래처 찾기
        CatalogClient existing = null;
        if (req.id() != null) {
            existing = clientRepository.findById(req.id())
                    .orElseThrow(() -> new CatalogException(CatalogErrorCode.CLIENT_NOT_FOUND));
        }
        // 기존 메모
        String previousMemo = existing != null ? existing.memo() : null;

        CatalogClient client;   
        // 신규 OR 수정 분기
        if (existing == null) {
            client = buildNewClient(req);
        } else {
            client = buildUpdatedClient(existing, req);
        }
        
        validateBusinessRegNoNotDuplicate(req.businessRegNo(), client.id());
        CatalogClient saved = clientRepository.save(client);

        if (!Objects.equals(previousMemo, saved.memo())) {
            eventPublisher.publishEvent(
                    new ClientMemoEvent(saved.id(), saved.companyName(), saved.memo())
            );
        }

        return saved;
    }

    private CatalogClient buildNewClient(ClientBulkUpsertRequest req) {
        boolean active = req.isActive() == null ? true : req.isActive();
        return new CatalogClient(null, null, req.companyName(), req.ceoName(), req.businessRegNo(),
                req.phone(), req.address(), req.managerName(), null, req.memo(), active);
    }

    private CatalogClient buildUpdatedClient(CatalogClient existing, ClientBulkUpsertRequest req) {
        boolean active = req.isActive() == null ? existing.active() : req.isActive();
        return existing.update(req.companyName(), req.ceoName(), req.businessRegNo(),
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
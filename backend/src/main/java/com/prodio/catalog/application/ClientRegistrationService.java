package com.prodio.catalog.application;

import com.prodio.catalog.ClientRegistrationApproved;
import com.prodio.catalog.domain.CatalogClient;
import com.prodio.catalog.domain.ClientRegistration;
import com.prodio.catalog.domain.RegistrationStatus;
import com.prodio.catalog.exception.CatalogErrorCode;
import com.prodio.catalog.exception.CatalogException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClientRegistrationService {
    private final CatalogClientRepository clientRepository;
    private final ClientRegistrationRepository registrationRepository;
    private final ApplicationEventPublisher eventPublisher;

    /** 1. 승인된 거래처(카탈로그) 존재 2.신청 존재(PENDING/REJECTED) 3.둘 다 없으면 미등록(empty). */
    @Transactional(readOnly = true)
    public Optional<ClientRegistrationStatusResult> getMyStatus(long userId) {
        // 이미 승인되어 실제 거래처로 등록된 사용자인지 확인
        CatalogClient approved = clientRepository.findByUserId(userId).orElse(null);
        if (approved != null) {
            return Optional.of(ClientRegistrationStatusResult.approved(approved));
        }

        // 이미 승인된 거래처 없다면 등록 신청 내역 있는지 확인
        ClientRegistration request = registrationRepository.findByUserId(userId).orElse(null);
        if (request == null) {
            return Optional.empty();
        }
        return Optional.of(ClientRegistrationStatusResult.fromRequest(request));
    }

    /** 거래처 등록 신청
     userId 기준 upsert — 있으면 덮어쓰고 PENDING으로 리셋, 없으면 신규 신청. */
    @Transactional
    public ClientRegistration submit(long userId, String companyName, String ceoName,
            String businessRegNo, String phone, String address, String managerName) {
        // 이미 승인된 거래처인지 확인
        if (clientRepository.findByUserId(userId).isPresent()) {
            throw new CatalogException(CatalogErrorCode.CLIENT_ALREADY_REGISTERED);
        }
        // 기존 신청 내역 확인
        ClientRegistration existing = registrationRepository.findByUserId(userId).orElse(null);

        ClientRegistration toSave;
        if (existing == null) {
            toSave = ClientRegistration.submit(userId, companyName, ceoName, businessRegNo, phone, address, managerName);
        } else {
            toSave = existing.resubmit(companyName, ceoName, businessRegNo, phone, address, managerName);
        }

        return registrationRepository.save(toSave);
    }

    /** status가 null이면 전체 조회 */
    @Transactional(readOnly = true)
    public List<ClientRegistration> getRegistrationList(RegistrationStatus status) {
        return registrationRepository.findAll(status);
    }

    /** 관리자 신청 승인
     businessRegNo로 기존 ADMIN 행에 매칭되면 userId만 연결, 아니면 신청 내용으로 신규 생성. */
    @Transactional
    public CatalogClient approve(long requestId) {
        ClientRegistration request = registrationRepository.findById(requestId)
                .orElseThrow(() -> new CatalogException(CatalogErrorCode.REGISTRATION_REQUEST_NOT_FOUND));

        CatalogClient matched = clientRepository.findByBusinessRegNo(request.businessRegNo()).orElse(null);

        CatalogClient linked;
        if (matched == null) {
            linked = createFromRequest(request);
        } else {
            linked = linkExisting(matched, request.userId());
        }

        registrationRepository.save(request.approve());
        eventPublisher.publishEvent(new ClientRegistrationApproved(request.userId()));
        return linked;
    }

    @Transactional
    public void reject(long requestId, String reason) {
        ClientRegistration request = registrationRepository.findById(requestId)
                .orElseThrow(() -> new CatalogException(CatalogErrorCode.REGISTRATION_REQUEST_NOT_FOUND));
        registrationRepository.save(request.reject(reason));
    }

    private CatalogClient linkExisting(CatalogClient existing, long userId) {
        if (existing.userId() != null) {
            throw new CatalogException(CatalogErrorCode.REGISTRATION_ALREADY_LINKED);
        }
        return clientRepository.save(existing.linkUser(userId));
    }

    private CatalogClient createFromRequest(ClientRegistration request) {
        CatalogClient created = CatalogClient.register(null, request.companyName(), request.ceoName(),
                request.businessRegNo(), request.phone(), request.address(), request.managerName(),
                request.userId(), null);
        return clientRepository.save(created);
    }
}
package com.prodio.catalog.application;

import com.prodio.catalog.domain.CatalogClient;
import com.prodio.catalog.domain.ClientRegistration;
import com.prodio.catalog.domain.RegistrationStatus;

/** GET /me 응답용 읽기 모델. 등록 이력 자체가 없으면 Optional.empty()로 표현 */
public record ClientRegistrationStatusResult(
        RegistrationStatus status,
        String companyName,
        String ceoName,
        String businessRegNo,
        String phone,
        String address,
        String managerName,
        String rejectReason
) {
    static ClientRegistrationStatusResult approved(CatalogClient client) {
        return new ClientRegistrationStatusResult(RegistrationStatus.APPROVED, client.companyName(),
                client.ceoName(), client.businessRegNo(), client.phone(), client.address(),
                client.managerName(), null);
    }

    static ClientRegistrationStatusResult fromRequest(ClientRegistration request) {
        return new ClientRegistrationStatusResult(request.status(), request.companyName(), request.ceoName(),
                request.businessRegNo(), request.phone(), request.address(), request.managerName(),
                request.rejectReason());
    }
}

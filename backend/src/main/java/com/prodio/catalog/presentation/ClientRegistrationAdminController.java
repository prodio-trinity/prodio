package com.prodio.catalog.presentation;

import com.prodio.catalog.application.ClientRegistrationService;
import com.prodio.catalog.domain.CatalogClient;
import com.prodio.catalog.domain.ClientRegistration;
import com.prodio.catalog.domain.RegistrationStatus;
import com.prodio.catalog.exception.CatalogErrorCode;
import com.prodio.catalog.exception.CatalogException;
import com.prodio.shared.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/admin/catalog/clients/registration-requests")
@RequiredArgsConstructor
@Validated
class ClientRegistrationAdminController {
    private final ClientRegistrationService clientRegistrationService;

    @GetMapping
    ApiResponse<List<RegistrationRequestResponse>> getRegistrationRequests(
            @RequestParam(defaultValue = "PENDING") String status) {
        List<ClientRegistration> requests = clientRegistrationService.getRegistrationList(parseStatus(status));
        return ApiResponse.success(requests.stream().map(RegistrationRequestResponse::from).toList());
    }

    @PatchMapping("/{id}/approve")
    ApiResponse<ApproveResponse> approve(@PathVariable Long id) {
        CatalogClient linked = clientRegistrationService.approve(id);
        return ApiResponse.success("등록 신청을 승인했습니다.", ApproveResponse.from(linked));
    }

    @PatchMapping("/{id}/reject")
    ApiResponse<Void> reject(@PathVariable Long id, @Valid @RequestBody RejectRequest request) {
        clientRegistrationService.reject(id, request.reason());
        return ApiResponse.success("등록 신청을 반려했습니다.", null);
    }

    private RegistrationStatus parseStatus(String value) {
        try {
            return RegistrationStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CatalogException(CatalogErrorCode.INVALID_REGISTRATION_STATUS);
        }
    }

    record RejectRequest(@NotBlank String reason) {
    }

    record ApproveResponse(String clientId, String clientCode, String companyName) {
        static ApproveResponse from(CatalogClient client) {
            return new ApproveResponse(String.valueOf(client.id()), client.clientCode(), client.companyName());
        }
    }

    record RegistrationRequestResponse(String id, String companyName, String ceoName, String businessRegNo,
            String phone, String address, String managerName, String status, String rejectReason,
            Instant createdAt, Instant reviewedAt) {
        static RegistrationRequestResponse from(ClientRegistration request) {
            return new RegistrationRequestResponse(String.valueOf(request.id()), request.companyName(),
                    request.ceoName(), request.businessRegNo(), request.phone(), request.address(),
                    request.managerName(), request.status().name(), request.rejectReason(),
                    request.createdAt(), request.reviewedAt());
        }
    }
}
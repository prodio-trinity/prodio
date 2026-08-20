package com.prodio.catalog.presentation;

import com.prodio.catalog.application.CatalogClientService;
import com.prodio.catalog.application.ClientAutocompleteItem;
import com.prodio.catalog.application.ClientRegistrationService;
import com.prodio.catalog.application.ClientRegistrationStatusResult;
import com.prodio.catalog.exception.CatalogErrorCode;
import com.prodio.catalog.exception.CatalogException;
import com.prodio.shared.ApiResponse;
import com.prodio.user.UserDirectory;
import com.prodio.user.UserRef;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/catalog/clients")
@RequiredArgsConstructor
@Validated
class CatalogClientController {
    private final CatalogClientService catalogClientService;
    private final ClientRegistrationService clientRegistrationService;
    private final UserDirectory userDirectory;

    @GetMapping("/autocomplete")
    ApiResponse<List<ClientAutocompleteResponse>> getClientAutocomplete(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "8") @Min(1) @Max(50) int size) {
        List<ClientAutocompleteItem> items = catalogClientService.getAutocomplete(keyword, size);
        return ApiResponse.success(items.stream()
                .map(ClientAutocompleteResponse::from).toList());
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('PENDING', 'CLIENT')")
    ApiResponse<MyRegistrationResponse> getMyRegistration(Authentication authentication) {
        UserRef user = currentUser(authentication);
        return ApiResponse.success(clientRegistrationService.getMyStatus(user.id())
                .map(MyRegistrationResponse::from)
                .orElseGet(MyRegistrationResponse::notRegistered));
    }

    @PostMapping("/me/registration-requests")
    @PreAuthorize("hasRole('PENDING')")
    ApiResponse<MyRegistrationResponse> submitRegistration(
            @Valid @RequestBody RegistrationSubmitRequest request, Authentication authentication) {
        UserRef user = currentUser(authentication);
        ClientRegistrationStatusResult saved = clientRegistrationService.submit(user.id(),
                request.companyName(), request.ceoName(), request.businessRegNo(),
                request.phone(), request.address(), request.managerName());
        return ApiResponse.success("등록 신청이 접수되었습니다.", MyRegistrationResponse.from(saved));
    }

    private UserRef currentUser(Authentication authentication) {
        return userDirectory.findActiveByEmail(authentication.getName())
                .orElseThrow(() -> new CatalogException(CatalogErrorCode.CLIENT_ACCOUNT_NOT_FOUND));
    }

    record ClientAutocompleteResponse(Long id, String companyName, String ceoName, String phone,
            String address, String managerName) {
        static ClientAutocompleteResponse from(ClientAutocompleteItem item) {
            return new ClientAutocompleteResponse(item.id(), item.companyName(), item.ceoName(),
                    item.phone(), item.address(), item.managerName());
        }
    }

    record RegistrationSubmitRequest(@NotBlank String companyName, String ceoName,
            @NotBlank String businessRegNo, String phone, String address, String managerName) {
    }

    record MyRegistrationResponse(boolean registered, String status, String companyName, String ceoName,
            String businessRegNo, String phone, String address, String managerName, String rejectReason) {
        static MyRegistrationResponse notRegistered() {
            return new MyRegistrationResponse(false, null, null, null, null, null, null, null, null);
        }

        static MyRegistrationResponse from(ClientRegistrationStatusResult result) {
            return new MyRegistrationResponse(true, result.status().name(), result.companyName(),
                    result.ceoName(), result.businessRegNo(), result.phone(), result.address(),
                    result.managerName(), result.rejectReason());
        }
    }
}
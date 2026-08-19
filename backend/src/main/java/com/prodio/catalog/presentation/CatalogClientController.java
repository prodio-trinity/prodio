package com.prodio.catalog.presentation;

import com.prodio.catalog.application.CatalogClientService;
import com.prodio.catalog.application.ClientAutocompleteItem;
import com.prodio.catalog.application.ClientBulkUpsertRequest;
import com.prodio.catalog.application.ClientBulkUpsertResult;
import com.prodio.catalog.application.ClientListItem;
import com.prodio.shared.ApiResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/catalog/clients")
@RequiredArgsConstructor
@Validated
class CatalogClientController {
    private final CatalogClientService catalogClientService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<ClientPageResponse> getClientList(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "") String sort) {
        Page<ClientListItem> result = catalogClientService.getClientList(keyword, isActive, page, size, sort);
        return ApiResponse.success(ClientPageResponse.from(result));
    }

    @GetMapping("/autocomplete")
    @PreAuthorize("hasAnyRole('ADMIN','CLIENT')")
    ApiResponse<List<ClientAutocompleteResponse>> getClientAutocomplete(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "8") @Min(1) @Max(50) int size) {
        List<ClientAutocompleteItem> items = catalogClientService.getAutocomplete(keyword, size);
        return ApiResponse.success(items.stream()
                .map(ClientAutocompleteResponse::from).toList());
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<List<ClientBulkUpsertResult>> upsertClients(@RequestBody List<ClientBulkUpsertRequest> requests) {
        return ApiResponse.success(catalogClientService.upsertRows(requests));
    }

    record ClientListItemResponse(Long id, String clientCode, String companyName, String ceoName,
            String businessRegNo, String phone, String address, String managerName, String memo,
            boolean isActive, boolean linkedToAccount, Instant createdAt) {
        static ClientListItemResponse from(ClientListItem item) {
            return new ClientListItemResponse(item.id(), item.clientCode(), item.companyName(),
                    item.ceoName(), item.businessRegNo(), item.phone(), item.address(),
                    item.managerName(), item.memo(), item.active(), item.linkedToAccount(),
                    item.createdAt());
        }
    }

    record ClientPageResponse(List<ClientListItemResponse> clients, int page, int size,
            long totalElements, int totalPages) {
        static ClientPageResponse from(Page<ClientListItem> page) {
            List<ClientListItemResponse> clients = page.getContent().stream()
                    .map(ClientListItemResponse::from).toList();
            return new ClientPageResponse(clients, page.getNumber(), page.getSize(),
                    page.getTotalElements(), page.getTotalPages());
        }
    }

    record ClientAutocompleteResponse(Long id, String companyName, String ceoName, String phone,
            String address, String managerName) {
        static ClientAutocompleteResponse from(ClientAutocompleteItem item) {
            return new ClientAutocompleteResponse(item.id(), item.companyName(), item.ceoName(),
                    item.phone(), item.address(), item.managerName());
        }
    }
}
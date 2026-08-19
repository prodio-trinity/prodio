package com.prodio.catalog.presentation;

import com.prodio.catalog.application.CatalogClientService;
import com.prodio.catalog.application.ClientAutocompleteItem;
import com.prodio.shared.ApiResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
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

    @GetMapping("/autocomplete")
    ApiResponse<List<ClientAutocompleteResponse>> getClientAutocomplete(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "8") @Min(1) @Max(50) int size) {
        List<ClientAutocompleteItem> items = catalogClientService.getAutocomplete(keyword, size);
        return ApiResponse.success(items.stream()
                .map(ClientAutocompleteResponse::from).toList());
    }

    record ClientAutocompleteResponse(Long id, String companyName, String ceoName, String phone,
            String address, String managerName) {
        static ClientAutocompleteResponse from(ClientAutocompleteItem item) {
            return new ClientAutocompleteResponse(item.id(), item.companyName(), item.ceoName(),
                    item.phone(), item.address(), item.managerName());
        }
    }
}
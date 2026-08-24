package com.prodio.production.presentation;

import com.prodio.catalog.CatalogOrderLookup;
import com.prodio.production.application.ProductionService;
import com.prodio.production.domain.ProductionRecord;
import com.prodio.production.exception.ProductionErrorCode;
import com.prodio.production.exception.ProductionException;
import com.prodio.shared.ApiResponse;
import com.prodio.user.UserDirectory;
import com.prodio.user.UserRef;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/production")
public class ProductionController {
    private final ProductionService service;
    private final UserDirectory userDirectory;
    private final CatalogOrderLookup catalogOrderLookup;

    @GetMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    ApiResponse<ProductionResponse> productionInfo(@PathVariable String orderId, Authentication authentication) {
        ProductionRecord productionInfo = service.getProductionInfoByOrderId(parseId(orderId));
        // 관리자는 소유권 상관없이 전체 열람 가능. 고객은 본인 소유 주문인지 검증.
        if (!isAdmin(authentication)) {
            long clientId = currentClientId(authentication);
            if (productionInfo.clientId() == null || productionInfo.clientId() != clientId) {
                throw new ProductionException(ProductionErrorCode.PRODUCTION_NOT_FOUND);
            }
        }
        return ApiResponse.success(ProductionResponse.from(productionInfo));
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }

    /** 로그인 계정 → 연결된 거래처 id. 계정이 거래처랑 연결 안 돼있으면 자기 소유가 있을 수 없으니 바로 막는다. */
    private long currentClientId(Authentication authentication) {
        UserRef user = userDirectory.findActiveByEmail(authentication.getName())
                .orElseThrow(() -> new ProductionException(ProductionErrorCode.PRODUCTION_NOT_FOUND));
        return catalogOrderLookup.findClientByAccountId(user.id())
                .map(CatalogOrderLookup.ClientSnapshot::id)
                .orElseThrow(() -> new ProductionException(ProductionErrorCode.PRODUCTION_NOT_FOUND));
    }

    private long parseId(String value) {
        try {
            long id = Long.parseLong(value);
            if (id <= 0) throw new NumberFormatException();
            return id;
        } catch (NumberFormatException exception) {
            throw new ProductionException(ProductionErrorCode.INVALID_PRODUCTION_REQUEST, "식별자가 올바르지 않습니다.");
        }
    }
}

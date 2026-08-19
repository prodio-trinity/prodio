package com.prodio.production.presentation;

import com.prodio.production.application.ProductionService;
import com.prodio.shared.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/production")
public class ProductionAdminController {
    private final ProductionService productService;

    record ActionResponse(boolean smsSent) {}

    @PatchMapping("/{productionId}/ship")
    ApiResponse<ActionResponse> ship(@PathVariable Long productionId) {
        boolean smsSent = productService.ship(productionId);
        return ApiResponse.success(new ActionResponse(smsSent));
    }

    @PatchMapping("/{productionId}/complete")
    ApiResponse<ActionResponse> complete(@PathVariable Long productionId) {
        boolean smsSent = productService.complete(productionId);
        return ApiResponse.success(new ActionResponse(smsSent));
    }
}

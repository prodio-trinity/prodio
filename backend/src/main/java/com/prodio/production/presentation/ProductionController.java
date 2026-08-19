package com.prodio.production.presentation;

import com.prodio.production.application.ProductionService;
import com.prodio.shared.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/production")
public class ProductionController {
    private final ProductionService productService;

    record ShipResponse(boolean smsSent) {}

    @PatchMapping("/{productionId}/ship")
    ApiResponse<ShipResponse> ship(@PathVariable Long productionId) {
        boolean smsSent = productService.ship(productionId);
        return ApiResponse.success(new ShipResponse(smsSent));
    }
}

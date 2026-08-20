package com.prodio.production.presentation;

import com.prodio.production.application.ProductionService;
import com.prodio.production.application.ProductionStatus;
import com.prodio.production.domain.ProductionRecord;
import com.prodio.production.exception.ProductionErrorCode;
import com.prodio.production.exception.ProductionException;
import com.prodio.shared.ApiResponse;
import com.prodio.shared.PageResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/production")
@Validated
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

    @GetMapping
    ApiResponse<PageResult<ProductionResponse>> list(@RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        ProductionStatus parsedStatus = parseStatus(status);
        PageResult<ProductionRecord> result = productService.list(parsedStatus, page, size);
        return ApiResponse.success(result.map(ProductionResponse::from));
    }

    @PatchMapping("/{productionId}/memo")
    ApiResponse<Void> memo(@PathVariable Long productionId, @Valid @RequestBody MemoRequest request){
        productService.addMemo(productionId, request.memo());
        return ApiResponse.success(null);
    }

    private ProductionStatus parseStatus(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return ProductionStatus.from(value);
        } catch (IllegalArgumentException e) {
            throw new ProductionException(ProductionErrorCode.INVALID_PRODUCTION_STATUS);
        }
    }

    record MemoRequest(@NotBlank @Size(max = 2000) String memo) {}
}

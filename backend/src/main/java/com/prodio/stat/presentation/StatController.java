package com.prodio.stat.presentation;

import com.prodio.shared.ApiResponse;
import com.prodio.stat.application.StatDashboardService;
import com.prodio.stat.domain.DailyProduction;
import com.prodio.stat.domain.DashboardSummary;
import com.prodio.stat.domain.ProductDistribution;
import com.prodio.stat.domain.StatFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
class StatController {

    private final StatDashboardService statDashboardService;

    @GetMapping("/dashboard")
    ApiResponse<DashboardResponse> dashboard(
        @RequestParam(required = false) LocalDate from,
        @RequestParam(required = false) LocalDate to,
        @RequestParam(required = false) String status
    ) {
        StatFilter filter = new StatFilter(from, to, StatFilterSupport.parseStatus(status));

        return ApiResponse.success(DashboardResponse.from(statDashboardService.getDashboard(filter)));
    }

    @GetMapping("/products")
    ApiResponse<List<ProductDistributionResponse>> products(
        @RequestParam(required = false) LocalDate from,
        @RequestParam(required = false) LocalDate to,
        @RequestParam(required = false) String status
    ) {
        StatFilter filter = new StatFilter(from, to, StatFilterSupport.parseStatus(status));
        List<ProductDistributionResponse> response = statDashboardService.getProductDistribution(filter).stream()
                .map(ProductDistributionResponse::from)
                .toList();

        return ApiResponse.success(response);
    }

    @GetMapping("/dashboard/daily")
    ApiResponse<List<DailyProductionResponse>> dailyProduction(
        @RequestParam(required = false) LocalDate from,
        @RequestParam(required = false) LocalDate to,
        @RequestParam(required = false) String status
    ) {
        StatFilter filter = new StatFilter(from, to, StatFilterSupport.parseStatus(status));
        List<DailyProductionResponse> response = statDashboardService.getDailyProduction(filter).stream()
                .map(DailyProductionResponse::from)
                .toList();

        return ApiResponse.success(response);
    }

    record DashboardResponse(
        long pendingCount,
        long inProductionCount,
        long inDeliveryCount,
        long completedCount,
        long cancelledCount,
        long totalCount,
        long completedQuantity
    ) {
        static DashboardResponse from(DashboardSummary summary) {
            return new DashboardResponse(
                summary.pendingCount(),
                summary.inProductionCount(),
                summary.inDeliveryCount(),
                summary.completedCount(),
                summary.cancelledCount(),
                summary.totalCount(),
                summary.completedQuantity()
            );
        }
    }

    record DailyProductionResponse(
        LocalDate date,
        long quantity
    ) {
        static DailyProductionResponse from(DailyProduction daily) {
            return new DailyProductionResponse(daily.date(), daily.quantity());
        }
    }

    record ProductDistributionResponse(
        String productId,
        String productName,
        long orderCount,
        long totalQuantity
    ) {
        static ProductDistributionResponse from(ProductDistribution distribution) {
            return new ProductDistributionResponse(
                Long.toString(distribution.productId()),
                distribution.productName(),
                distribution.orderCount(),
                distribution.totalQuantity()
            );
        }
    }
}

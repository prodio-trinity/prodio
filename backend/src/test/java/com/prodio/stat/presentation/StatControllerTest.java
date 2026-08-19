package com.prodio.stat.presentation;

import com.prodio.shared.ApiResponse;
import com.prodio.stat.application.StatDashboardService;
import com.prodio.stat.domain.DashboardSummary;
import com.prodio.stat.domain.OrderViewStatus;
import com.prodio.stat.domain.ProductDistribution;
import com.prodio.stat.domain.StatFilter;
import com.prodio.stat.exception.StatErrorCode;
import com.prodio.stat.exception.StatException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("StatController")
class StatControllerTest {

    @Mock private StatDashboardService statDashboardService;
    private StatController controller;

    @BeforeEach
    void setUp() {
        controller = new StatController(statDashboardService);
    }

    @Test
    @DisplayName("쿼리 파라미터로 StatFilter를 만들어 대시보드를 조회한다")
    void dashboardBuildsFilterFromQueryParams() {
        LocalDate from = LocalDate.parse("2026-08-01");
        LocalDate to = LocalDate.parse("2026-08-31");
        DashboardSummary summary = new DashboardSummary(1, 2, 3, 4, 1, 11, 40);
        when(statDashboardService.getDashboard(new StatFilter(from, to, OrderViewStatus.COMPLETED)))
                .thenReturn(summary);

        ApiResponse<StatController.DashboardResponse> response = controller.dashboard(from, to, "completed");

        assertThat(response.data()).isEqualTo(new StatController.DashboardResponse(1, 2, 3, 4, 1, 11, 40));
    }

    @Test
    @DisplayName("품목별 분포 조회 시 productId를 문자열로 변환한다")
    void productsConvertsProductIdToString() {
        when(statDashboardService.getProductDistribution(new StatFilter(null, null, null)))
                .thenReturn(List.of(new ProductDistribution(7L, "정밀 샤프트", 3, 30)));

        ApiResponse<List<StatController.ProductDistributionResponse>> response =
                controller.products(null, null, null);

        assertThat(response.data())
                .containsExactly(new StatController.ProductDistributionResponse("7", "정밀 샤프트", 3, 30));
    }

    @Test
    @DisplayName("알 수 없는 status 값이면 예외를 던진다")
    void unknownStatusThrows() {
        assertThatThrownBy(() -> controller.dashboard(null, null, "알수없음"))
                .isInstanceOfSatisfying(StatException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(StatErrorCode.STAT_INVALID_FILTER));
    }
}

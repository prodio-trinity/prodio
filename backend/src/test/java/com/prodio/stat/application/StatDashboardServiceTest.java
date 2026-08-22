package com.prodio.stat.application;

import com.prodio.stat.domain.DailyProduction;
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
@DisplayName("StatDashboardService")
class StatDashboardServiceTest {

    @Mock private StatDashboardRepository repository;
    private StatDashboardService service;

    @BeforeEach
    void setUp() {
        service = new StatDashboardService(repository);
    }

    @Test
    @DisplayName("필터를 그대로 레포지토리에 전달해 대시보드 요약을 조회한다")
    void getDashboardDelegatesToRepository() {
        StatFilter filter = new StatFilter(LocalDate.parse("2026-08-01"), LocalDate.parse("2026-08-31"), null);
        DashboardSummary summary = new DashboardSummary(1, 2, 3, 4, 1, 11, 40);
        when(repository.summarize(filter)).thenReturn(summary);

        DashboardSummary result = service.getDashboard(filter);

        assertThat(result).isSameAs(summary);
    }

    @Test
    @DisplayName("필터를 그대로 레포지토리에 전달해 품목별 분포를 조회한다")
    void getProductDistributionDelegatesToRepository() {
        StatFilter filter = new StatFilter(null, null, OrderViewStatus.COMPLETED);
        List<ProductDistribution> distributions = List.of(new ProductDistribution(1L, "정밀 샤프트", 5, 50));
        when(repository.productDistribution(filter)).thenReturn(distributions);

        List<ProductDistribution> result = service.getProductDistribution(filter);

        assertThat(result).isSameAs(distributions);
    }

    @Test
    @DisplayName("필터를 그대로 레포지토리에 전달해 일별 생산량을 조회한다")
    void getDailyProductionDelegatesToRepository() {
        StatFilter filter = new StatFilter(LocalDate.parse("2026-08-01"), LocalDate.parse("2026-08-07"), null);
        List<DailyProduction> daily = List.of(new DailyProduction(LocalDate.parse("2026-08-01"), 10));
        when(repository.dailyProduction(filter)).thenReturn(daily);

        List<DailyProduction> result = service.getDailyProduction(filter);

        assertThat(result).isSameAs(daily);
    }

    @Test
    @DisplayName("from이 to보다 늦으면 일별 생산량 조회 시 예외를 던진다")
    void getDailyProductionRejectsFromAfterTo() {
        StatFilter filter = new StatFilter(LocalDate.parse("2026-09-01"), LocalDate.parse("2026-08-01"), null);

        assertThatThrownBy(() -> service.getDailyProduction(filter))
                .isInstanceOfSatisfying(StatException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(StatErrorCode.STAT_INVALID_FILTER));
    }

    @Test
    @DisplayName("from이 to보다 늦으면 대시보드 조회 시 예외를 던진다")
    void getDashboardRejectsFromAfterTo() {
        StatFilter filter = new StatFilter(LocalDate.parse("2026-09-01"), LocalDate.parse("2026-08-01"), null);

        assertThatThrownBy(() -> service.getDashboard(filter))
                .isInstanceOfSatisfying(StatException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(StatErrorCode.STAT_INVALID_FILTER));
    }

    @Test
    @DisplayName("from이 to보다 늦으면 품목별 분포 조회 시 예외를 던진다")
    void getProductDistributionRejectsFromAfterTo() {
        StatFilter filter = new StatFilter(LocalDate.parse("2026-09-01"), LocalDate.parse("2026-08-01"), null);

        assertThatThrownBy(() -> service.getProductDistribution(filter))
                .isInstanceOfSatisfying(StatException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(StatErrorCode.STAT_INVALID_FILTER));
    }
}

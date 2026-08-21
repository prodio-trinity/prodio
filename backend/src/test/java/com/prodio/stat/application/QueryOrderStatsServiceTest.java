package com.prodio.stat.application;

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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("QueryOrderStatsService")
class QueryOrderStatsServiceTest {

    @Mock private StatDashboardRepository statDashboardRepository;
    private QueryOrderStatsService service;

    @BeforeEach
    void setUp() {
        service = new QueryOrderStatsService(statDashboardRepository);
    }

    @Test
    @DisplayName("from/to/status 문자열을 파싱해 필터로 조회하고 결과를 텍스트로 정리한다")
    void parsesArgsAndFormatsResult() {
        StatFilter expectedFilter = new StatFilter(
                LocalDate.parse("2026-07-01"), LocalDate.parse("2026-07-31"), OrderViewStatus.COMPLETED);
        DashboardSummary summary = new DashboardSummary(1, 2, 3, 4, 0, 10, 40);
        List<ProductDistribution> distribution = List.of(new ProductDistribution(7L, "정밀 샤프트", 3, 30));
        when(statDashboardRepository.summarize(eq(expectedFilter))).thenReturn(summary);
        when(statDashboardRepository.productDistribution(eq(expectedFilter))).thenReturn(distribution);

        String result = service.queryOrderStats("2026-07-01", "2026-07-31", "completed");

        assertThat(result)
                .contains("조회 조건: 2026-07-01 ~ 2026-07-31, status=COMPLETED")
                .contains("대기: 1건")
                .contains("생산중: 2건")
                .contains("배송중: 3건")
                .contains("완료: 4건")
                .contains("취소: 0건")
                .contains("전체: 10건")
                .contains("완료 생산량: 40")
                .contains("품목별 분포:")
                .contains("- 정밀 샤프트: 3건, 수량 30");
    }

    @Test
    @DisplayName("from/to/status가 비어있으면 전체 조회 필터로 처리한다")
    void treatsBlankArgsAsUnfiltered() {
        StatFilter expectedFilter = new StatFilter(null, null, null);
        when(statDashboardRepository.summarize(eq(expectedFilter)))
                .thenReturn(new DashboardSummary(0, 0, 0, 0, 0, 0, 0));
        when(statDashboardRepository.productDistribution(eq(expectedFilter))).thenReturn(List.of());

        String result = service.queryOrderStats(null, "", "  ");

        assertThat(result).contains("조회 조건: 전체 ~ 전체");
        assertThat(result).doesNotContain("status=");
    }

    @Test
    @DisplayName("품목 분포가 없으면 품목별 분포 섹션을 생략한다")
    void omitsDistributionSectionWhenEmpty() {
        StatFilter expectedFilter = new StatFilter(null, null, null);
        when(statDashboardRepository.summarize(eq(expectedFilter)))
                .thenReturn(new DashboardSummary(0, 0, 0, 0, 0, 0, 0));
        when(statDashboardRepository.productDistribution(eq(expectedFilter))).thenReturn(List.of());

        String result = service.queryOrderStats(null, null, null);

        assertThat(result).doesNotContain("품목별 분포:");
    }

    @Test
    @DisplayName("status 문자열이 유효하지 않으면 STAT_INVALID_FILTER 예외를 던지고 조회하지 않는다")
    void rejectsInvalidStatus() {
        assertThatThrownBy(() -> service.queryOrderStats(null, null, "완료"))
                .isInstanceOfSatisfying(StatException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(StatErrorCode.STAT_INVALID_FILTER));
        verifyNoInteractions(statDashboardRepository);
    }

    @Test
    @DisplayName("날짜 형식이 잘못되면 STAT_INVALID_FILTER 예외를 던지고 조회하지 않는다")
    void rejectsInvalidDateFormat() {
        assertThatThrownBy(() -> service.queryOrderStats("2026/07/01", null, null))
                .isInstanceOfSatisfying(StatException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(StatErrorCode.STAT_INVALID_FILTER));
        verifyNoInteractions(statDashboardRepository);
    }

    @Test
    @DisplayName("from이 to보다 늦으면 조회 없이 예외를 던진다")
    void rejectsFromAfterTo() {
        assertThatThrownBy(() -> service.queryOrderStats("2026-09-01", "2026-08-01", null))
                .isInstanceOfSatisfying(StatException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(StatErrorCode.STAT_INVALID_FILTER));
        verifyNoInteractions(statDashboardRepository);
    }
}

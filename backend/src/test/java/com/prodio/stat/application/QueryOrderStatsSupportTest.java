package com.prodio.stat.application;

import com.prodio.stat.domain.DashboardSummary;
import com.prodio.stat.domain.OrderViewStatus;
import com.prodio.stat.domain.ProductDistribution;
import com.prodio.stat.domain.StatFilter;
import com.prodio.stat.exception.StatErrorCode;
import com.prodio.stat.exception.StatException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("QueryOrderStatsSupport")
class QueryOrderStatsSupportTest {

    @Test
    @DisplayName("null/공백 문자열은 날짜 없음으로 처리한다")
    void parseDateTreatsBlankAsNull() {
        assertThat(QueryOrderStatsSupport.parseDate(null)).isNull();
        assertThat(QueryOrderStatsSupport.parseDate("  ")).isNull();
    }

    @Test
    @DisplayName("유효한 날짜 문자열을 LocalDate로 파싱한다")
    void parseDateParsesValidDate() {
        assertThat(QueryOrderStatsSupport.parseDate("2026-07-01")).isEqualTo(LocalDate.parse("2026-07-01"));
    }

    @Test
    @DisplayName("잘못된 날짜 형식이면 STAT_INVALID_FILTER 예외를 던진다")
    void parseDateRejectsInvalidFormat() {
        assertThatThrownBy(() -> QueryOrderStatsSupport.parseDate("2026/07/01"))
                .isInstanceOfSatisfying(StatException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(StatErrorCode.STAT_INVALID_FILTER));
    }

    @Test
    @DisplayName("null/공백 문자열은 status 없음으로 처리한다")
    void parseStatusTreatsBlankAsNull() {
        assertThat(QueryOrderStatsSupport.parseStatus(null)).isNull();
        assertThat(QueryOrderStatsSupport.parseStatus(" ")).isNull();
    }

    @Test
    @DisplayName("대소문자 구분 없이 유효한 status를 파싱한다")
    void parseStatusParsesCaseInsensitively() {
        assertThat(QueryOrderStatsSupport.parseStatus("completed")).isEqualTo(OrderViewStatus.COMPLETED);
    }

    @Test
    @DisplayName("존재하지 않는 status 값이면 STAT_INVALID_FILTER 예외를 던진다")
    void parseStatusRejectsInvalidValue() {
        assertThatThrownBy(() -> QueryOrderStatsSupport.parseStatus("완료"))
                .isInstanceOfSatisfying(StatException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(StatErrorCode.STAT_INVALID_FILTER));
    }

    @Test
    @DisplayName("from이 to보다 늦으면 예외를 던진다")
    void validateRejectsFromAfterTo() {
        StatFilter filter = new StatFilter(LocalDate.parse("2026-09-01"), LocalDate.parse("2026-08-01"), null);

        assertThatThrownBy(() -> QueryOrderStatsSupport.validate(filter))
                .isInstanceOfSatisfying(StatException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(StatErrorCode.STAT_INVALID_FILTER));
    }

    @Test
    @DisplayName("from/to가 없거나 순서가 올바르면 예외를 던지지 않는다")
    void validateAllowsValidRange() {
        assertThatCode(() -> QueryOrderStatsSupport.validate(new StatFilter(null, null, null)))
                .doesNotThrowAnyException();
        assertThatCode(() -> QueryOrderStatsSupport.validate(
                new StatFilter(LocalDate.parse("2026-07-01"), LocalDate.parse("2026-07-31"), null)))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("집계와 품목 분포를 텍스트로 정리한다")
    void formatBuildsSummaryText() {
        StatFilter filter = new StatFilter(LocalDate.parse("2026-07-01"), LocalDate.parse("2026-07-31"), OrderViewStatus.COMPLETED);
        DashboardSummary summary = new DashboardSummary(1, 2, 3, 4, 0, 10, 40);
        List<ProductDistribution> distribution = List.of(new ProductDistribution(7L, "정밀 샤프트", 3, 30));

        String result = QueryOrderStatsSupport.format(filter, summary, distribution);

        assertThat(result)
                .contains("조회 조건: 2026-07-01 ~ 2026-07-31, status=COMPLETED")
                .contains("완료: 4건")
                .contains("전체: 10건")
                .contains("완료 생산량: 40")
                .contains("품목별 분포:")
                .contains("- 정밀 샤프트: 3건, 수량 30");
    }

    @Test
    @DisplayName("품목 분포가 없으면 품목별 분포 섹션을 생략한다")
    void formatOmitsDistributionSectionWhenEmpty() {
        StatFilter filter = new StatFilter(null, null, null);
        DashboardSummary summary = new DashboardSummary(0, 0, 0, 0, 0, 0, 0);

        String result = QueryOrderStatsSupport.format(filter, summary, List.of());

        assertThat(result).contains("조회 조건: 전체 ~ 전체").doesNotContain("품목별 분포:");
    }
}

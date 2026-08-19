package com.prodio.stat.application;

import com.prodio.stat.domain.AiQueryLog;
import com.prodio.stat.domain.DashboardSummary;
import com.prodio.stat.domain.ProductDistribution;
import com.prodio.stat.domain.QueryType;
import com.prodio.stat.domain.StatFilter;
import com.prodio.stat.exception.StatErrorCode;
import com.prodio.stat.exception.StatException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("StatSummaryService")
class StatSummaryServiceTest {

    @Mock private StatDashboardRepository statDashboardRepository;
    @Mock private AiClient aiClient;
    @Mock private AiQueryLogRepository aiQueryLogRepository;
    private StatSummaryService service;

    @BeforeEach
    void setUp() {
        service = new StatSummaryService(statDashboardRepository, aiClient, aiQueryLogRepository);
    }

    @Test
    @DisplayName("집계 데이터를 요약 프롬프트로 만들어 AI 응답을 STATS_SUMMARY 로그로 저장한다")
    void summarizeGeneratesAndSavesLog() {
        StatFilter filter = new StatFilter(LocalDate.parse("2026-08-01"), LocalDate.parse("2026-08-31"), null);
        DashboardSummary summary = new DashboardSummary(1, 2, 3, 4, 1, 11, 40);
        List<ProductDistribution> distribution = List.of(new ProductDistribution(7L, "정밀 샤프트", 3, 30));
        when(statDashboardRepository.summarize(filter)).thenReturn(summary);
        when(statDashboardRepository.productDistribution(filter)).thenReturn(distribution);
        when(aiClient.generateText(any())).thenReturn("이번 달 주문은 총 11건입니다.");
        AiQueryLog saved = new AiQueryLog(null, QueryType.STATS_SUMMARY, null,
                "2026-08-01 ~ 2026-08-31", "이번 달 주문은 총 11건입니다.", null);
        when(aiQueryLogRepository.save(any())).thenReturn(saved);

        AiQueryLog result = service.summarize(filter);

        assertThat(result).isSameAs(saved);
        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        verify(aiClient).generateText(promptCaptor.capture());
        assertThat(promptCaptor.getValue())
                .contains("정밀 샤프트")
                .contains("11")
                .contains("2026-08-01 ~ 2026-08-31");

        ArgumentCaptor<AiQueryLog> logCaptor = ArgumentCaptor.forClass(AiQueryLog.class);
        verify(aiQueryLogRepository).save(logCaptor.capture());
        assertThat(logCaptor.getValue().queryType()).isEqualTo(QueryType.STATS_SUMMARY);
        assertThat(logCaptor.getValue().sourceType()).isNull();
        assertThat(logCaptor.getValue().question()).isEqualTo("2026-08-01 ~ 2026-08-31");
        assertThat(logCaptor.getValue().response()).isEqualTo("이번 달 주문은 총 11건입니다.");
    }

    @Test
    @DisplayName("from이 to보다 늦으면 AI 호출 없이 예외를 던진다")
    void summarizeRejectsFromAfterTo() {
        StatFilter filter = new StatFilter(LocalDate.parse("2026-09-01"), LocalDate.parse("2026-08-01"), null);

        assertThatThrownBy(() -> service.summarize(filter))
                .isInstanceOfSatisfying(StatException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(StatErrorCode.STAT_INVALID_FILTER));
        verifyNoInteractions(aiClient, aiQueryLogRepository);
        verify(statDashboardRepository, never()).summarize(any());
    }

    @Test
    @DisplayName("STATS_SUMMARY 타입으로 로그 페이지를 조회한다")
    void getSummaryLogsDelegatesToRepository() {
        AiQueryLogPage page = new AiQueryLogPage(List.of(), 0, 10, 0);
        when(aiQueryLogRepository.findPage(QueryType.STATS_SUMMARY, 0, 10)).thenReturn(page);

        AiQueryLogPage result = service.getSummaryLogs(0, 10);

        assertThat(result).isSameAs(page);
    }
}

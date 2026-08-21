package com.prodio.stat.application;

import com.prodio.infra.exception.InfraErrorCode;
import com.prodio.infra.exception.InfraException;
import com.prodio.stat.domain.AiQueryLog;
import com.prodio.stat.domain.DashboardSummary;
import com.prodio.stat.domain.OrderViewStatus;
import com.prodio.stat.domain.ProductDistribution;
import com.prodio.stat.domain.QueryType;
import com.prodio.stat.domain.SourceType;
import com.prodio.stat.domain.StatFilter;
import com.prodio.stat.embedding.application.ClientEmbeddingRepository;
import com.prodio.stat.embedding.application.EmbeddingMatch;
import com.prodio.stat.embedding.application.OrderEmbeddingRepository;
import com.prodio.stat.embedding.application.ProductionEmbeddingRepository;
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
import java.util.Map;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RagQaService")
class RagQaServiceTest {

    @Mock private AiClient aiClient;
    @Mock private OrderEmbeddingRepository orderEmbeddingRepository;
    @Mock private ClientEmbeddingRepository clientEmbeddingRepository;
    @Mock private ProductionEmbeddingRepository productionEmbeddingRepository;
    @Mock private StatDashboardRepository statDashboardRepository;
    @Mock private AiQueryLogRepository aiQueryLogRepository;
    private RagQaService service;

    private final float[] queryVector = {0.1f, 0.2f};

    @BeforeEach
    void setUp() {
        service = new RagQaService(aiClient, orderEmbeddingRepository, clientEmbeddingRepository,
                productionEmbeddingRepository, statDashboardRepository, aiQueryLogRepository);
    }

    private void stubSaveReturnsArgument() {
        when(aiQueryLogRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private void simulateToolCalls(String finalAnswer, ToolCall... calls) {
        when(aiClient.ask(any(), any(), any())).thenAnswer(invocation -> {
            Function<ToolCall, String> executor = invocation.getArgument(2);
            for (ToolCall call : calls) {
                executor.apply(call);
            }
            return finalAnswer;
        });
    }

    @Test
    @DisplayName("searchNotes가 호출되면 질문을 임베딩해 해당 sourceType만 검색하고 그 sourceType을 로그에 남긴다")
    void executesSearchNotesForSingleSourceType() {
        stubSaveReturnsArgument();
        when(aiClient.embed("납기 지연")).thenReturn(queryVector);
        when(orderEmbeddingRepository.search(queryVector, 5))
                .thenReturn(List.of(new EmbeddingMatch(1L, "10월 15일까지 납기 요청", 0.1)));
        simulateToolCalls("납기는 10월 15일입니다.",
                new ToolCall("searchNotes", Map.of("query", "납기 지연", "sourceType", "ORDER_NOTE")));

        AiQueryLog result = service.ask(42L, "납기 언제야?");

        verify(orderEmbeddingRepository).search(queryVector, 5);
        verifyNoInteractions(clientEmbeddingRepository, productionEmbeddingRepository, statDashboardRepository);
        ArgumentCaptor<AiQueryLog> captor = ArgumentCaptor.forClass(AiQueryLog.class);
        verify(aiQueryLogRepository).save(captor.capture());
        assertThat(captor.getValue().queryType()).isEqualTo(QueryType.RAG_QA);
        assertThat(captor.getValue().sourceType()).isEqualTo(SourceType.ORDER_NOTE);
        assertThat(captor.getValue().requestedBy()).isEqualTo(42L);
        assertThat(captor.getValue().question()).isEqualTo("납기 언제야?");
        assertThat(result.response()).isEqualTo("납기는 10월 15일입니다.");
    }

    @Test
    @DisplayName("sourceType이 ALL이면 세 임베딩 레포지토리를 모두 같은 질문 벡터로 검색한다")
    void searchesAllThreeSourcesWhenSourceTypeIsAll() {
        stubSaveReturnsArgument();
        when(aiClient.embed("최근 이슈")).thenReturn(queryVector);
        when(orderEmbeddingRepository.search(any(), eq(5))).thenReturn(List.of());
        when(clientEmbeddingRepository.search(any(), eq(5))).thenReturn(List.of());
        when(productionEmbeddingRepository.search(any(), eq(5))).thenReturn(List.of());
        simulateToolCalls("정리된 답변",
                new ToolCall("searchNotes", Map.of("query", "최근 이슈", "sourceType", "ALL")));

        service.ask(42L, "최근 이슈 정리해줘");

        verify(orderEmbeddingRepository).search(queryVector, 5);
        verify(clientEmbeddingRepository).search(queryVector, 5);
        verify(productionEmbeddingRepository).search(queryVector, 5);
    }

    @Test
    @DisplayName("sourceType 인자를 생략하면 ALL로 취급해 세 소스를 모두 검색한다")
    void defaultsToAllWhenSourceTypeArgMissing() {
        stubSaveReturnsArgument();
        when(aiClient.embed("지연")).thenReturn(queryVector);
        when(orderEmbeddingRepository.search(any(), eq(5))).thenReturn(List.of());
        when(clientEmbeddingRepository.search(any(), eq(5))).thenReturn(List.of());
        when(productionEmbeddingRepository.search(any(), eq(5))).thenReturn(List.of());
        simulateToolCalls("답변", new ToolCall("searchNotes", Map.of("query", "지연")));

        AiQueryLog result = service.ask(42L, "질문");

        assertThat(result.sourceType()).isEqualTo(SourceType.ALL);
    }

    @Test
    @DisplayName("queryOrderStats가 호출되면 파싱된 필터로 StatDashboardRepository를 조회하고, sourceType 없이 기록한다")
    void executesQueryOrderStatsWithParsedFilter() {
        stubSaveReturnsArgument();
        StatFilter expectedFilter = new StatFilter(
                LocalDate.parse("2026-07-01"), LocalDate.parse("2026-07-31"), OrderViewStatus.COMPLETED);
        when(statDashboardRepository.summarize(eq(expectedFilter)))
                .thenReturn(new DashboardSummary(1, 2, 3, 4, 0, 10, 40));
        when(statDashboardRepository.productDistribution(eq(expectedFilter))).thenReturn(List.of());
        simulateToolCalls("7월 매출은 3200만원입니다.",
                new ToolCall("queryOrderStats", Map.of("from", "2026-07-01", "to", "2026-07-31", "status", "completed")));

        AiQueryLog result = service.ask(42L, "7월 매출 얼마야?");

        verify(statDashboardRepository).summarize(expectedFilter);
        verify(statDashboardRepository).productDistribution(expectedFilter);
        verifyNoInteractions(orderEmbeddingRepository, clientEmbeddingRepository, productionEmbeddingRepository);
        assertThat(result.sourceType()).isNull();
    }

    @Test
    @DisplayName("queryOrderStats에 잘못된 status가 오면 예외를 던지고 조회하지 않는다")
    void rejectsInvalidStatusForQueryOrderStats() {
        simulateToolCalls("무시됨", new ToolCall("queryOrderStats", Map.of("status", "완료")));

        assertThatThrownBy(() -> service.ask(42L, "질문"))
                .isInstanceOfSatisfying(StatException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(StatErrorCode.STAT_INVALID_FILTER));
        verifyNoInteractions(statDashboardRepository, aiQueryLogRepository);
    }

    @Test
    @DisplayName("서로 다른 sourceType으로 searchNotes가 여러 번 쓰이면 ALL로 기록한다")
    void recordsAllWhenMultipleSourceTypesUsed() {
        stubSaveReturnsArgument();
        when(aiClient.embed(any())).thenReturn(queryVector);
        when(orderEmbeddingRepository.search(any(), eq(5))).thenReturn(List.of());
        when(clientEmbeddingRepository.search(any(), eq(5))).thenReturn(List.of());
        simulateToolCalls("종합 답변",
                new ToolCall("searchNotes", Map.of("query", "지연", "sourceType", "ORDER_NOTE")),
                new ToolCall("searchNotes", Map.of("query", "불만", "sourceType", "CLIENT_MEMO")));

        AiQueryLog result = service.ask(42L, "최근 이슈 정리해줘");

        assertThat(result.sourceType()).isEqualTo(SourceType.ALL);
    }

    @Test
    @DisplayName("도구를 전혀 쓰지 않으면 sourceType 없이 로그를 저장한다")
    void recordsNullSourceTypeWhenNoToolUsed() {
        stubSaveReturnsArgument();
        simulateToolCalls("안녕하세요! 무엇을 도와드릴까요?");

        AiQueryLog result = service.ask(42L, "안녕");

        assertThat(result.sourceType()).isNull();
        verifyNoInteractions(orderEmbeddingRepository, clientEmbeddingRepository, productionEmbeddingRepository, statDashboardRepository);
    }

    @Test
    @DisplayName("알 수 없는 도구 이름이 오면 AI_REQUEST_FAILED 예외를 던지고 로그를 저장하지 않는다")
    void throwsOnUnknownToolName() {
        simulateToolCalls("무시됨", new ToolCall("deleteEverything", Map.of()));

        assertThatThrownBy(() -> service.ask(42L, "질문"))
                .isInstanceOfSatisfying(InfraException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(InfraErrorCode.AI_REQUEST_FAILED));
        verifyNoInteractions(aiQueryLogRepository);
    }

    @Test
    @DisplayName("RAG_QA 타입으로 로그 페이지를 조회한다")
    void getAskLogsDelegatesToRepository() {
        AiQueryLogPage page = new AiQueryLogPage(List.of(), 0, 10, 0);
        when(aiQueryLogRepository.findPage(42L, QueryType.RAG_QA, 0, 10)).thenReturn(page);

        AiQueryLogPage result = service.getAskLogs(42L, 0, 10);

        assertThat(result).isSameAs(page);
    }
}

package com.prodio.stat.application;

import com.prodio.infra.exception.InfraErrorCode;
import com.prodio.infra.exception.InfraException;
import com.prodio.stat.domain.AiQueryLog;
import com.prodio.stat.domain.QueryType;
import com.prodio.stat.domain.SourceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    @Mock private SearchNotesService searchNotesService;
    @Mock private QueryOrderStatsService queryOrderStatsService;
    @Mock private AiQueryLogRepository aiQueryLogRepository;
    private RagQaService service;

    @BeforeEach
    void setUp() {
        service = new RagQaService(aiClient, searchNotesService, queryOrderStatsService, aiQueryLogRepository);
    }

    private void stubSaveReturnsArgument() {
        when(aiQueryLogRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @SuppressWarnings("unchecked")
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
    @DisplayName("searchNotes가 호출되면 인자를 그대로 실행하고 실제 쓰인 sourceType을 로그에 남긴다")
    void executesSearchNotesAndRecordsSourceType() {
        stubSaveReturnsArgument();
        simulateToolCalls("납기는 10월 15일입니다.",
                new ToolCall("searchNotes", Map.of("query", "납기 지연", "sourceType", "ORDER_NOTE")));
        when(searchNotesService.searchNotes("납기 지연", SourceType.ORDER_NOTE)).thenReturn("[ORDER_NOTE #1] ...");

        AiQueryLog result = service.ask(42L, "납기 언제야?");

        verify(searchNotesService).searchNotes("납기 지연", SourceType.ORDER_NOTE);
        ArgumentCaptor<AiQueryLog> captor = ArgumentCaptor.forClass(AiQueryLog.class);
        verify(aiQueryLogRepository).save(captor.capture());
        assertThat(captor.getValue().queryType()).isEqualTo(QueryType.RAG_QA);
        assertThat(captor.getValue().sourceType()).isEqualTo(SourceType.ORDER_NOTE);
        assertThat(captor.getValue().requestedBy()).isEqualTo(42L);
        assertThat(captor.getValue().question()).isEqualTo("납기 언제야?");
        assertThat(captor.getValue().response()).isEqualTo("납기는 10월 15일입니다.");
        assertThat(result.response()).isEqualTo("납기는 10월 15일입니다.");
    }

    @Test
    @DisplayName("queryOrderStats가 호출되면 인자를 그대로 전달하고, searchNotes를 안 썼으면 sourceType 없이 기록한다")
    void executesQueryOrderStatsWithoutSourceType() {
        stubSaveReturnsArgument();
        simulateToolCalls("7월 매출은 3200만원입니다.",
                new ToolCall("queryOrderStats", Map.of("from", "2026-07-01", "to", "2026-07-31", "status", "completed")));
        when(queryOrderStatsService.queryOrderStats("2026-07-01", "2026-07-31", "completed"))
                .thenReturn("완료: 10건");

        AiQueryLog result = service.ask(42L, "7월 매출 얼마야?");

        verify(queryOrderStatsService).queryOrderStats("2026-07-01", "2026-07-31", "completed");
        verifyNoInteractions(searchNotesService);
        assertThat(result.sourceType()).isNull();
    }

    @Test
    @DisplayName("서로 다른 sourceType으로 searchNotes가 여러 번 쓰이면 ALL로 기록한다")
    void recordsAllWhenMultipleSourceTypesUsed() {
        stubSaveReturnsArgument();
        simulateToolCalls("종합 답변",
                new ToolCall("searchNotes", Map.of("query", "지연", "sourceType", "ORDER_NOTE")),
                new ToolCall("searchNotes", Map.of("query", "불만", "sourceType", "CLIENT_MEMO")));
        when(searchNotesService.searchNotes(any(), any())).thenReturn("스니펫");

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
        verifyNoInteractions(searchNotesService, queryOrderStatsService);
    }

    @Test
    @DisplayName("sourceType 인자를 생략하면 ALL로 검색한다")
    void defaultsToAllWhenSourceTypeArgMissing() {
        stubSaveReturnsArgument();
        simulateToolCalls("답변", new ToolCall("searchNotes", Map.of("query", "지연")));
        when(searchNotesService.searchNotes(any(), any())).thenReturn("스니펫");

        service.ask(42L, "질문");

        verify(searchNotesService).searchNotes("지연", SourceType.ALL);
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

package com.prodio.stat.application;

import com.prodio.stat.domain.SourceType;
import com.prodio.stat.embedding.application.ClientEmbeddingRepository;
import com.prodio.stat.embedding.application.EmbeddingMatch;
import com.prodio.stat.embedding.application.OrderEmbeddingRepository;
import com.prodio.stat.embedding.application.ProductionEmbeddingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SearchNotesService")
class SearchNotesServiceTest {

    @Mock private AiClient aiClient;
    @Mock private OrderEmbeddingRepository orderEmbeddingRepository;
    @Mock private ClientEmbeddingRepository clientEmbeddingRepository;
    @Mock private ProductionEmbeddingRepository productionEmbeddingRepository;
    private SearchNotesService service;

    private final float[] queryVector = {0.1f, 0.2f};

    @BeforeEach
    void setUp() {
        service = new SearchNotesService(aiClient, orderEmbeddingRepository, clientEmbeddingRepository, productionEmbeddingRepository);
        when(aiClient.embed("납기 지연 문의")).thenReturn(queryVector);
    }

    @Test
    @DisplayName("sourceType이 ORDER_NOTE면 order 임베딩만 검색한다")
    void searchesOnlyOrderNoteWhenSourceTypeIsOrderNote() {
        when(orderEmbeddingRepository.search(queryVector, 5))
                .thenReturn(List.of(new EmbeddingMatch(1L, "10월 15일까지 납기 요청", 0.1)));

        String result = service.searchNotes("납기 지연 문의", SourceType.ORDER_NOTE);

        assertThat(result).contains("[ORDER_NOTE #1] 10월 15일까지 납기 요청");
        verify(orderEmbeddingRepository).search(queryVector, 5);
        verifyNoInteractions(clientEmbeddingRepository, productionEmbeddingRepository);
    }

    @Test
    @DisplayName("sourceType이 ALL이면 세 테이블을 모두 검색해 거리순으로 병합하고 상위 5건만 남긴다")
    void mergesAllSourcesAndLimitsToTopKWhenSourceTypeIsAll() {
        when(orderEmbeddingRepository.search(any(), anyInt())).thenReturn(List.of(
                new EmbeddingMatch(1L, "order-a", 0.30),
                new EmbeddingMatch(2L, "order-b", 0.05),
                new EmbeddingMatch(3L, "order-c", 0.50)
        ));
        when(clientEmbeddingRepository.search(any(), anyInt())).thenReturn(List.of(
                new EmbeddingMatch(4L, "client-a", 0.20),
                new EmbeddingMatch(5L, "client-b", 0.40)
        ));
        when(productionEmbeddingRepository.search(any(), anyInt())).thenReturn(List.of(
                new EmbeddingMatch(6L, "production-a", 0.10),
                new EmbeddingMatch(7L, "production-b", 0.60)
        ));

        String result = service.searchNotes("납기 지연 문의", SourceType.ALL);

        List<String> lines = List.of(result.strip().split("\n"));
        assertThat(lines).hasSize(5);
        assertThat(lines).containsExactly(
                "[ORDER_NOTE #2] order-b",
                "[PRODUCTION_MEMO #6] production-a",
                "[CLIENT_MEMO #4] client-a",
                "[ORDER_NOTE #1] order-a",
                "[CLIENT_MEMO #5] client-b"
        );
        verify(orderEmbeddingRepository).search(eq(queryVector), eq(5));
        verify(clientEmbeddingRepository).search(eq(queryVector), eq(5));
        verify(productionEmbeddingRepository).search(eq(queryVector), eq(5));
    }

    @Test
    @DisplayName("검색 결과가 없으면 안내 문구를 반환한다")
    void returnsFallbackMessageWhenNoMatches() {
        when(orderEmbeddingRepository.search(any(), anyInt())).thenReturn(List.of());

        String result = service.searchNotes("납기 지연 문의", SourceType.ORDER_NOTE);

        assertThat(result).isEqualTo("관련된 노트/메모를 찾지 못했습니다.");
    }

    @Test
    @DisplayName("ALL이 아니면 지정된 소스 하나만 조회하고 나머지는 호출하지 않는다")
    void searchesOnlyProductionMemoWhenRequested() {
        when(productionEmbeddingRepository.search(any(), anyInt()))
                .thenReturn(List.of(new EmbeddingMatch(9L, "생산 지연 메모", 0.2)));

        service.searchNotes("납기 지연 문의", SourceType.PRODUCTION_MEMO);

        verify(productionEmbeddingRepository).search(queryVector, 5);
        verify(orderEmbeddingRepository, never()).search(any(), anyInt());
        verify(clientEmbeddingRepository, never()).search(any(), anyInt());
    }
}

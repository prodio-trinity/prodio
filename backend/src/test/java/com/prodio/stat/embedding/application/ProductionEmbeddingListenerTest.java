package com.prodio.stat.embedding.application;

import com.prodio.production.event.ProductionMemo;
import com.prodio.stat.application.OrderStatViewRepository;
import com.prodio.stat.domain.OrderStatView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductionEmbeddingListener")
class ProductionEmbeddingListenerTest {
    private static final long ORDER_ID = 1L;

    @Mock private OrderStatViewRepository orderStatViewRepository;
    @Mock private ProductionEmbeddingRepository productionEmbeddingRepository;
    @Mock private ProductionEmbeddingWriter productionEmbeddingWriter;
    private ProductionEmbeddingListener listener;

    @BeforeEach
    void setUp() {
        listener = new ProductionEmbeddingListener(orderStatViewRepository,
                productionEmbeddingRepository, productionEmbeddingWriter);
    }

    @Test
    @DisplayName("memo가 있으면 OrderStatView로 문맥을 조회해 writer에 넘긴다")
    void memoPresentDelegatesToWriter() {
        when(productionEmbeddingRepository.findText(ORDER_ID)).thenReturn(Optional.empty());
        OrderStatView shaft = OrderStatView.create(ORDER_ID, 2L, "○○상사", 3L, "정밀 샤프트", 3, 25_500L,
                OffsetDateTime.parse("2026-08-18T10:00:00+09:00"));
        when(orderStatViewRepository.findAllByOrderId(ORDER_ID)).thenReturn(List.of(shaft));

        listener.handle(new ProductionMemo(ORDER_ID, "생산 과정에서 도색 이슈 발생했습니다."));

        verify(productionEmbeddingWriter).upsert(ORDER_ID,
                "[정밀 샤프트 3개, ○○상사 주문] 생산 과정에서 도색 이슈 발생했습니다.");
    }

    @Test
    @DisplayName("memo가 비어 있으면 스킵한다")
    void memoBlankSkips() {
        listener.handle(new ProductionMemo(ORDER_ID, ""));

        verifyNoInteractions(orderStatViewRepository, productionEmbeddingRepository, productionEmbeddingWriter);
    }

    @Test
    @DisplayName("조합한 텍스트가 기존 저장값과 같으면 재임베딩을 스킵한다")
    void skipsWhenTextUnchanged() {
        when(productionEmbeddingRepository.findText(ORDER_ID))
                .thenReturn(Optional.of("[정밀 샤프트 3개, ○○상사 주문] 생산 과정에서 도색 이슈 발생했습니다."));
        OrderStatView shaft = OrderStatView.create(ORDER_ID, 2L, "○○상사", 3L, "정밀 샤프트", 3, 25_500L,
                OffsetDateTime.parse("2026-08-18T10:00:00+09:00"));
        when(orderStatViewRepository.findAllByOrderId(ORDER_ID)).thenReturn(List.of(shaft));

        listener.handle(new ProductionMemo(ORDER_ID, "생산 과정에서 도색 이슈 발생했습니다."));

        verifyNoInteractions(productionEmbeddingWriter);
    }

    @Test
    @DisplayName("OrderStatView가 없으면 예외를 던져 이벤트를 미완료 상태로 남긴다")
    void throwsWhenOrderStatViewMissing() {
        when(orderStatViewRepository.findAllByOrderId(ORDER_ID)).thenReturn(List.of());

        assertThatThrownBy(() -> listener.handle(new ProductionMemo(ORDER_ID, "도색 이슈 발생했습니다.")))
                .isInstanceOf(IllegalStateException.class);

        verifyNoInteractions(productionEmbeddingWriter);
    }
}

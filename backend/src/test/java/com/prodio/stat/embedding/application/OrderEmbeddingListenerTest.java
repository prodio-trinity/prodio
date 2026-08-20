package com.prodio.stat.embedding.application;

import com.prodio.order.OrderCancelledEvent;
import com.prodio.order.OrderCreatedEvent;
import com.prodio.order.OrderDeliveryEventData;
import com.prodio.order.OrderItemEventData;
import com.prodio.order.OrderUpdatedEvent;
import com.prodio.stat.application.AiClient;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderEmbeddingListener")
class OrderEmbeddingListenerTest {
    private static final long ORDER_ID = 1L;
    private static final float[] EMBEDDING = new float[] {0.1f, 0.2f};

    @Mock private OrderEmbeddingRepository orderEmbeddingRepository;
    @Mock private OrderStatViewRepository orderStatViewRepository;
    @Mock private AiClient aiClient;
    private OrderEmbeddingListener listener;

    @BeforeEach
    void setUp() {
        listener = new OrderEmbeddingListener(orderEmbeddingRepository, orderStatViewRepository, aiClient);
    }

    @Test
    @DisplayName("생성 이벤트에 note가 있으면 텍스트를 임베딩해 저장한다")
    void createdEventWithNoteEmbedsAndUpserts() {
        OrderCreatedEvent event = createdEvent("급하게 부탁드려요");
        when(orderEmbeddingRepository.findNoteText(ORDER_ID)).thenReturn(Optional.empty());
        when(aiClient.embed(any())).thenReturn(EMBEDDING);

        listener.handle(event);

        String expectedText = "[정밀 샤프트 3개, ○○상사 주문] 급하게 부탁드려요";
        verify(aiClient).embed(expectedText);
        verify(orderEmbeddingRepository).upsert(ORDER_ID, expectedText, EMBEDDING);
    }

    @Test
    @DisplayName("생성 이벤트에 note가 없으면 스킵한다")
    void createdEventWithoutNoteSkips() {
        listener.handle(createdEvent(""));

        verifyNoInteractions(orderEmbeddingRepository, aiClient);
    }

    @Test
    @DisplayName("수정 이벤트로 조합한 텍스트가 기존 저장값과 같으면 재임베딩을 스킵한다")
    void updatedEventSkipsWhenTextUnchanged() {
        String unchangedText = "[정밀 샤프트 3개, ○○상사 주문] 급하게 부탁드려요";
        when(orderEmbeddingRepository.findNoteText(ORDER_ID)).thenReturn(Optional.of(unchangedText));

        listener.handle(updatedEvent("급하게 부탁드려요"));

        verify(aiClient, never()).embed(any());
        verify(orderEmbeddingRepository, never()).upsert(anyLong(), any(), any());
    }

    @Test
    @DisplayName("수정 이벤트로 조합한 텍스트가 기존과 다르면 다시 임베딩한다")
    void updatedEventEmbedsWhenTextChanged() {
        when(orderEmbeddingRepository.findNoteText(ORDER_ID)).thenReturn(Optional.of("[정밀 샤프트 3개, ○○상사 주문] 이전 요청"));
        when(aiClient.embed(any())).thenReturn(EMBEDDING);

        listener.handle(updatedEvent("변경된 요청사항"));

        String expectedText = "[정밀 샤프트 3개, ○○상사 주문] 변경된 요청사항";
        verify(aiClient).embed(expectedText);
        verify(orderEmbeddingRepository).upsert(ORDER_ID, expectedText, EMBEDDING);
    }

    @Test
    @DisplayName("기존 note_text가 있으면 취소사유를 이어붙여 재임베딩한다")
    void cancelledEventAppendsToExistingText() {
        when(orderEmbeddingRepository.findNoteText(ORDER_ID))
                .thenReturn(Optional.of("[정밀 샤프트 3개, ○○상사 주문] 급하게 부탁드려요"));
        when(aiClient.embed(any())).thenReturn(EMBEDDING);

        listener.handle(new OrderCancelledEvent(ORDER_ID, "재고 부족으로 취소",
                OffsetDateTime.parse("2026-08-20T10:00:00+09:00")));

        String expectedText = "[정밀 샤프트 3개, ○○상사 주문] 급하게 부탁드려요\n[취소사유] 재고 부족으로 취소";
        verify(aiClient).embed(expectedText);
        verify(orderEmbeddingRepository).upsert(ORDER_ID, expectedText, EMBEDDING);
        verifyNoInteractions(orderStatViewRepository);
    }

    @Test
    @DisplayName("기존 note_text가 없으면 OrderStatView로 문맥을 새로 조회해 취소사유를 임베딩한다")
    void cancelledEventRebuildsContextWhenNoExistingText() {
        when(orderEmbeddingRepository.findNoteText(ORDER_ID)).thenReturn(Optional.empty());
        OrderStatView shaft = OrderStatView.create(ORDER_ID, 2L, "○○상사", 3L, "정밀 샤프트", 3, 25_500L,
                OffsetDateTime.parse("2026-08-18T10:00:00+09:00"));
        when(orderStatViewRepository.findAllByOrderId(ORDER_ID)).thenReturn(List.of(shaft));
        when(aiClient.embed(any())).thenReturn(EMBEDDING);

        listener.handle(new OrderCancelledEvent(ORDER_ID, "재고 부족으로 취소",
                OffsetDateTime.parse("2026-08-20T10:00:00+09:00")));

        String expectedText = "[정밀 샤프트 3개, ○○상사 주문] [취소사유] 재고 부족으로 취소";
        verify(aiClient).embed(expectedText);
        verify(orderEmbeddingRepository).upsert(ORDER_ID, expectedText, EMBEDDING);
    }

    @Test
    @DisplayName("이미 취소사유가 반영돼 있으면 이벤트 재발행에도 중복 append하지 않는다")
    void cancelledEventSkipsWhenAlreadyApplied() {
        when(orderEmbeddingRepository.findNoteText(ORDER_ID))
                .thenReturn(Optional.of("[정밀 샤프트 3개, ○○상사 주문] 급하게 부탁드려요\n[취소사유] 재고 부족으로 취소"));

        listener.handle(new OrderCancelledEvent(ORDER_ID, "재고 부족으로 취소",
                OffsetDateTime.parse("2026-08-20T10:00:00+09:00")));

        verify(aiClient, never()).embed(any());
        verify(orderEmbeddingRepository, never()).upsert(anyLong(), any(), any());
        verifyNoInteractions(orderStatViewRepository);
    }

    private OrderCreatedEvent createdEvent(String note) {
        OrderItemEventData shaft = new OrderItemEventData(3L, "정밀 샤프트", 8_500L, 3, 25_500L);
        return new OrderCreatedEvent(ORDER_ID, 2L, "○○상사", "010-0000-0000",
                "", List.of(shaft), true, 25_500L, delivery(), note,
                OffsetDateTime.parse("2026-08-18T10:00:00+09:00"));
    }

    private OrderUpdatedEvent updatedEvent(String note) {
        OrderItemEventData shaft = new OrderItemEventData(3L, "정밀 샤프트", 8_500L, 3, 25_500L);
        return new OrderUpdatedEvent(ORDER_ID, 2L, "○○상사", "010-0000-0000",
                "", List.of(shaft), true, 25_500L, delivery(), note,
                OffsetDateTime.parse("2026-08-19T10:00:00+09:00"));
    }

    private OrderDeliveryEventData delivery() {
        return new OrderDeliveryEventData("기본 배송지", "홍길동", "010-0000-0000",
                "12345", "서울시 어딘가", "");
    }
}

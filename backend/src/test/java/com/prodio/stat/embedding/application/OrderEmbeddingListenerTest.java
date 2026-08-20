package com.prodio.stat.embedding.application;

import com.prodio.order.OrderCancelledEvent;
import com.prodio.order.OrderCreatedEvent;
import com.prodio.order.OrderDeliveryEventData;
import com.prodio.order.OrderItemEventData;
import com.prodio.order.OrderUpdatedEvent;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderEmbeddingListener")
class OrderEmbeddingListenerTest {
    private static final long ORDER_ID = 1L;

    @Mock private OrderEmbeddingRepository orderEmbeddingRepository;
    @Mock private OrderStatViewRepository orderStatViewRepository;
    @Mock private OrderEmbeddingWriter orderEmbeddingWriter;
    private OrderEmbeddingListener listener;

    @BeforeEach
    void setUp() {
        listener = new OrderEmbeddingListener(orderEmbeddingRepository, orderStatViewRepository, orderEmbeddingWriter);
    }

    @Test
    @DisplayName("생성 이벤트에 note가 있으면 조합한 텍스트를 writer에 넘긴다")
    void createdEventWithNoteDelegatesToWriter() {
        OrderCreatedEvent event = createdEvent("급하게 부탁드려요");
        when(orderEmbeddingRepository.findText(ORDER_ID)).thenReturn(Optional.empty());

        listener.handle(event);

        verify(orderEmbeddingWriter).upsert(ORDER_ID, "[정밀 샤프트 3개, ○○상사 주문] 급하게 부탁드려요");
    }

    @Test
    @DisplayName("생성 이벤트에 note가 없으면 스킵한다")
    void createdEventWithoutNoteSkips() {
        listener.handle(createdEvent(""));

        verifyNoInteractions(orderEmbeddingRepository, orderEmbeddingWriter);
    }

    @Test
    @DisplayName("수정 이벤트로 조합한 텍스트가 기존 저장값과 같으면 재임베딩을 스킵한다")
    void updatedEventSkipsWhenTextUnchanged() {
        String unchangedText = "[정밀 샤프트 3개, ○○상사 주문] 급하게 부탁드려요";
        when(orderEmbeddingRepository.findText(ORDER_ID)).thenReturn(Optional.of(unchangedText));

        listener.handle(updatedEvent("급하게 부탁드려요"));

        verifyNoInteractions(orderEmbeddingWriter);
    }

    @Test
    @DisplayName("수정 이벤트로 조합한 텍스트가 기존과 다르면 writer에 새 텍스트를 넘긴다")
    void updatedEventDelegatesWhenTextChanged() {
        when(orderEmbeddingRepository.findText(ORDER_ID)).thenReturn(Optional.of("[정밀 샤프트 3개, ○○상사 주문] 이전 요청"));

        listener.handle(updatedEvent("변경된 요청사항"));

        verify(orderEmbeddingWriter).upsert(ORDER_ID, "[정밀 샤프트 3개, ○○상사 주문] 변경된 요청사항");
    }

    @Test
    @DisplayName("기존 note_text가 있으면 취소사유를 이어붙여 writer에 넘긴다")
    void cancelledEventAppendsToExistingText() {
        when(orderEmbeddingRepository.findText(ORDER_ID))
                .thenReturn(Optional.of("[정밀 샤프트 3개, ○○상사 주문] 급하게 부탁드려요"));

        listener.handle(new OrderCancelledEvent(ORDER_ID, "재고 부족으로 취소",
                OffsetDateTime.parse("2026-08-20T10:00:00+09:00")));

        verify(orderEmbeddingWriter).upsert(ORDER_ID,
                "[정밀 샤프트 3개, ○○상사 주문] 급하게 부탁드려요\n[취소사유] 재고 부족으로 취소");
        verifyNoInteractions(orderStatViewRepository);
    }

    @Test
    @DisplayName("기존 note_text가 없으면 OrderStatView로 문맥을 새로 조회해 취소사유를 writer에 넘긴다")
    void cancelledEventRebuildsContextWhenNoExistingText() {
        when(orderEmbeddingRepository.findText(ORDER_ID)).thenReturn(Optional.empty());
        OrderStatView shaft = OrderStatView.create(ORDER_ID, 2L, "○○상사", 3L, "정밀 샤프트", 3, 25_500L,
                OffsetDateTime.parse("2026-08-18T10:00:00+09:00"));
        when(orderStatViewRepository.findAllByOrderId(ORDER_ID)).thenReturn(List.of(shaft));

        listener.handle(new OrderCancelledEvent(ORDER_ID, "재고 부족으로 취소",
                OffsetDateTime.parse("2026-08-20T10:00:00+09:00")));

        verify(orderEmbeddingWriter).upsert(ORDER_ID, "[정밀 샤프트 3개, ○○상사 주문] [취소사유] 재고 부족으로 취소");
    }

    @Test
    @DisplayName("OrderStatView가 아직 없으면 예외를 던져 이벤트를 미완료 상태로 남긴다")
    void cancelledEventThrowsWhenOrderStatViewMissing() {
        when(orderEmbeddingRepository.findText(ORDER_ID)).thenReturn(Optional.empty());
        when(orderStatViewRepository.findAllByOrderId(ORDER_ID)).thenReturn(List.of());

        assertThatThrownBy(() -> listener.handle(new OrderCancelledEvent(ORDER_ID, "재고 부족으로 취소",
                OffsetDateTime.parse("2026-08-20T10:00:00+09:00"))))
                .isInstanceOf(IllegalStateException.class);

        verifyNoInteractions(orderEmbeddingWriter);
    }

    @Test
    @DisplayName("이미 이번 취소사유가 반영돼 있으면 이벤트 재발행에도 중복 append하지 않는다")
    void cancelledEventSkipsWhenAlreadyApplied() {
        when(orderEmbeddingRepository.findText(ORDER_ID))
                .thenReturn(Optional.of("[정밀 샤프트 3개, ○○상사 주문] 급하게 부탁드려요\n[취소사유] 재고 부족으로 취소"));

        listener.handle(new OrderCancelledEvent(ORDER_ID, "재고 부족으로 취소",
                OffsetDateTime.parse("2026-08-20T10:00:00+09:00")));

        verifyNoInteractions(orderEmbeddingWriter, orderStatViewRepository);
    }

    @Test
    @DisplayName("note 안에 우연히 [취소사유] 문구가 있어도, 이번 취소사유와 다르면 정상적으로 append한다")
    void cancelledEventDoesNotFalsePositiveOnUnrelatedBracketText() {
        when(orderEmbeddingRepository.findText(ORDER_ID))
                .thenReturn(Optional.of("[정밀 샤프트 3개, ○○상사 주문] 이전에 [취소사유] 관련 문의 남겼었어요"));

        listener.handle(new OrderCancelledEvent(ORDER_ID, "재고 부족으로 취소",
                OffsetDateTime.parse("2026-08-20T10:00:00+09:00")));

        verify(orderEmbeddingWriter).upsert(ORDER_ID,
                "[정밀 샤프트 3개, ○○상사 주문] 이전에 [취소사유] 관련 문의 남겼었어요\n[취소사유] 재고 부족으로 취소");
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

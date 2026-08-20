package com.prodio.stat.embedding.application;

import com.prodio.order.OrderCreatedEvent;
import com.prodio.order.OrderDeliveryEventData;
import com.prodio.order.OrderItemEventData;
import com.prodio.order.OrderUpdatedEvent;
import com.prodio.stat.domain.OrderStatView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("OrderEmbeddingTextBuilder")
class OrderEmbeddingTextBuilderTest {

    @Test
    @DisplayName("생성 이벤트를 받으면 주문명+품목+거래처명+note를 조합한다")
    void fromCreatedEventCombinesAllContext() {
        OrderItemEventData shaft = new OrderItemEventData(3L, "정밀 샤프트", 8_500L, 3, 25_500L);
        OrderCreatedEvent event = new OrderCreatedEvent(1L, 2L, "○○상사", "010-0000-0000",
                "8월 정기 발주", List.of(shaft), true, 25_500L, delivery(), "급하게 부탁드려요",
                OffsetDateTime.parse("2026-08-18T10:00:00+09:00"));

        String result = OrderEmbeddingTextBuilder.from(event);

        assertThat(result).isEqualTo("[8월 정기 발주, 정밀 샤프트 3개, ○○상사 주문] 급하게 부탁드려요");
    }

    @Test
    @DisplayName("주문명이 비어 있으면 품목+거래처명만으로 조합한다")
    void fromCreatedEventSkipsBlankOrderName() {
        OrderItemEventData shaft = new OrderItemEventData(3L, "정밀 샤프트", 8_500L, 3, 25_500L);
        OrderCreatedEvent event = new OrderCreatedEvent(1L, 2L, "○○상사", "010-0000-0000",
                "", List.of(shaft), true, 25_500L, delivery(), "급하게 부탁드려요",
                OffsetDateTime.parse("2026-08-18T10:00:00+09:00"));

        String result = OrderEmbeddingTextBuilder.from(event);

        assertThat(result).isEqualTo("[정밀 샤프트 3개, ○○상사 주문] 급하게 부탁드려요");
    }

    @Test
    @DisplayName("품목이 여러 개면 쉼표로 이어붙인다")
    void fromCreatedEventJoinsMultipleItems() {
        OrderItemEventData shaft = new OrderItemEventData(3L, "정밀 샤프트", 8_500L, 3, 25_500L);
        OrderItemEventData bearing = new OrderItemEventData(4L, "베어링", 5_000L, 5, 25_000L);
        OrderCreatedEvent event = new OrderCreatedEvent(1L, 2L, "○○상사", "010-0000-0000",
                "", List.of(shaft, bearing), true, 50_500L, delivery(), "",
                OffsetDateTime.parse("2026-08-18T10:00:00+09:00"));

        String result = OrderEmbeddingTextBuilder.from(event);

        assertThat(result).isEqualTo("[정밀 샤프트 3개, 베어링 5개, ○○상사 주문] ");
    }

    @Test
    @DisplayName("수정 이벤트도 최신 데이터로 동일하게 조합한다")
    void fromUpdatedEventCombinesLatestData() {
        OrderItemEventData shaft = new OrderItemEventData(3L, "정밀 샤프트", 8_500L, 10, 85_000L);
        OrderUpdatedEvent event = new OrderUpdatedEvent(1L, 2L, "○○상사", "010-0000-0000",
                "", List.of(shaft), true, 85_000L, delivery(), "수량 늘려주세요",
                OffsetDateTime.parse("2026-08-19T10:00:00+09:00"));

        String result = OrderEmbeddingTextBuilder.from(event);

        assertThat(result).isEqualTo("[정밀 샤프트 10개, ○○상사 주문] 수량 늘려주세요");
    }

    @Test
    @DisplayName("기존 텍스트가 있으면 취소사유를 줄바꿈으로 이어붙인다")
    void appendCancellationReasonAppendsToExistingText() {
        String result = OrderEmbeddingTextBuilder.appendCancellationReason(
                "[정밀 샤프트 3개, ○○상사 주문] 급하게 부탁드려요", "재고 부족으로 취소");

        assertThat(result).isEqualTo("[정밀 샤프트 3개, ○○상사 주문] 급하게 부탁드려요\n[취소사유] 재고 부족으로 취소");
    }

    @Test
    @DisplayName("기존 텍스트가 없으면 취소사유만으로 텍스트를 만든다")
    void appendCancellationReasonHandlesBlankExisting() {
        assertThat(OrderEmbeddingTextBuilder.appendCancellationReason(null, "재고 부족으로 취소"))
                .isEqualTo("[취소사유] 재고 부족으로 취소");
        assertThat(OrderEmbeddingTextBuilder.appendCancellationReason("", "재고 부족으로 취소"))
                .isEqualTo("[취소사유] 재고 부족으로 취소");
    }

    @Test
    @DisplayName("기존 note_text가 없으면 OrderStatView에서 품목/거래처명을 다시 조회해 조합한다")
    void cancelledWithoutNoteRebuildsContextFromOrderStatView() {
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-08-18T10:00:00+09:00");
        OrderStatView shaft = OrderStatView.create(1L, 2L, "○○상사", 3L, "정밀 샤프트", 3, 25_500L, createdAt);
        OrderStatView bearing = OrderStatView.create(1L, 2L, "○○상사", 4L, "베어링", 5, 25_000L, createdAt);

        String result = OrderEmbeddingTextBuilder.cancelledWithoutNote(1L, List.of(shaft, bearing), "재고 부족으로 취소");

        assertThat(result).isEqualTo("[정밀 샤프트 3개, 베어링 5개, ○○상사 주문] [취소사유] 재고 부족으로 취소");
    }

    @Test
    @DisplayName("OrderStatView가 비어 있으면 예외를 던진다")
    void cancelledWithoutNoteThrowsWhenViewsEmpty() {
        assertThatThrownBy(() -> OrderEmbeddingTextBuilder.cancelledWithoutNote(1L, List.of(), "재고 부족으로 취소"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("1");
    }

    private OrderDeliveryEventData delivery() {
        return new OrderDeliveryEventData("기본 배송지", "홍길동", "010-0000-0000",
                "12345", "서울시 어딘가", "");
    }
}

package com.prodio.stat.application;

import com.prodio.order.OrderCancelledEvent;
import com.prodio.order.OrderConfirmedEvent;
import com.prodio.order.OrderCreatedEvent;
import com.prodio.order.OrderDeliveryEventData;
import com.prodio.order.OrderItemEventData;
import com.prodio.order.OrderUpdatedEvent;
import com.prodio.production.event.OrderCompleted;
import com.prodio.production.event.OrderShipped;
import com.prodio.stat.domain.OrderStatView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("StatListener")
class StatListenerTest {
    private static final long ORDER_ID = 1L;
    private static final LocalDate DUE_DATE = LocalDate.parse("2026-09-01");

    @Mock private OrderStatViewRepository repository;
    private StatListener listener;

    @BeforeEach
    void setUp() {
        listener = new StatListener(repository);
    }

    @Test
    @DisplayName("생성 이벤트를 받으면 품목마다 OrderStatView를 하나씩 생성한다")
    void createdEventCreatesOneOrderStatViewPerItem() {
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-08-18T10:00:00+09:00");
        OrderItemEventData shaft = new OrderItemEventData(3L, "정밀 샤프트", 8_500L, 10, 85_000L);
        OrderItemEventData bearing = new OrderItemEventData(4L, "베어링", 5_000L, 5, 25_000L);
        OrderCreatedEvent event = new OrderCreatedEvent(ORDER_ID, 2L, "거래처", "010-0000-0000",
                List.of(shaft, bearing), 110_000L, DUE_DATE, delivery(), createdAt);

        listener.handle(event);

        verify(repository).create(OrderStatView.create(ORDER_ID, 2L, "거래처",
                3L, "정밀 샤프트", 10, 85_000L, DUE_DATE, createdAt));
        verify(repository).create(OrderStatView.create(ORDER_ID, 2L, "거래처",
                4L, "베어링", 5, 25_000L, DUE_DATE, createdAt));
    }

    @Test
    @DisplayName("확정 이벤트를 받으면 markProductionStarted를 호출한다")
    void confirmedEventMarksProductionStarted() {
        OffsetDateTime confirmedAt = OffsetDateTime.parse("2026-08-19T09:00:00+09:00");
        OrderConfirmedEvent event = new OrderConfirmedEvent(ORDER_ID, 2L, "거래처", "010-0000-0000",
                List.of(), 110_000L, DUE_DATE, delivery(), "", confirmedAt.minusDays(1), confirmedAt);

        listener.handle(event);

        verify(repository).markProductionStarted(ORDER_ID, confirmedAt);
    }

    @Test
    @DisplayName("배송 시작 이벤트를 받으면 markShipped를 호출한다")
    void shippedEventMarksShipped() {
        OffsetDateTime shippedAt = OffsetDateTime.parse("2026-08-25T09:00:00+09:00");

        listener.handle(new OrderShipped(ORDER_ID, shippedAt));

        verify(repository).markShipped(ORDER_ID, shippedAt);
    }

    @Test
    @DisplayName("완료 시각이 납기일 이내면 onTime을 true로 기록한다")
    void completedOnOrBeforeDueDateIsOnTime() {
        when(repository.findAllByOrderId(ORDER_ID)).thenReturn(List.of(view()));
        OffsetDateTime completedAt = DUE_DATE.atStartOfDay(java.time.ZoneOffset.UTC).toOffsetDateTime();

        listener.handle(new OrderCompleted(ORDER_ID, completedAt));

        verify(repository).markCompleted(ORDER_ID, completedAt, true);
    }

    @Test
    @DisplayName("완료 시각이 납기일을 넘기면 onTime을 false로 기록한다")
    void completedAfterDueDateIsNotOnTime() {
        when(repository.findAllByOrderId(ORDER_ID)).thenReturn(List.of(view()));
        OffsetDateTime completedAt = DUE_DATE.plusDays(1).atStartOfDay(java.time.ZoneOffset.UTC).toOffsetDateTime();

        listener.handle(new OrderCompleted(ORDER_ID, completedAt));

        verify(repository).markCompleted(ORDER_ID, completedAt, false);
    }

    @Test
    @DisplayName("완료 이벤트 처리 시 저장된 view가 없으면 예외를 던진다")
    void completedEventFailsWhenViewIsMissing() {
        when(repository.findAllByOrderId(ORDER_ID)).thenReturn(List.of());
        OffsetDateTime completedAt = OffsetDateTime.parse("2026-09-01T09:00:00+09:00");

        assertThatThrownBy(() -> listener.handle(new OrderCompleted(ORDER_ID, completedAt)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("취소 이벤트를 받으면 markCancelled를 호출한다")
    void cancelledEventMarksCancelled() {
        OffsetDateTime cancelledAt = OffsetDateTime.parse("2026-08-20T09:00:00+09:00");

        listener.handle(new OrderCancelledEvent(ORDER_ID, "고객 요청", cancelledAt));

        verify(repository).markCancelled(ORDER_ID, "고객 요청", cancelledAt);
    }

    @Test
    @DisplayName("수정 이벤트를 받으면 기존 row를 지우고 새 items로 orderCreatedAt을 보존한 채 다시 만든다")
    void updatedEventReplacesRowsPreservingOrderCreatedAt() {
        OffsetDateTime orderCreatedAt = OffsetDateTime.parse("2026-08-18T10:00:00+09:00");
        when(repository.findAllByOrderId(ORDER_ID)).thenReturn(List.of(view()));
        LocalDate newDueDate = DUE_DATE.plusDays(3);
        OrderItemEventData bolt = new OrderItemEventData(5L, "볼트", 120L, 100, 12_000L);
        OrderUpdatedEvent event = new OrderUpdatedEvent(ORDER_ID, 2L, "거래처",
                List.of(bolt), 12_000L, newDueDate, delivery(), OffsetDateTime.parse("2026-08-21T09:00:00+09:00"));

        listener.handle(event);

        verify(repository).deleteAllByOrderId(ORDER_ID);
        verify(repository).create(OrderStatView.create(ORDER_ID, 2L, "거래처",
                5L, "볼트", 100, 12_000L, newDueDate, orderCreatedAt));
    }

    @Test
    @DisplayName("수정 이벤트 처리 시 기존 row가 없으면 예외를 던지고 지우지 않는다")
    void updatedEventFailsWhenViewIsMissing() {
        when(repository.findAllByOrderId(ORDER_ID)).thenReturn(List.of());
        OrderUpdatedEvent event = new OrderUpdatedEvent(ORDER_ID, 2L, "거래처",
                List.of(), 0L, DUE_DATE, delivery(), OffsetDateTime.parse("2026-08-21T09:00:00+09:00"));

        assertThatThrownBy(() -> listener.handle(event))
                .isInstanceOf(IllegalStateException.class);

        verify(repository, never()).deleteAllByOrderId(ORDER_ID);
    }

    private OrderStatView view() {
        return OrderStatView.create(ORDER_ID, 2L, "거래처", 3L, "정밀 샤프트", 10, 85_000L,
                DUE_DATE, OffsetDateTime.parse("2026-08-18T10:00:00+09:00"));
    }

    private OrderDeliveryEventData delivery() {
        return new OrderDeliveryEventData("기본 배송지", "홍길동", "010-0000-0000",
                "12345", "서울시 어딘가", "");
    }
}

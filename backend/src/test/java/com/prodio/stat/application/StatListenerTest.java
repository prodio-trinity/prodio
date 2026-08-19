package com.prodio.stat.application;

import com.prodio.stat.SampleOrderCancelledEvent;
import com.prodio.stat.SampleOrderCompletedEvent;
import com.prodio.stat.SampleOrderCreatedEvent;
import com.prodio.stat.SampleOrderItem;
import com.prodio.stat.SampleOrderShippedEvent;
import com.prodio.stat.SampleOrderStartedEvent;
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
        SampleOrderItem shaft = new SampleOrderItem(3L, "정밀 샤프트", 10, 85_000L);
        SampleOrderItem bearing = new SampleOrderItem(4L, "베어링", 5, 25_000L);
        SampleOrderCreatedEvent event = new SampleOrderCreatedEvent(ORDER_ID, 2L, "거래처",
                List.of(shaft, bearing), DUE_DATE, createdAt);

        listener.handle(event);

        verify(repository).create(OrderStatView.create(ORDER_ID, 2L, "거래처",
                3L, "정밀 샤프트", 10, 85_000L, DUE_DATE, createdAt));
        verify(repository).create(OrderStatView.create(ORDER_ID, 2L, "거래처",
                4L, "베어링", 5, 25_000L, DUE_DATE, createdAt));
    }

    @Test
    @DisplayName("생산 시작 이벤트를 받으면 markProductionStarted를 호출한다")
    void startedEventMarksProductionStarted() {
        OffsetDateTime startedAt = OffsetDateTime.parse("2026-08-19T09:00:00+09:00");

        listener.handle(new SampleOrderStartedEvent(ORDER_ID, startedAt));

        verify(repository).markProductionStarted(ORDER_ID, startedAt);
    }

    @Test
    @DisplayName("배송 시작 이벤트를 받으면 markShipped를 호출한다")
    void shippedEventMarksShipped() {
        OffsetDateTime shippedAt = OffsetDateTime.parse("2026-08-25T09:00:00+09:00");

        listener.handle(new SampleOrderShippedEvent(ORDER_ID, shippedAt));

        verify(repository).markShipped(ORDER_ID, shippedAt);
    }

    @Test
    @DisplayName("완료 시각이 납기일 이내면 onTime을 true로 기록한다")
    void completedOnOrBeforeDueDateIsOnTime() {
        when(repository.findAllByOrderId(ORDER_ID)).thenReturn(List.of(view()));
        OffsetDateTime completedAt = DUE_DATE.atStartOfDay(java.time.ZoneOffset.UTC).toOffsetDateTime();

        listener.handle(new SampleOrderCompletedEvent(ORDER_ID, completedAt));

        verify(repository).markCompleted(ORDER_ID, completedAt, true);
    }

    @Test
    @DisplayName("완료 시각이 납기일을 넘기면 onTime을 false로 기록한다")
    void completedAfterDueDateIsNotOnTime() {
        when(repository.findAllByOrderId(ORDER_ID)).thenReturn(List.of(view()));
        OffsetDateTime completedAt = DUE_DATE.plusDays(1).atStartOfDay(java.time.ZoneOffset.UTC).toOffsetDateTime();

        listener.handle(new SampleOrderCompletedEvent(ORDER_ID, completedAt));

        verify(repository).markCompleted(ORDER_ID, completedAt, false);
    }

    @Test
    @DisplayName("완료 이벤트 처리 시 저장된 view가 없으면 예외를 던진다")
    void completedEventFailsWhenViewIsMissing() {
        when(repository.findAllByOrderId(ORDER_ID)).thenReturn(List.of());
        OffsetDateTime completedAt = OffsetDateTime.parse("2026-09-01T09:00:00+09:00");

        assertThatThrownBy(() -> listener.handle(new SampleOrderCompletedEvent(ORDER_ID, completedAt)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("취소 이벤트를 받으면 markCancelled를 호출한다")
    void cancelledEventMarksCancelled() {
        OffsetDateTime cancelledAt = OffsetDateTime.parse("2026-08-20T09:00:00+09:00");

        listener.handle(new SampleOrderCancelledEvent(ORDER_ID, "고객 요청", cancelledAt));

        verify(repository).markCancelled(ORDER_ID, "고객 요청", cancelledAt);
    }

    private OrderStatView view() {
        return OrderStatView.create(ORDER_ID, 2L, "거래처", 3L, "정밀 샤프트", 10, 85_000L,
                DUE_DATE, OffsetDateTime.parse("2026-08-18T10:00:00+09:00"));
    }
}

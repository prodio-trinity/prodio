package com.prodio.stat.infrastructure.persistence;

import com.prodio.stat.domain.OrderStatView;
import com.prodio.stat.domain.OrderViewStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OrderStatViewEntity")
class OrderStatViewEntityTest {

    @Test
    @DisplayName("CANCELLED 이후 markShipped를 호출해도 상태가 바뀌지 않는다")
    void ignoresMarkShippedAfterCancelled() {
        OrderStatViewEntity entity = OrderStatViewEntity.from(view());
        entity.markCancelled("고객 요청", OffsetDateTime.parse("2026-08-20T09:00:00+09:00"));

        entity.markShipped(OffsetDateTime.parse("2026-08-25T09:00:00+09:00"));

        assertThat(entity.toDomain().status()).isEqualTo(OrderViewStatus.CANCELLED);
        assertThat(entity.toDomain().shippedAt()).isNull();
    }

    @Test
    @DisplayName("COMPLETED 이후 markCancelled를 호출해도 상태가 바뀌지 않는다")
    void ignoresMarkCancelledAfterCompleted() {
        OrderStatViewEntity entity = OrderStatViewEntity.from(view());
        entity.markCompleted(OffsetDateTime.parse("2026-08-25T09:00:00+09:00"), true);

        entity.markCancelled("고객 요청", OffsetDateTime.parse("2026-08-26T09:00:00+09:00"));

        assertThat(entity.toDomain().status()).isEqualTo(OrderViewStatus.COMPLETED);
        assertThat(entity.toDomain().cancellationReason()).isNull();
    }

    @Test
    @DisplayName("정상 상태에서는 markProductionStarted가 상태를 바꾼다")
    void marksProductionStartedWhenNotTerminal() {
        OrderStatViewEntity entity = OrderStatViewEntity.from(view());
        OffsetDateTime startedAt = OffsetDateTime.parse("2026-08-19T09:00:00+09:00");

        entity.markProductionStarted(startedAt);

        assertThat(entity.toDomain().status()).isEqualTo(OrderViewStatus.IN_PRODUCTION);
        assertThat(entity.toDomain().productionStartedAt()).isEqualTo(startedAt);
    }

    private OrderStatView view() {
        return OrderStatView.create(1L, 2L, "거래처", 3L, "정밀 샤프트", 10, 85_000L,
                LocalDate.parse("2026-09-01"), OffsetDateTime.parse("2026-08-18T10:00:00+09:00"));
    }
}

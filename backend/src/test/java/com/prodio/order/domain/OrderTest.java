package com.prodio.order.domain;

import com.prodio.order.exception.OrderException;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {
    private static final OffsetDateTime NOW = OffsetDateTime.parse("2026-08-18T10:00:00+09:00");

    @Test
    void vatIncludedAmountIsRoundedToWon() {
        Order order = Order.place(1, "거래처", "010", null,
                java.util.List.of(OrderItem.of(2, "품목", 15_001, 3)), true,
                delivery(), "메모", 1, NOW);

        assertThat(order.totalAmount()).isEqualTo(49_503);
    }

    @Test
    void pendingOrderCanBeUpdatedAndTotalIsRecalculated() {
        Order order = Order.place(1, "거래처", "010", "설비 주문",
                java.util.List.of(OrderItem.of(2, "품목", 15_000, 10)), false,
                delivery(), "", 1, NOW);

        order.update("변경된 주문서", java.util.List.of(
                        OrderItem.of(3, "변경 품목", 20_000, 2),
                        OrderItem.of(4, "추가 품목", 10_000, 1)), true,
                delivery(), "새 메모", NOW.plusHours(1));

        assertThat(order.items()).hasSize(2);
        assertThat(order.orderName()).isEqualTo("변경된 주문서");
        assertThat(order.totalAmount()).isEqualTo(55_000);
        assertThat(order.status()).isEqualTo(OrderStatus.PENDING_PAYMENT);
    }

    @Test
    void confirmedOrderCannotBeChangedAgain() {
        Order order = Order.place(1, "거래처", "010", null,
                java.util.List.of(OrderItem.of(2, "품목", 15_000, 10)), false,
                delivery(), "", 1, NOW);

        order.confirm(NOW.plusMinutes(1));

        assertThat(order.status()).isEqualTo(OrderStatus.CONFIRMED);
        assertThatThrownBy(() -> order.confirm(NOW.plusMinutes(2)))
                .isInstanceOf(OrderException.class);
    }

    @Test
    void cancellationRequiresAReasonAndLocksTheOrder() {
        Order order = Order.place(1, "거래처", "010", null,
                java.util.List.of(OrderItem.of(2, "품목", 15_000, 10)), false,
                delivery(), "", 1, NOW);

        assertThatThrownBy(() -> order.cancel(" ", NOW.plusMinutes(1)))
                .isInstanceOf(IllegalArgumentException.class);

        order.cancel("고객 요청", NOW.plusMinutes(2));

        assertThat(order.status()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(order.cancellationReason()).isEqualTo("고객 요청");
        assertThatThrownBy(() -> order.update(null, java.util.List.of(OrderItem.of(3, "변경", 1, 1)), false,
                delivery(), "", NOW.plusMinutes(3)))
                .isInstanceOf(OrderException.class);
    }

    private DeliverySnapshot delivery() {
        return new DeliverySnapshot(null, "본사", "담당자", "010", "12345", "주소", "상세");
    }
}

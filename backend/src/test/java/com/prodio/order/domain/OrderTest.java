package com.prodio.order.domain;

import com.prodio.order.exception.OrderException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {
    private static final OffsetDateTime NOW = OffsetDateTime.parse("2026-08-18T10:00:00+09:00");

    @Test
    void vatIncludedAmountIsRoundedToWon() {
        Order order = Order.place(1, "거래처", "010", 2, "품목", 15_001,
                3, true, LocalDate.parse("2026-09-01"), "주소", "메모", 1, NOW);

        assertThat(order.totalAmount()).isEqualTo(49_503);
    }

    @Test
    void productionCanStartOnlyOnce() {
        Order order = Order.place(1, "거래처", "010", 2, "품목", 15_000,
                10, false, LocalDate.parse("2026-09-01"), "주소", "", 1, NOW);

        order.startProduction(NOW.plusHours(1));

        assertThat(order.status()).isEqualTo(OrderStatus.IN_PRODUCTION);
        assertThatThrownBy(() -> order.startProduction(NOW.plusHours(2)))
                .isInstanceOf(OrderException.class);
    }

    @Test
    void paymentConfirmationIsIdempotent() {
        Order order = Order.place(1, "거래처", "010", 2, "품목", 15_000,
                10, false, LocalDate.parse("2026-09-01"), "주소", "", 1, NOW);

        order.changePaymentConfirmation(true, NOW.plusMinutes(1));
        order.changePaymentConfirmation(true, NOW.plusMinutes(2));

        assertThat(order.paymentConfirmed()).isTrue();
    }
}

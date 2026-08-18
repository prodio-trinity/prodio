package com.prodio.order.application;

import com.prodio.catalog.CatalogOrderLookup;
import com.prodio.order.domain.Order;
import com.prodio.order.domain.OrderStatus;
import com.prodio.order.exception.OrderErrorCode;
import com.prodio.order.exception.OrderException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    private static final long ACCOUNT_ID = 7L;
    private static final long CLIENT_ID = 42L;
    private static final OffsetDateTime NOW = OffsetDateTime.parse("2026-08-18T10:00:00+09:00");

    @Mock private OrderRepository orderRepository;
    @Mock private CatalogOrderLookup catalogOrderLookup;
    @Mock private ApplicationEventPublisher eventPublisher;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-08-18T01:00:00Z"), ZoneOffset.UTC);
        orderService = new OrderService(orderRepository, catalogOrderLookup, eventPublisher, clock);
    }

    @Test
    void myOrdersAreFilteredByTheClientLinkedToTheAccount() {
        CatalogOrderLookup.ClientSnapshot client = client(CLIENT_ID);
        OrderPage expected = new OrderPage(List.of(order(1L, CLIENT_ID)), 0, 10, 1);
        when(catalogOrderLookup.findClientByAccountId(ACCOUNT_ID)).thenReturn(Optional.of(client));
        when(orderRepository.findAllByClientId(CLIENT_ID, OrderStatus.PENDING, "샤프트", 0, 10))
                .thenReturn(expected);

        OrderPage result = orderService.listMine(ACCOUNT_ID, OrderStatus.PENDING, " 샤프트 ", 0, 10);

        assertThat(result).isSameAs(expected);
        verify(orderRepository).findAllByClientId(CLIENT_ID, OrderStatus.PENDING, "샤프트", 0, 10);
    }

    @Test
    void anotherClientsOrderIsHiddenFromMyOrderDetails() {
        when(catalogOrderLookup.findClientByAccountId(ACCOUNT_ID)).thenReturn(Optional.of(client(CLIENT_ID)));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order(1L, 99L)));

        assertThatThrownBy(() -> orderService.getMine(1L, ACCOUNT_ID))
                .isInstanceOfSatisfying(OrderException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(OrderErrorCode.ORDER_NOT_FOUND));
    }

    private CatalogOrderLookup.ClientSnapshot client(long id) {
        return new CatalogOrderLookup.ClientSnapshot(id, "거래처", "대표자", "주소", "010-0000-0000");
    }

    private Order order(long id, long clientId) {
        return Order.reconstitute(id, clientId, "거래처", "010-0000-0000",
                2L, "정밀 샤프트", 8_500L, 10, false, 85_000L,
                LocalDate.parse("2026-09-01"), "주소", "", OrderStatus.PENDING,
                false, ACCOUNT_ID, NOW, NOW);
    }
}

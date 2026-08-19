package com.prodio.order.application;

import com.prodio.catalog.CatalogOrderLookup;
import com.prodio.order.OrderCancelledEvent;
import com.prodio.order.OrderConfirmedEvent;
import com.prodio.order.OrderCreatedEvent;
import com.prodio.order.OrderUpdatedEvent;
import com.prodio.order.domain.Order;
import com.prodio.order.domain.OrderStatus;
import com.prodio.order.domain.OrderItem;
import com.prodio.order.domain.DeliverySnapshot;
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
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    private static final long ACCOUNT_ID = 7L;
    private static final long CLIENT_ID = 42L;
    private static final OffsetDateTime NOW = OffsetDateTime.parse("2026-08-18T10:00:00+09:00");

    @Mock private OrderRepository orderRepository;
    @Mock private DeliveryAddressRepository deliveryAddressRepository;
    @Mock private CatalogOrderLookup catalogOrderLookup;
    @Mock private ApplicationEventPublisher eventPublisher;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-08-18T01:00:00Z"), ZoneOffset.UTC);
        orderService = new OrderService(orderRepository, deliveryAddressRepository,
                catalogOrderLookup, eventPublisher, clock);
    }

    @Test
    void myOrdersAreFilteredByTheClientLinkedToTheAccount() {
        CatalogOrderLookup.ClientSnapshot client = client(CLIENT_ID);
        OrderPage expected = new OrderPage(List.of(order(1L, CLIENT_ID)), 0, 10, 1);
        when(catalogOrderLookup.findClientByAccountId(ACCOUNT_ID)).thenReturn(Optional.of(client));
        when(orderRepository.findAllByClientId(CLIENT_ID, OrderStatus.PENDING_PAYMENT, "샤프트", 0, 10))
                .thenReturn(expected);

        OrderPage result = orderService.listMine(ACCOUNT_ID, OrderStatus.PENDING_PAYMENT, " 샤프트 ", 0, 10);

        assertThat(result).isSameAs(expected);
        verify(orderRepository).findAllByClientId(CLIENT_ID, OrderStatus.PENDING_PAYMENT, "샤프트", 0, 10);
    }

    @Test
    void orderFormContextContainsTheLinkedClientAndActiveCatalogProducts() {
        when(catalogOrderLookup.findClientByAccountId(ACCOUNT_ID))
                .thenReturn(Optional.of(client(CLIENT_ID)));
        when(catalogOrderLookup.findActiveProducts()).thenReturn(List.of(product(2L), product(3L)));

        OrderFormContext context = orderService.formContext(ACCOUNT_ID);

        assertThat(context.client().clientId()).isEqualTo(CLIENT_ID);
        assertThat(context.client().businessRegistrationNumber()).isEqualTo("123-45-67890");
        assertThat(context.products()).extracting(OrderProductContext::productId)
                .containsExactly(2L, 3L);
    }

    @Test
    void anotherClientsOrderIsHiddenFromMyOrderDetails() {
        when(catalogOrderLookup.findClientByAccountId(ACCOUNT_ID)).thenReturn(Optional.of(client(CLIENT_ID)));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order(1L, 99L)));

        assertThatThrownBy(() -> orderService.getMine(1L, ACCOUNT_ID))
                .isInstanceOfSatisfying(OrderException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(OrderErrorCode.ORDER_NOT_FOUND));
    }

    @Test
    void clientCanUpdateItsOwnOrderBeforePaymentIsConfirmed() {
        Order pending = order(1L, CLIENT_ID);
        when(orderRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(pending));
        when(catalogOrderLookup.findClientByAccountId(ACCOUNT_ID)).thenReturn(Optional.of(client(CLIENT_ID)));
        when(catalogOrderLookup.findProduct(3L)).thenReturn(Optional.of(product(3L)));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order updated = orderService.updateMine(1L, ACCOUNT_ID, new UpdateOrderCommand(
                List.of(new OrderItemCommand(3L, 2)), true,
                LocalDate.parse("2026-09-10"), deliveryCommand(), "거래처 수정"));

        assertThat(updated.items()).extracting(OrderItem::productId).containsExactly(3L);
        assertThat(updated.note()).isEqualTo("거래처 수정");
        verify(eventPublisher).publishEvent(any(OrderUpdatedEvent.class));
    }

    @Test
    void clientCannotUpdateAnotherClientsOrder() {
        when(orderRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(order(1L, 99L)));
        when(catalogOrderLookup.findClientByAccountId(ACCOUNT_ID)).thenReturn(Optional.of(client(CLIENT_ID)));

        assertThatThrownBy(() -> orderService.updateMine(1L, ACCOUNT_ID, new UpdateOrderCommand(
                List.of(new OrderItemCommand(3L, 2)), true,
                LocalDate.parse("2026-09-10"), deliveryCommand(), "변경 시도")))
                .isInstanceOfSatisfying(OrderException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(OrderErrorCode.ORDER_NOT_FOUND));
    }

    @Test
    void creatingAnOrderPublishesTheCreatedEvent() {
        when(catalogOrderLookup.findClient(CLIENT_ID)).thenReturn(Optional.of(client(CLIENT_ID)));
        when(catalogOrderLookup.findProduct(2L)).thenReturn(Optional.of(product(2L)));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> withId(invocation.getArgument(0)));

        orderService.create(new CreateOrderCommand(CLIENT_ID,
                List.of(new OrderItemCommand(2L, 10)), false,
                LocalDate.parse("2026-09-01"), deliveryCommand(), "메모", ACCOUNT_ID));

        verify(eventPublisher).publishEvent(any(OrderCreatedEvent.class));
    }

    @Test
    void updateConfirmAndCancelPublishTheirOwnEvents() {
        Order pending = order(1L, CLIENT_ID);
        when(catalogOrderLookup.findProduct(3L)).thenReturn(Optional.of(product(3L)));
        when(orderRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(pending));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        orderService.update(1L, new UpdateOrderCommand(
                List.of(new OrderItemCommand(3L, 2)), true,
                LocalDate.parse("2026-09-10"), deliveryCommand(), "새 메모"));

        verify(eventPublisher).publishEvent(any(OrderUpdatedEvent.class));

        Order confirmable = order(2L, CLIENT_ID);
        when(orderRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(confirmable));
        orderService.confirm(2L);
        verify(eventPublisher).publishEvent(any(OrderConfirmedEvent.class));

        Order cancellable = order(3L, CLIENT_ID);
        when(orderRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(cancellable));
        orderService.cancel(3L, "고객 요청");
        verify(eventPublisher).publishEvent(any(OrderCancelledEvent.class));
    }

    private CatalogOrderLookup.ClientSnapshot client(long id) {
        return new CatalogOrderLookup.ClientSnapshot(id, "CLIENT-001", "거래처", "대표자",
                "123-45-67890", "주소", "010-0000-0000", "담당자", "메모", true);
    }

    private CatalogOrderLookup.ProductSnapshot product(long id) {
        return new CatalogOrderLookup.ProductSnapshot(id, "PRODUCT-001", "정밀 샤프트",
                1L, "EA", "규격", "메모", 8_500L, true);
    }

    private Order withId(Order order) {
        return Order.reconstitute(1L, order.clientId(), order.clientNameSnapshot(),
                order.clientPhoneSnapshot(), order.items(), order.vatIncluded(), order.totalAmount(),
                order.dueDate(), order.delivery(), order.note(), order.status(),
                order.cancellationReason(), order.createdBy(), order.createdAt(), order.updatedAt());
    }

    private Order order(long id, long clientId) {
        return Order.reconstitute(id, clientId, "거래처", "010-0000-0000",
                List.of(OrderItem.of(2L, "정밀 샤프트", 8_500L, 10)), false, 85_000L,
                LocalDate.parse("2026-09-01"), delivery(), "", OrderStatus.PENDING_PAYMENT,
                null, ACCOUNT_ID, NOW, NOW);
    }

    private DeliveryCommand deliveryCommand() {
        return new DeliveryCommand(null, "본사", "담당자", "010", "12345", "주소", "상세");
    }

    private DeliverySnapshot delivery() {
        return deliveryCommand().toSnapshot();
    }
}

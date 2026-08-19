package com.prodio.order.application;

import com.prodio.catalog.CatalogOrderLookup;
import com.prodio.order.OrderCancelledEvent;
import com.prodio.order.OrderConfirmedEvent;
import com.prodio.order.OrderCreatedEvent;
import com.prodio.order.OrderUpdatedEvent;
import com.prodio.order.domain.Order;
import com.prodio.order.domain.OrderStatus;
import com.prodio.order.exception.OrderErrorCode;
import com.prodio.order.exception.OrderException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final CatalogOrderLookup catalogOrderLookup;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    @Transactional
    public Order create(CreateOrderCommand command) {
        CatalogOrderLookup.ClientSnapshot client = catalogOrderLookup.findClient(command.clientId())
                .orElseThrow(() -> new OrderException(OrderErrorCode.CLIENT_NOT_FOUND));
        CatalogOrderLookup.ProductSnapshot product = catalogOrderLookup.findProduct(command.productId())
                .orElseThrow(() -> new OrderException(OrderErrorCode.PRODUCT_NOT_FOUND));
        OffsetDateTime now = OffsetDateTime.now(clock);
        Order order = Order.place(client.id(), client.companyName(), client.phone(),
                product.id(), product.name(), product.unitPrice(), command.quantity(),
                command.vatIncluded(), command.dueDate(), command.deliveryAddress(),
                command.note(), command.createdBy(), now);
        Order saved = orderRepository.save(order);
        eventPublisher.publishEvent(OrderCreatedEvent.from(saved));
        return saved;
    }

    @Transactional(readOnly = true)
    public Order get(long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public OrderPage list(OrderStatus status, String query, int page, int size) {
        validatePage(page, size);
        return orderRepository.findAll(status, normalizeQuery(query), page, size);
    }

    @Transactional(readOnly = true)
    public OrderPage listMine(long accountId, OrderStatus status, String query, int page, int size) {
        validatePage(page, size);
        long clientId = findClientForAccount(accountId).id();
        return orderRepository.findAllByClientId(clientId, status, normalizeQuery(query), page, size);
    }

    @Transactional(readOnly = true)
    public Order getMine(long id, long accountId) {
        Order order = get(id);
        if (order.clientId() != findClientForAccount(accountId).id()) {
            throw new OrderException(OrderErrorCode.ORDER_NOT_FOUND);
        }
        return order;
    }

    private CatalogOrderLookup.ClientSnapshot findClientForAccount(long accountId) {
        return catalogOrderLookup.findClientByAccountId(accountId)
                .orElseThrow(() -> new OrderException(OrderErrorCode.CLIENT_ACCOUNT_NOT_LINKED));
    }

    private void validatePage(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new OrderException(OrderErrorCode.INVALID_ORDER_REQUEST,
                    "페이지는 0 이상, 조회 크기는 1~100이어야 합니다.");
        }
    }

    private String normalizeQuery(String query) {
        return query == null ? "" : query.trim();
    }

    @Transactional
    public Order update(long id, UpdateOrderCommand command) {
        Order order = findForUpdate(id);
        CatalogOrderLookup.ProductSnapshot product = catalogOrderLookup.findProduct(command.productId())
                .orElseThrow(() -> new OrderException(OrderErrorCode.PRODUCT_NOT_FOUND));
        order.update(product.id(), product.name(), product.unitPrice(), command.quantity(),
                command.vatIncluded(), command.dueDate(), command.deliveryAddress(), command.note(),
                OffsetDateTime.now(clock));
        Order saved = orderRepository.save(order);
        eventPublisher.publishEvent(OrderUpdatedEvent.from(saved));
        return saved;
    }

    @Transactional
    public Order confirm(long id) {
        Order order = findForUpdate(id);
        order.confirm(OffsetDateTime.now(clock));
        Order saved = orderRepository.save(order);
        eventPublisher.publishEvent(OrderConfirmedEvent.from(saved));
        return saved;
    }

    @Transactional
    public Order cancel(long id, String reason) {
        Order order = findForUpdate(id);
        order.cancel(reason, OffsetDateTime.now(clock));
        Order saved = orderRepository.save(order);
        eventPublisher.publishEvent(OrderCancelledEvent.from(saved));
        return saved;
    }

    private Order findForUpdate(long id) {
        return orderRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));
    }
}

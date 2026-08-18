package com.prodio.order.application;

import com.prodio.order.OrderCreated;
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
    private final CatalogSnapshotGateway catalogSnapshotGateway;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    @Transactional
    public Order create(CreateOrderCommand command) {
        CatalogSnapshotGateway.ClientSnapshot client = catalogSnapshotGateway.findClient(command.clientId())
                .orElseThrow(() -> new OrderException(OrderErrorCode.CLIENT_NOT_FOUND));
        CatalogSnapshotGateway.ProductSnapshot product = catalogSnapshotGateway.findProduct(command.productId())
                .orElseThrow(() -> new OrderException(OrderErrorCode.PRODUCT_NOT_FOUND));
        OffsetDateTime now = OffsetDateTime.now(clock);
        Order order = Order.place(client.id(), client.companyName(), client.phone(),
                product.id(), product.name(), product.unitPrice(), command.quantity(),
                command.vatIncluded(), command.dueDate(), command.deliveryAddress(),
                command.note(), command.createdBy(), now);
        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public Order get(long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public OrderPage list(OrderStatus status, String query, int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new OrderException(OrderErrorCode.INVALID_ORDER_REQUEST,
                    "페이지는 0 이상, 조회 크기는 1~100이어야 합니다.");
        }
        return orderRepository.findAll(status, query == null ? "" : query.trim(), page, size);
    }

    @Transactional
    public Order startProduction(long id) {
        Order order = findForUpdate(id);
        order.startProduction(OffsetDateTime.now(clock));
        Order saved = orderRepository.save(order);
        eventPublisher.publishEvent(OrderCreated.from(saved));
        return saved;
    }

    @Transactional
    public Order updatePayment(long id, boolean confirmed) {
        Order order = findForUpdate(id);
        order.changePaymentConfirmation(confirmed, OffsetDateTime.now(clock));
        return orderRepository.save(order);
    }

    private Order findForUpdate(long id) {
        return orderRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));
    }
}

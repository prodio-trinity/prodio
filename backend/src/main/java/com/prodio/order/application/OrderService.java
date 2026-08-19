package com.prodio.order.application;

import com.prodio.catalog.CatalogOrderLookup;
import com.prodio.order.OrderCancelledEvent;
import com.prodio.order.OrderConfirmedEvent;
import com.prodio.order.OrderCreatedEvent;
import com.prodio.order.OrderUpdatedEvent;
import com.prodio.order.domain.Order;
import com.prodio.order.domain.OrderItem;
import com.prodio.order.domain.OrderStatus;
import com.prodio.order.domain.DeliveryAddress;
import com.prodio.order.domain.DeliverySnapshot;
import com.prodio.order.exception.OrderErrorCode;
import com.prodio.order.exception.OrderException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final DeliveryAddressRepository deliveryAddressRepository;
    private final CatalogOrderLookup catalogOrderLookup;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    @Transactional
    public Order create(CreateOrderCommand command) {
        CatalogOrderLookup.ClientSnapshot client = catalogOrderLookup.findClient(command.clientId())
                .filter(CatalogOrderLookup.ClientSnapshot::active)
                .orElseThrow(() -> new OrderException(OrderErrorCode.CLIENT_NOT_FOUND));
        OffsetDateTime now = OffsetDateTime.now(clock);
        Order order = Order.place(client.id(), client.companyName(), client.phone(),
                resolveItems(command.items()), command.vatIncluded(), command.dueDate(),
                resolveDelivery(client.id(), command.delivery()),
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

    @Transactional(readOnly = true)
    public OrderClientContext clientContext(long accountId) {
        return toClientContext(findClientForAccount(accountId));
    }

    @Transactional(readOnly = true)
    public OrderFormContext formContext(long accountId) {
        return formContext(findClientForAccount(accountId));
    }

    @Transactional(readOnly = true)
    public OrderFormContext formContextForClient(long clientId) {
        CatalogOrderLookup.ClientSnapshot client = catalogOrderLookup.findClient(clientId)
                .filter(CatalogOrderLookup.ClientSnapshot::active)
                .orElseThrow(() -> new OrderException(OrderErrorCode.CLIENT_NOT_FOUND));
        return formContext(client);
    }

    private OrderFormContext formContext(CatalogOrderLookup.ClientSnapshot client) {
        List<OrderProductContext> products = catalogOrderLookup.findActiveProducts().stream()
                .map(product -> new OrderProductContext(product.id(), product.productCode(),
                        product.name(), product.subCategoryId(), product.unit(), product.description(),
                        product.memo(), product.unitPrice()))
                .toList();
        return new OrderFormContext(toClientContext(client), products);
    }

    private OrderClientContext toClientContext(CatalogOrderLookup.ClientSnapshot client) {
        return new OrderClientContext(client.id(), client.clientCode(), client.companyName(),
                client.representative(), client.businessRegistrationNumber(), client.defaultAddress(),
                client.phone(), client.managerName(), client.memo());
    }

    private CatalogOrderLookup.ClientSnapshot findClientForAccount(long accountId) {
        return catalogOrderLookup.findClientByAccountId(accountId)
                .filter(CatalogOrderLookup.ClientSnapshot::active)
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
        return updateOrder(order, command);
    }

    @Transactional
    public Order updateMine(long id, long accountId, UpdateOrderCommand command) {
        Order order = findForUpdate(id);
        if (order.clientId() != findClientForAccount(accountId).id()) {
            throw new OrderException(OrderErrorCode.ORDER_NOT_FOUND);
        }
        return updateOrder(order, command);
    }

    private Order updateOrder(Order order, UpdateOrderCommand command) {
        order.update(resolveItems(command.items()), command.vatIncluded(), command.dueDate(),
                resolveDelivery(order.clientId(), command.delivery()), command.note(),
                OffsetDateTime.now(clock));
        Order saved = orderRepository.save(order);
        eventPublisher.publishEvent(OrderUpdatedEvent.from(saved));
        return saved;
    }

    private List<OrderItem> resolveItems(List<OrderItemCommand> commands) {
        if (commands == null || commands.isEmpty()) {
            throw new OrderException(OrderErrorCode.INVALID_ORDER_REQUEST, "주문 품목이 하나 이상 필요합니다.");
        }
        return commands.stream().map(command -> {
            CatalogOrderLookup.ProductSnapshot product = catalogOrderLookup.findProduct(command.productId())
                    .filter(CatalogOrderLookup.ProductSnapshot::active)
                    .orElseThrow(() -> new OrderException(OrderErrorCode.PRODUCT_NOT_FOUND));
            return OrderItem.of(product.id(), product.name(), product.unitPrice(), command.quantity());
        }).toList();
    }

    private DeliverySnapshot resolveDelivery(long clientId, DeliveryCommand command) {
        if (command == null) {
            throw new OrderException(OrderErrorCode.INVALID_ORDER_REQUEST, "배송 정보가 필요합니다.");
        }
        if (command.addressId() == null) return command.toSnapshot();
        DeliveryAddress address = deliveryAddressRepository.findById(command.addressId())
                .orElseThrow(() -> new OrderException(OrderErrorCode.DELIVERY_ADDRESS_NOT_FOUND));
        if (address.clientId() != clientId) {
            throw new OrderException(OrderErrorCode.DELIVERY_ADDRESS_ACCESS_DENIED);
        }
        return address.snapshot();
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

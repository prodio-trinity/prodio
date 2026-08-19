package com.prodio.order.application;

import com.prodio.catalog.CatalogOrderLookup;
import com.prodio.order.domain.DeliveryAddress;
import com.prodio.order.domain.DeliverySnapshot;
import com.prodio.order.exception.OrderErrorCode;
import com.prodio.order.exception.OrderException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class OrderDeliveryService {
    private final DeliveryAddressRepository deliveryAddressRepository;
    private final OrderRepository orderRepository;
    private final CatalogOrderLookup catalogOrderLookup;
    private final Clock clock;

    @Transactional(readOnly = true)
    public DeliveryContext context(long clientId, long accountId, boolean privileged) {
        CatalogOrderLookup.ClientSnapshot client = authorizedClient(clientId, accountId, privileged);
        DeliverySnapshot headquarters = client.defaultAddress() == null || client.defaultAddress().isBlank()
                ? null : new DeliverySnapshot(null, "본사", client.representative(),
                        client.phone(), "", client.defaultAddress(), "");
        DeliverySnapshot recent = orderRepository.findAllByClientId(clientId, null, "", 0, 20)
                .orders().stream().map(order -> order.delivery())
                .filter(delivery -> !"미입력".equals(delivery.addressLine1()))
                .map(delivery -> new DeliverySnapshot(null, "최근 배송지",
                        delivery.recipientName(), delivery.recipientPhone(), delivery.postalCode(),
                        delivery.addressLine1(), delivery.addressLine2()))
                .findFirst().orElse(null);
        return new DeliveryContext(client.id(), client.companyName(), headquarters, recent,
                deliveryAddressRepository.findAllByClientId(clientId).stream()
                        .map(DeliveryAddress::snapshot).toList());
    }

    @Transactional
    public DeliverySnapshot create(long clientId, DeliveryCommand command,
            long accountId, boolean privileged) {
        authorizedClient(clientId, accountId, privileged);
        DeliveryAddress address = DeliveryAddress.create(clientId, withoutId(command), now());
        return deliveryAddressRepository.save(address).snapshot();
    }

    @Transactional
    public DeliverySnapshot update(long id, DeliveryCommand command,
            long accountId, boolean privileged) {
        DeliveryAddress address = find(id);
        authorizedClient(address.clientId(), accountId, privileged);
        address.update(withoutId(command), now());
        return deliveryAddressRepository.save(address).snapshot();
    }

    @Transactional
    public void delete(long id, long accountId, boolean privileged) {
        DeliveryAddress address = find(id);
        authorizedClient(address.clientId(), accountId, privileged);
        deliveryAddressRepository.delete(address);
    }

    private CatalogOrderLookup.ClientSnapshot authorizedClient(long clientId, long accountId,
            boolean privileged) {
        CatalogOrderLookup.ClientSnapshot client = catalogOrderLookup.findClient(clientId)
                .orElseThrow(() -> new OrderException(OrderErrorCode.CLIENT_NOT_FOUND));
        if (!privileged) {
            long ownedClientId = catalogOrderLookup.findClientByAccountId(accountId)
                    .orElseThrow(() -> new OrderException(OrderErrorCode.CLIENT_ACCOUNT_NOT_LINKED)).id();
            if (ownedClientId != clientId) {
                throw new OrderException(OrderErrorCode.DELIVERY_ADDRESS_ACCESS_DENIED);
            }
        }
        return client;
    }

    private DeliveryAddress find(long id) {
        return deliveryAddressRepository.findById(id)
                .orElseThrow(() -> new OrderException(OrderErrorCode.DELIVERY_ADDRESS_NOT_FOUND));
    }

    private DeliverySnapshot withoutId(DeliveryCommand command) {
        return new DeliverySnapshot(null, command.name(), command.recipientName(),
                command.recipientPhone(), command.postalCode(), command.addressLine1(),
                command.addressLine2());
    }

    private OffsetDateTime now() { return OffsetDateTime.now(clock); }
}

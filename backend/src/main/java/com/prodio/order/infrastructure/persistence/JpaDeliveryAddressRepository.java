package com.prodio.order.infrastructure.persistence;

import com.prodio.order.application.DeliveryAddressRepository;
import com.prodio.order.domain.DeliveryAddress;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
class JpaDeliveryAddressRepository implements DeliveryAddressRepository {
    private final SpringDataDeliveryAddressRepository addresses;

    @Override
    public DeliveryAddress save(DeliveryAddress address) {
        DeliveryAddressEntity entity;
        if (address.id() == 0) {
            entity = DeliveryAddressEntity.from(address);
        } else {
            entity = addresses.findById(address.id()).orElseThrow();
            entity.apply(address);
        }
        return addresses.save(entity).toDomain();
    }

    @Override
    public Optional<DeliveryAddress> findById(long id) {
        return addresses.findById(id).map(DeliveryAddressEntity::toDomain);
    }

    @Override
    public List<DeliveryAddress> findAllByClientId(long clientId) {
        return addresses.findAllByClientIdOrderByUpdatedAtDesc(clientId).stream()
                .map(DeliveryAddressEntity::toDomain).toList();
    }

    @Override
    public void delete(DeliveryAddress address) {
        addresses.deleteById(address.id());
    }
}

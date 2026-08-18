package com.prodio.order.application;

import java.util.Optional;

/** Catalog 공개 계약이 준비되면 실제 어댑터로 교체할 Order 측 포트. */
public interface CatalogSnapshotGateway {
    Optional<ClientSnapshot> findClient(long clientId);
    Optional<ProductSnapshot> findProduct(long productId);

    record ClientSnapshot(long id, String companyName, String phone) {}
    record ProductSnapshot(long id, String name, long unitPrice) {}
}

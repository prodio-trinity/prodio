package com.prodio.order.infrastructure.catalog;

import com.prodio.order.application.CatalogSnapshotGateway;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/** Catalog 모듈 공개 계약이 생기기 전 Order 화면·API 확인용 임시 어댑터. */
@Component
class StubCatalogSnapshotGateway implements CatalogSnapshotGateway {
    private static final Map<Long, ClientSnapshot> CLIENTS = Map.of(
            1L, new ClientSnapshot(1L, "ABC 제조", "010-1234-5678"),
            2L, new ClientSnapshot(2L, "한빛 테크", "010-2468-1357"),
            3L, new ClientSnapshot(3L, "미래 정밀", "010-9876-5432"));

    private static final Map<Long, ProductSnapshot> PRODUCTS = Map.of(
            1L, new ProductSnapshot(1L, "표준 브래킷", 10_000L),
            2L, new ProductSnapshot(2L, "A형 부품", 15_000L),
            3L, new ProductSnapshot(3L, "정밀 샤프트", 8_500L));

    @Override
    public Optional<ClientSnapshot> findClient(long clientId) {
        return Optional.ofNullable(CLIENTS.get(clientId));
    }

    @Override
    public Optional<ProductSnapshot> findProduct(long productId) {
        return Optional.ofNullable(PRODUCTS.get(productId));
    }
}

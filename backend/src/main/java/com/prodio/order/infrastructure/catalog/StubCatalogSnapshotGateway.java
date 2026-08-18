package com.prodio.order.infrastructure.catalog;

import com.prodio.catalog.CatalogOrderLookup;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/** Catalog 모듈의 실제 구현이 들어오기 전 Order 화면·API 확인용 임시 어댑터. */
@Component
class StubCatalogSnapshotGateway implements CatalogOrderLookup {
    private static final Map<Long, ClientSnapshot> CLIENTS = Map.of(
            1L, new ClientSnapshot(1L, "ABC 제조", "김대표", "서울시 강남구", "010-1234-5678"),
            2L, new ClientSnapshot(2L, "한빛 테크", "이대표", "경기도 수원시", "010-2468-1357"),
            3L, new ClientSnapshot(3L, "미래 정밀", "박대표", "인천시 남동구", "010-9876-5432"));

    private static final Map<Long, ProductSnapshot> PRODUCTS = Map.of(
            1L, new ProductSnapshot(1L, "표준 브래킷", "SB-100 / 일반형", 10_000L),
            2L, new ProductSnapshot(2L, "A형 부품", "PART-A / 알루미늄", 15_000L),
            3L, new ProductSnapshot(3L, "정밀 샤프트", "SHAFT-08 / Ø8", 8_500L));

    @Override
    public Optional<ClientSnapshot> findClient(long clientId) {
        return Optional.ofNullable(CLIENTS.get(clientId));
    }

    @Override
    public Optional<ClientSnapshot> findClientByAccountId(long accountId) {
        // 실제 Catalog 구현에서는 계정-거래처 1:1 매핑을 조회한다.
        return Optional.ofNullable(CLIENTS.get(accountId));
    }

    @Override
    public Optional<ProductSnapshot> findProduct(long productId) {
        return Optional.ofNullable(PRODUCTS.get(productId));
    }
}

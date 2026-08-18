package com.prodio.catalog;

import java.util.Optional;

/**
 * 주문 등록에 필요한 카탈로그 정보를 제공하는 공개 계약.
 * 구현은 Catalog 모듈이 담당하고 Order 모듈은 이 인터페이스만 사용한다.
 */
public interface CatalogOrderLookup {
    Optional<ClientSnapshot> findClient(long clientId);
    /** 로그인 계정과 1:1로 연결된 거래처를 조회한다. */
    Optional<ClientSnapshot> findClientByAccountId(long accountId);
    Optional<ProductSnapshot> findProduct(long productId);

    record ClientSnapshot(
            long id,
            String companyName,
            String representative,
            String defaultAddress,
            String phone) {}

    record ProductSnapshot(
            long id,
            String name,
            String description,
            long unitPrice) {}
}

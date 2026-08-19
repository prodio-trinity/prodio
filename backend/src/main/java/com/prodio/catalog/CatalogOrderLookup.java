package com.prodio.catalog;

import java.util.List;
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
    /** 주문 화면에 표시할 활성 품목 전체를 조회한다. */
    List<ProductSnapshot> findActiveProducts();

    record ClientSnapshot(
            long id,
            String clientCode,
            String companyName,
            String representative,
            String businessRegistrationNumber,
            String defaultAddress,
            String phone,
            String managerName,
            String memo,
            boolean active) {}

    record ProductSnapshot(
            long id,
            String productCode,
            String name,
            long subCategoryId,
            String unit,
            String description,
            String memo,
            long unitPrice,
            boolean active) {}
}

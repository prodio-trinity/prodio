package com.prodio.stat.embedding.application;

import com.prodio.stat.domain.OrderStatView;

import java.util.List;

/**
 * production은 거래처명/품목 정보를 모른다(주문 참조는 orderId뿐).
 * stat 자체 read model인 OrderStatView에서 매번 조회해 문맥을 채운다.
 */
final class ProductionEmbeddingTextBuilder {

    private ProductionEmbeddingTextBuilder() {}

    static String from(long orderId, List<OrderStatView> views, String memo) {
        return "[" + OrderStatViewContext.describe(orderId, views) + "] " + memo;
    }
}

package com.prodio.stat.embedding.application;

import com.prodio.stat.domain.OrderStatView;

import java.util.List;
import java.util.stream.Collectors;

/**
 * production은 거래처명/품목 정보를 모른다(주문 참조는 orderId뿐).
 * stat 자체 read model인 OrderStatView에서 매번 조회해 문맥을 채운다.
 */
final class ProductionEmbeddingTextBuilder {

    private ProductionEmbeddingTextBuilder() {}

    static String from(long orderId, List<OrderStatView> views, String memo) {
        if (views.isEmpty()) {
            throw new IllegalStateException("OrderStatView를 찾을 수 없습니다. orderId=" + orderId);
        }

        String clientName = views.get(0).clientName();
        String itemSummary = views.stream()
                .map(view -> view.productName() + " " + view.quantity() + "개")
                .collect(Collectors.joining(", "));

        return "[" + itemSummary + ", " + clientName + " 주문] " + memo;
    }
}

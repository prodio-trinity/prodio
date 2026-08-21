package com.prodio.stat.embedding.application;

import com.prodio.stat.domain.OrderStatView;

import java.util.List;
import java.util.stream.Collectors;

final class OrderStatViewContext {

    private OrderStatViewContext() {}

    static String describe(long orderId, List<OrderStatView> views) {
        if (views.isEmpty()) {
            throw new IllegalStateException("OrderStatView를 찾을 수 없습니다. orderId=" + orderId);
        }

        String clientName = views.get(0).clientName();
        String itemSummary = views.stream()
                .map(view -> view.productName() + " " + view.quantity() + "개")
                .collect(Collectors.joining(", "));

        return itemSummary + ", " + clientName + " 주문";
    }
}

package com.prodio.order;

import com.prodio.order.domain.OrderItem;

public record OrderItemEventData(long productId, String productName,
        long unitPrice, int quantity, long lineAmount) {
    static OrderItemEventData from(OrderItem item) {
        return new OrderItemEventData(item.productId(), item.productNameSnapshot(),
                item.unitPriceSnapshot(), item.quantity(), item.lineAmount());
    }
}

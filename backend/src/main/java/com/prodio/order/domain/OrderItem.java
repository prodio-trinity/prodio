package com.prodio.order.domain;

public record OrderItem(long productId, String productNameSnapshot,
        long unitPriceSnapshot, int quantity, long lineAmount) {

    public OrderItem {
        if (productId <= 0 || unitPriceSnapshot < 0 || quantity <= 0 || lineAmount < 0) {
            throw new IllegalArgumentException("주문 품목 값이 올바르지 않습니다.");
        }
        if (productNameSnapshot == null || productNameSnapshot.isBlank()) {
            throw new IllegalArgumentException("품목명이 필요합니다.");
        }
        productNameSnapshot = productNameSnapshot.trim();
        if (Math.multiplyExact(unitPriceSnapshot, (long) quantity) != lineAmount) {
            throw new IllegalArgumentException("품목 금액이 단가와 수량에 맞지 않습니다.");
        }
    }

    public static OrderItem of(long productId, String productName, long unitPrice, int quantity) {
        return new OrderItem(productId, productName, unitPrice, quantity,
                Math.multiplyExact(unitPrice, (long) quantity));
    }
}

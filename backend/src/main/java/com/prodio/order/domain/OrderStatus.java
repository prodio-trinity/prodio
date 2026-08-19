package com.prodio.order.domain;

public enum OrderStatus {
    PENDING_PAYMENT,
    CONFIRMED,
    CANCELLED;

    public static OrderStatus from(String value) {
        return OrderStatus.valueOf(value.trim().toUpperCase());
    }
}

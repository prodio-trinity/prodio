package com.prodio.order.domain;

public enum OrderStatus {
    PENDING,
    IN_PRODUCTION;

    public static OrderStatus from(String value) {
        return OrderStatus.valueOf(value.trim().toUpperCase());
    }
}

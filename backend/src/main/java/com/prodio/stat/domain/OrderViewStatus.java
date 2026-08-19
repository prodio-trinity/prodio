package com.prodio.stat.domain;

public enum OrderViewStatus {
    PENDING,
    IN_PRODUCTION,
    IN_DELIVERY,
    COMPLETED,
    CANCELLED;

    public static OrderViewStatus from(String value) {
        return OrderViewStatus.valueOf(value.trim().toUpperCase());
    }
}

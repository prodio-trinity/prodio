package com.prodio.production.application;

public enum ProductionStatus {
    IN_PRODUCTION, IN_DELIVERY, COMPLETED;

    public static ProductionStatus from(String value) {
        return ProductionStatus.valueOf(value.toUpperCase());
    }
}

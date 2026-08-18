package com.prodio.catalog.domain;

public enum Unit {
    EA,
    BOX,
    KG,
    SET;

    public static Unit from(String value) {
        return Unit.valueOf(value.trim().toUpperCase());
    }
}
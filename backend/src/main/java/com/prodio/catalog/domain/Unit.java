package com.prodio.catalog.domain;

public enum Unit {
    EA,
    BOX,
    KG,
    SET;

    public static Unit from(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("단위는 필수입니다.");
        }
        try {
            return Unit.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("존재하지 않는 단위입니다: " + value);
        }
    }
}
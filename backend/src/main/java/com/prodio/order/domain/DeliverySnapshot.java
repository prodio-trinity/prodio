package com.prodio.order.domain;

public record DeliverySnapshot(Long addressId, String name, String recipientName,
        String recipientPhone, String postalCode, String addressLine1, String addressLine2) {

    public DeliverySnapshot {
        if (addressId != null && addressId <= 0) {
            throw new IllegalArgumentException("배송지 식별자는 양수여야 합니다.");
        }
        name = requireText(name, "배송지명이 필요합니다.");
        recipientName = normalize(recipientName);
        recipientPhone = normalize(recipientPhone);
        postalCode = normalize(postalCode);
        addressLine1 = requireText(addressLine1, "배송지 주소가 필요합니다.");
        addressLine2 = normalize(addressLine2);
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(message);
        return value.trim();
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}

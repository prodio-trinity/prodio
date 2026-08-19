package com.prodio.order.application;

public record OrderClientContext(long clientId, String clientCode, String companyName,
        String representative, String businessRegistrationNumber, String defaultAddress,
        String phone, String managerName, String memo) {
}

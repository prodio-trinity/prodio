package com.prodio.production.application;

import java.time.LocalDateTime;

public record ShipInfo(Long orderId, String phone, LocalDateTime shippedAt) {
}

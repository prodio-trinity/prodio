package com.prodio.production.event;

import java.time.OffsetDateTime;

public record OrderShipped(
        Long orderId,
        OffsetDateTime shippedAt
) {}

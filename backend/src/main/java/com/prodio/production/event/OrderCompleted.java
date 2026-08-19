package com.prodio.production.event;

import java.time.OffsetDateTime;

public record OrderCompleted(
        long orderId,
        OffsetDateTime completedAt
) {}

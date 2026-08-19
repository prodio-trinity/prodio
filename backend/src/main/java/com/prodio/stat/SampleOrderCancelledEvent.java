package com.prodio.stat;

import java.time.OffsetDateTime;

/** order 모듈의 실제 이벤트가 준비되기 전까지 리스너 배선을 검증하기 위한 placeholder.
 * 실제 OrderCancelledEvent(com.prodio.order)와 동일한 payload 형태다. */
public record SampleOrderCancelledEvent(
        long orderId,
        String cancellationReason,
        OffsetDateTime cancelledAt
) {}

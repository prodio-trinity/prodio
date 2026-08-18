package com.prodio.stat;

import java.time.OffsetDateTime;

/** order 모듈의 실제 이벤트가 준비되기 전까지 리스너 배선을 검증하기 위한 placeholder. */
public record SampleOrderStartedEvent(
        long orderId,
        OffsetDateTime startedAt
) {}

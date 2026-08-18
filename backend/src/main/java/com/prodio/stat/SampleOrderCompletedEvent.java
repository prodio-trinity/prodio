package com.prodio.stat;

import java.time.OffsetDateTime;

/** production 모듈의 실제 이벤트가 준비되기 전까지 리스너 배선을 검증하기 위한 placeholder. */
public record SampleOrderCompletedEvent(
        long orderId,
        OffsetDateTime completedAt
) {}

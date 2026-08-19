package com.prodio.stat;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * order 모듈의 실제 이벤트가 준비되기 전까지 리스너 배선을 검증하기 위한 placeholder.
 * order 모듈이 다품목(cart) 주문을 지원하면서 실제 OrderCreatedEvent도 품목을
 * items 리스트로 담기 때문에, 여기도 동일한 모양으로 맞춘다.
 */
public record SampleOrderCreatedEvent(
        long orderId,
        long clientId,
        String clientName,
        List<SampleOrderItem> items,
        LocalDate dueDate,
        OffsetDateTime createdAt
) {}

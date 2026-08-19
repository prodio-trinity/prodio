package com.prodio.stat;

/** order 모듈의 실제 OrderItemEventData가 준비되기 전까지 쓰는 placeholder. */
public record SampleOrderItem(
    long productId,
    String productName,
    int quantity,
    long lineAmount
) {}

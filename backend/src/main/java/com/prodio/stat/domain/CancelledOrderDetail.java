package com.prodio.stat.domain;

/** 취소된 개별 주문의 사유 — queryOrderStats가 CANCELLED 필터로 조회될 때만 채워진다. */
public record CancelledOrderDetail(
        long orderId,
        String clientName,
        String cancellationReason
) {}

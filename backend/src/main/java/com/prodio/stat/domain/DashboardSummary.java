package com.prodio.stat.domain;

/**
 * 통계 대시보드 집계 결과.
 * completedQuantity는 생산량(완료된 주문의 수량 합)이다.
 */
public record DashboardSummary(
        long pendingCount,
        long inProductionCount,
        long inDeliveryCount,
        long completedCount,
        long cancelledCount,
        long totalCount,
        long completedQuantity
) {}

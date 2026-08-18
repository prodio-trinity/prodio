package com.prodio.stat.domain;

/**
 * 통계 대시보드 집계 결과.
 * completedQuantity는 생산량(완료된 주문의 수량 합), onTimeRate는 납기 이행률
 * (완료 건 중 on_time 비율)이며 완료 건이 없으면 null이다.
 */
public record DashboardSummary(
        long pendingCount,
        long inProductionCount,
        long inDeliveryCount,
        long completedCount,
        long totalCount,
        long completedQuantity,
        Double onTimeRate
) {}

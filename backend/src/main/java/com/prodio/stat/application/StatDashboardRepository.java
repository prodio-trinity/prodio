package com.prodio.stat.application;

import com.prodio.stat.domain.CancelledOrderDetail;
import com.prodio.stat.domain.DailyProduction;
import com.prodio.stat.domain.DashboardSummary;
import com.prodio.stat.domain.ProductDistribution;
import com.prodio.stat.domain.StatFilter;

import java.util.List;

public interface StatDashboardRepository {
    DashboardSummary summarize(StatFilter filter);
    List<ProductDistribution> productDistribution(StatFilter filter);

    /** filter.from()/to()가 없으면 오늘까지 최근 14일로 기본 범위를 잡는다. */
    List<DailyProduction> dailyProduction(StatFilter filter);

    /** filter.status()가 CANCELLED가 아니면 빈 목록을 반환한다 — 취소 사유는 CANCELLED에서만 의미가 있다. */
    List<CancelledOrderDetail> cancelledOrderDetails(StatFilter filter);
}

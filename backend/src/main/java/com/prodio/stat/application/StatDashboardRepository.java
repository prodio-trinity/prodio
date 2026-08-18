package com.prodio.stat.application;

import com.prodio.stat.domain.DashboardSummary;
import com.prodio.stat.domain.ProductDistribution;
import com.prodio.stat.domain.StatFilter;

import java.util.List;

public interface StatDashboardRepository {
    DashboardSummary summarize(StatFilter filter);
    List<ProductDistribution> productDistribution(StatFilter filter);
}

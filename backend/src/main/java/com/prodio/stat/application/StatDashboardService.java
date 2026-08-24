package com.prodio.stat.application;

import com.prodio.stat.domain.DailyProduction;
import com.prodio.stat.domain.DashboardSummary;
import com.prodio.stat.domain.ProductDistribution;
import com.prodio.stat.domain.StatFilter;
import com.prodio.stat.exception.StatErrorCode;
import com.prodio.stat.exception.StatException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StatDashboardService {
    
    private final StatDashboardRepository repository;

    @Transactional(readOnly = true)
    public DashboardSummary getDashboard(StatFilter filter) {
        validate(filter);
        return repository.summarize(filter);
    }

    @Transactional(readOnly = true)
    public List<ProductDistribution> getProductDistribution(StatFilter filter) {
        validate(filter);
        return repository.productDistribution(filter);
    }

    @Transactional(readOnly = true)
    public List<DailyProduction> getDailyProduction(StatFilter filter) {
        validate(filter);
        return repository.dailyProduction(filter);
    }

    private void validate(StatFilter filter) {
        if (filter.from() != null && filter.to() != null && filter.from().isAfter(filter.to())) {
            throw new StatException(StatErrorCode.STAT_INVALID_FILTER);
        }
    }
}

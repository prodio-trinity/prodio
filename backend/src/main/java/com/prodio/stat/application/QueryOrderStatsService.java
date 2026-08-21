package com.prodio.stat.application;

import com.prodio.stat.domain.DashboardSummary;
import com.prodio.stat.domain.OrderViewStatus;
import com.prodio.stat.domain.ProductDistribution;
import com.prodio.stat.domain.StatFilter;
import com.prodio.stat.exception.StatErrorCode;
import com.prodio.stat.exception.StatException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * 기존 StatDashboardService를 감싸 문자열 인자(from/to/status)를 받는 얇은 어댑터.
 * RAG QA의 queryOrderStats 도구가 실행할 실제 조회 로직.
 */
@Service
@RequiredArgsConstructor
public class QueryOrderStatsService {

    private final StatDashboardRepository statDashboardRepository;

    public String queryOrderStats(String from, String to, String status) {
        StatFilter filter = new StatFilter(parseDate(from), parseDate(to), parseStatus(status));
        validate(filter);

        DashboardSummary summary = statDashboardRepository.summarize(filter);
        List<ProductDistribution> distribution = statDashboardRepository.productDistribution(filter);

        return format(filter, summary, distribution);
    }

    private void validate(StatFilter filter) {
        if (filter.from() != null && filter.to() != null && filter.from().isAfter(filter.to())) {
            throw new StatException(StatErrorCode.STAT_INVALID_FILTER);
        }
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException exception) {
            throw new StatException(StatErrorCode.STAT_INVALID_FILTER);
        }
    }

    private OrderViewStatus parseStatus(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return OrderViewStatus.from(value);
        } catch (IllegalArgumentException exception) {
            throw new StatException(StatErrorCode.STAT_INVALID_FILTER);
        }
    }

    private String format(StatFilter filter, DashboardSummary summary, List<ProductDistribution> distribution) {
        StringBuilder result = new StringBuilder();
        result.append("조회 조건: ").append(describeFilter(filter)).append("\n");
        result.append("대기: ").append(summary.pendingCount()).append("건\n");
        result.append("생산중: ").append(summary.inProductionCount()).append("건\n");
        result.append("배송중: ").append(summary.inDeliveryCount()).append("건\n");
        result.append("완료: ").append(summary.completedCount()).append("건\n");
        result.append("취소: ").append(summary.cancelledCount()).append("건\n");
        result.append("전체: ").append(summary.totalCount()).append("건\n");
        result.append("완료 생산량: ").append(summary.completedQuantity()).append("\n");

        if (distribution.isEmpty()) {
            return result.toString();
        }

        result.append("품목별 분포:\n");
        for (ProductDistribution product : distribution) {
            result.append("- ").append(product.productName()).append(": ")
                    .append(product.orderCount()).append("건, 수량 ").append(product.totalQuantity()).append("\n");
        }

        return result.toString();
    }

    private String describeFilter(StatFilter filter) {
        String from = filter.from() != null ? filter.from().toString() : "전체";
        String to = filter.to() != null ? filter.to().toString() : "전체";
        String description = from + " ~ " + to;

        return filter.status() != null ? description + ", status=" + filter.status() : description;
    }
}

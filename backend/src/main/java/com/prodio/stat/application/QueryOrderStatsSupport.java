package com.prodio.stat.application;

import com.prodio.stat.domain.CancelledOrderDetail;
import com.prodio.stat.domain.DashboardSummary;
import com.prodio.stat.domain.OrderViewStatus;
import com.prodio.stat.domain.ProductDistribution;
import com.prodio.stat.domain.StatFilter;
import com.prodio.stat.exception.StatErrorCode;
import com.prodio.stat.exception.StatException;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/** queryOrderStats 도구의 순수 로직(파싱/검증/포맷)만 모아둔 정적 유틸. 조회(I/O)는 RagQaService가 직접 수행한다. */
final class QueryOrderStatsSupport {

    private QueryOrderStatsSupport() {}

    static void validate(StatFilter filter) {
        if (filter.from() != null && filter.to() != null && filter.from().isAfter(filter.to())) {
            throw new StatException(StatErrorCode.STAT_INVALID_FILTER);
        }
    }

    static LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException exception) {
            throw new StatException(StatErrorCode.STAT_INVALID_FILTER);
        }
    }

    static OrderViewStatus parseStatus(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return OrderViewStatus.from(value);
        } catch (IllegalArgumentException exception) {
            throw new StatException(StatErrorCode.STAT_INVALID_FILTER);
        }
    }

    static String format(StatFilter filter, DashboardSummary summary, List<ProductDistribution> distribution,
            List<CancelledOrderDetail> cancelledDetails) {
        StringBuilder result = new StringBuilder();
        result.append("조회 조건: ").append(describeFilter(filter)).append("\n");
        result.append("대기: ").append(summary.pendingCount()).append("건\n");
        result.append("생산중: ").append(summary.inProductionCount()).append("건\n");
        result.append("배송중: ").append(summary.inDeliveryCount()).append("건\n");
        result.append("완료: ").append(summary.completedCount()).append("건\n");
        result.append("취소: ").append(summary.cancelledCount()).append("건\n");
        result.append("전체: ").append(summary.totalCount()).append("건\n");
        result.append("완료 생산량: ").append(summary.completedQuantity()).append("\n");

        if (!cancelledDetails.isEmpty()) {
            result.append("취소 사유:\n");
            for (CancelledOrderDetail detail : cancelledDetails) {
                result.append("- 주문 #").append(detail.orderId()).append(" (").append(detail.clientName())
                        .append("): ").append(detail.cancellationReason()).append("\n");
            }
        }

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

    private static String describeFilter(StatFilter filter) {
        String from = filter.from() != null ? filter.from().toString() : "전체";
        String to = filter.to() != null ? filter.to().toString() : "전체";
        String description = from + " ~ " + to;

        return filter.status() != null ? description + ", status=" + filter.status() : description;
    }
}

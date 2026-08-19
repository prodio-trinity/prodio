package com.prodio.stat.infrastructure.persistence;

import com.prodio.stat.application.StatDashboardRepository;
import com.prodio.stat.domain.DashboardSummary;
import com.prodio.stat.domain.OrderViewStatus;
import com.prodio.stat.domain.ProductDistribution;
import com.prodio.stat.domain.StatFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
class JpaStatDashboardRepository implements StatDashboardRepository {
    private final OrderStatViewJpaRepository orderStatViews;
    private final Clock clock;

    @Override
    public DashboardSummary summarize(StatFilter filter) {
        OffsetDateTime from = toStartOfDay(filter.from());
        OffsetDateTime to = toExclusiveEnd(filter.to());

        Map<OrderViewStatus, Long> counts = new EnumMap<>(OrderViewStatus.class);
        for (OrderViewStatus status : OrderViewStatus.values()) {
            counts.put(status, 0L);
        }
        long total = 0;
        for (OrderStatViewJpaRepository.StatusCount row : orderStatViews.countByStatus(from, to, filter.status())) {
            counts.put(row.getStatus(), row.getCount());
            total += row.getCount();
        }

        OrderStatViewJpaRepository.CompletedAggregate completed =
                orderStatViews.completedAggregate(from, to, filter.status());

        return new DashboardSummary(
                counts.get(OrderViewStatus.PENDING),
                counts.get(OrderViewStatus.IN_PRODUCTION),
                counts.get(OrderViewStatus.IN_DELIVERY),
                counts.get(OrderViewStatus.COMPLETED),
                counts.get(OrderViewStatus.CANCELLED),
                total,
                completed.getTotalQuantity(),
                completed.getOnTimeRate());
    }

    @Override
    public List<ProductDistribution> productDistribution(StatFilter filter) {
        OffsetDateTime from = toStartOfDay(filter.from());
        OffsetDateTime to = toExclusiveEnd(filter.to());

        return orderStatViews.productDistribution(from, to, filter.status()).stream()
                .map(row -> new ProductDistribution(
                        row.getProductId(), row.getProductName(), row.getOrderCount(), row.getTotalQuantity()))
                .toList();
    }

    /** to는 배타적 상한이라 하루를 더해 그 날짜 자정으로 맞춘다(해당 날짜 전체를 포함). */
    private OffsetDateTime toExclusiveEnd(LocalDate date) {
        return date == null ? null : date.plusDays(1).atStartOfDay(clock.getZone()).toOffsetDateTime();
    }

    private OffsetDateTime toStartOfDay(LocalDate date) {
        return date == null ? null : date.atStartOfDay(clock.getZone()).toOffsetDateTime();
    }
}

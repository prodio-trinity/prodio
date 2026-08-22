package com.prodio.stat.infrastructure.persistence;

import com.prodio.stat.application.StatDashboardRepository;
import com.prodio.stat.domain.DailyProduction;
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
import java.util.TreeMap;

@Repository
@RequiredArgsConstructor
class JpaStatDashboardRepository implements StatDashboardRepository {
    /** 일별 생산량 차트에서 from/to가 둘 다 없을 때 기본으로 보여줄 범위(오늘 포함 최근 N일). */
    private static final int DEFAULT_DAILY_RANGE_DAYS = 14;

    private final OrderStatViewJpaRepository orderStatViews;
    private final Clock clock;

    @Override
    public DashboardSummary summarize(StatFilter filter) {
        OffsetDateTime from = toStartOfDay(filter.from());
        OffsetDateTime to = toExclusiveEnd(filter.to());

        String status = filter.status() == null ? null : filter.status().name();

        Map<OrderViewStatus, Long> counts = new EnumMap<>(OrderViewStatus.class);
        for (OrderViewStatus value : OrderViewStatus.values()) {
            counts.put(value, 0L);
        }
        long total = 0;
        for (OrderStatViewJpaRepository.StatusCount row : orderStatViews.countByStatus(from, to, status)) {
            counts.put(row.getStatus(), row.getCount());
            total += row.getCount();
        }

        OrderStatViewJpaRepository.CompletedAggregate completed =
                orderStatViews.completedAggregate(from, to, status);

        return new DashboardSummary(
                counts.get(OrderViewStatus.PENDING),
                counts.get(OrderViewStatus.IN_PRODUCTION),
                counts.get(OrderViewStatus.IN_DELIVERY),
                counts.get(OrderViewStatus.COMPLETED),
                counts.get(OrderViewStatus.CANCELLED),
                total,
                completed.getTotalQuantity());
    }

    @Override
    public List<ProductDistribution> productDistribution(StatFilter filter) {
        OffsetDateTime from = toStartOfDay(filter.from());
        OffsetDateTime to = toExclusiveEnd(filter.to());

        String status = filter.status() == null ? null : filter.status().name();

        return orderStatViews.productDistribution(from, to, status).stream()
                .map(row -> new ProductDistribution(
                        row.getProductId(), row.getProductName(), row.getOrderCount(), row.getTotalQuantity()))
                .toList();
    }

    @Override
    public List<DailyProduction> dailyProduction(StatFilter filter) {
        LocalDate to = filter.to() != null ? filter.to() : LocalDate.now(clock);
        LocalDate from = filter.from() != null ? filter.from() : to.minusDays(DEFAULT_DAILY_RANGE_DAYS - 1L);

        String status = filter.status() == null ? null : filter.status().name();

        Map<LocalDate, Long> quantityByDate = new TreeMap<>();
        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            quantityByDate.put(date, 0L);
        }

        List<OrderStatViewJpaRepository.CompletedRow> rows =
                orderStatViews.findCompletedInRange(toStartOfDay(from), toExclusiveEnd(to), status);
        for (OrderStatViewJpaRepository.CompletedRow row : rows) {
            LocalDate date = row.getCompletedAt().atZone(clock.getZone()).toLocalDate();
            quantityByDate.merge(date, (long) row.getQuantity(), Long::sum);
        }

        return quantityByDate.entrySet().stream()
                .map(entry -> new DailyProduction(entry.getKey(), entry.getValue()))
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

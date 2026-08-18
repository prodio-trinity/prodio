package com.prodio.stat.infrastructure.persistence;

import com.prodio.stat.domain.OrderViewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

interface OrderStatViewJpaRepository extends JpaRepository<OrderStatViewEntity, Long> {
    Optional<OrderStatViewEntity> findByOrderId(long orderId);

    @Query("""
            select v.status as status, count(v) as count
            from OrderStatViewEntity v
            where (:from is null or v.orderCreatedAt >= :from)
              and (:to is null or v.orderCreatedAt < :to)
              and (:status is null or v.status = :status)
            group by v.status
            """)
    List<StatusCount> countByStatus(@Param("from") OffsetDateTime from, @Param("to") OffsetDateTime to,
            @Param("status") OrderViewStatus status);

    @Query("""
            select coalesce(sum(v.quantity), 0) as totalQuantity,
                   avg(case when v.onTime = true then 1.0 else 0.0 end) as onTimeRate
            from OrderStatViewEntity v
            where v.status = com.prodio.stat.domain.OrderViewStatus.COMPLETED
              and (:from is null or v.orderCreatedAt >= :from)
              and (:to is null or v.orderCreatedAt < :to)
              and (:status is null or v.status = :status)
            """)
    CompletedAggregate completedAggregate(@Param("from") OffsetDateTime from, @Param("to") OffsetDateTime to,
            @Param("status") OrderViewStatus status);

    @Query("""
            select v.productId as productId, v.productName as productName,
                   count(v) as orderCount, coalesce(sum(v.quantity), 0) as totalQuantity
            from OrderStatViewEntity v
            where (:from is null or v.orderCreatedAt >= :from)
              and (:to is null or v.orderCreatedAt < :to)
              and (:status is null or v.status = :status)
            group by v.productId, v.productName
            """)
    List<ProductDistributionRow> productDistribution(@Param("from") OffsetDateTime from, @Param("to") OffsetDateTime to,
            @Param("status") OrderViewStatus status);

    interface StatusCount {
        OrderViewStatus getStatus();
        long getCount();
    }

    interface CompletedAggregate {
        long getTotalQuantity();
        Double getOnTimeRate();
    }

    interface ProductDistributionRow {
        long getProductId();
        String getProductName();
        long getOrderCount();
        long getTotalQuantity();
    }
}

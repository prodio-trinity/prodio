package com.prodio.stat.infrastructure.persistence;

import com.prodio.stat.domain.OrderViewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;

interface OrderStatViewJpaRepository extends JpaRepository<OrderStatViewEntity, Long> {
    List<OrderStatViewEntity> findAllByOrderId(long orderId);
    void deleteAllByOrderId(long orderId);

    /**
     * from/to/status 파라미터를 JPQL에 두 번(is null 체크 + 비교) 쓰면 Hibernate가 이를 서로 다른
     * JDBC 파라미터로 각각 바인딩하는데, is null 쪽은 타입을 유추할 문맥이 없어 PostgreSQL이
     * "could not determine data type of parameter"를 던진다. native SQL로 CAST를 명시해 우회한다.
     */
    @Query(value = """
            select status, count(id) as count
            from statistics_order_view
            where (cast(:from as timestamptz) is null or order_created_at >= cast(:from as timestamptz))
              and (cast(:to as timestamptz) is null or order_created_at < cast(:to as timestamptz))
              and (cast(:status as varchar) is null or status = cast(:status as varchar))
            group by status
            """, nativeQuery = true)
    List<StatusCount> countByStatus(@Param("from") OffsetDateTime from, @Param("to") OffsetDateTime to,
            @Param("status") String status);

    @Query(value = """
            select coalesce(sum(quantity), 0) as totalQuantity
            from statistics_order_view
            where status = 'COMPLETED'
              and (cast(:from as timestamptz) is null or order_created_at >= cast(:from as timestamptz))
              and (cast(:to as timestamptz) is null or order_created_at < cast(:to as timestamptz))
              and (cast(:status as varchar) is null or status = cast(:status as varchar))
            """, nativeQuery = true)
    CompletedAggregate completedAggregate(@Param("from") OffsetDateTime from, @Param("to") OffsetDateTime to,
            @Param("status") String status);

    @Query(value = """
            select product_id as productId, product_name as productName,
                   count(id) as orderCount, coalesce(sum(quantity), 0) as totalQuantity
            from statistics_order_view
            where (cast(:from as timestamptz) is null or order_created_at >= cast(:from as timestamptz))
              and (cast(:to as timestamptz) is null or order_created_at < cast(:to as timestamptz))
              and (cast(:status as varchar) is null or status = cast(:status as varchar))
            group by product_id, product_name
            """, nativeQuery = true)
    List<ProductDistributionRow> productDistribution(@Param("from") OffsetDateTime from, @Param("to") OffsetDateTime to,
            @Param("status") String status);

    /** 일별 생산량 집계는 애플리케이션(Clock의 zone)에서 날짜로 묶는다 — 완료 건의 원본 행만 가져온다. */
    @Query(value = """
            select completed_at as completedAt, quantity as quantity
            from statistics_order_view
            where status = 'COMPLETED'
              and completed_at is not null
              and (cast(:from as timestamptz) is null or completed_at >= cast(:from as timestamptz))
              and (cast(:to as timestamptz) is null or completed_at < cast(:to as timestamptz))
              and (cast(:status as varchar) is null or status = cast(:status as varchar))
            """, nativeQuery = true)
    List<CompletedRow> findCompletedInRange(@Param("from") OffsetDateTime from, @Param("to") OffsetDateTime to,
            @Param("status") String status);

    interface StatusCount {
        OrderViewStatus getStatus();
        long getCount();
    }

    interface CompletedAggregate {
        long getTotalQuantity();
    }

    interface ProductDistributionRow {
        long getProductId();
        String getProductName();
        long getOrderCount();
        long getTotalQuantity();
    }

    /**
     * completed_at(timestamptz)을 native projection으로 받으면 Hibernate가 OffsetDateTime이 아니라
     * Instant로 매핑해서 넘긴다 — Spring Data가 그 둘을 자동 변환해주지 않아 Instant로 선언해야 한다.
     */
    interface CompletedRow {
        Instant getCompletedAt();
        int getQuantity();
    }
}

package com.prodio.stat.infrastructure.persistence;

import com.prodio.stat.domain.OrderStatView;
import com.prodio.stat.domain.OrderViewStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Getter
@Table(name = "statistics_order_view")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class OrderStatViewEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private long orderId;

    @Column(name = "client_id", nullable = false)
    private long clientId;

    @Column(name = "client_name", nullable = false)
    private String clientName;

    @Column(name = "product_id", nullable = false)
    private long productId;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "line_amount", nullable = false)
    private long lineAmount;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderViewStatus status;

    @Column(name = "payment_confirmed", nullable = false)
    private boolean paymentConfirmed;

    @Column(name = "on_time")
    private Boolean onTime;

    @Column(name = "order_created_at", nullable = false)
    private OffsetDateTime orderCreatedAt;

    @Column(name = "production_started_at")
    private OffsetDateTime productionStartedAt;

    @Column(name = "shipped_at")
    private OffsetDateTime shippedAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Column(name = "cancellation_reason")
    private String cancellationReason;

    @Column(name = "cancelled_at")
    private OffsetDateTime cancelledAt;

    /** 최초 생성(OrderCreated) 시점 스냅샷. status는 항상 PENDING으로 시작한다. */
    private OrderStatViewEntity(OrderStatView view) {
        orderId = view.orderId();
        clientId = view.clientId();
        clientName = view.clientName();
        productId = view.productId();
        productName = view.productName();
        quantity = view.quantity();
        lineAmount = view.lineAmount();
        dueDate = view.dueDate();
        status = OrderViewStatus.PENDING;
        paymentConfirmed = false;
        orderCreatedAt = view.orderCreatedAt();
    }

    static OrderStatViewEntity from(OrderStatView view) {
        return new OrderStatViewEntity(view);
    }
    
    void markProductionStarted(OffsetDateTime startedAt) {
        if (isTerminal()) return;
        this.status = OrderViewStatus.IN_PRODUCTION;
        this.productionStartedAt = startedAt;
    }

    void markShipped(OffsetDateTime shippedAt) {
        if (isTerminal()) return;
        this.status = OrderViewStatus.IN_DELIVERY;
        this.shippedAt = shippedAt;
    }

    void markCompleted(OffsetDateTime completedAt, boolean onTime) {
        if (isTerminal()) return;
        this.status = OrderViewStatus.COMPLETED;
        this.completedAt = completedAt;
        this.onTime = onTime;
    }

    void markCancelled(String cancellationReason, OffsetDateTime cancelledAt) {
        if (isTerminal()) return;
        this.status = OrderViewStatus.CANCELLED;
        this.cancellationReason = cancellationReason;
        this.cancelledAt = cancelledAt;
    }

    private boolean isTerminal() {
        return status == OrderViewStatus.CANCELLED || status == OrderViewStatus.COMPLETED;
    }

    void confirmPayment() {
        this.paymentConfirmed = true;
    }

    OrderStatView toDomain() {
        return new OrderStatView(id, orderId, clientId, clientName, productId, productName,
                quantity, lineAmount, dueDate, status, paymentConfirmed, onTime,
                orderCreatedAt, productionStartedAt, shippedAt, completedAt,
                cancellationReason, cancelledAt);
    }
}

package com.prodio.order.domain;

import com.prodio.order.exception.OrderErrorCode;
import com.prodio.order.exception.OrderException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public final class Order {
    private final long id;
    private final long clientId;
    private final String clientNameSnapshot;
    private final String clientPhoneSnapshot;
    private List<OrderItem> items;
    private boolean vatIncluded;
    private long totalAmount;
    private LocalDate dueDate;
    private DeliverySnapshot delivery;
    private String note;
    private OrderStatus status;
    private String cancellationReason;
    private final long createdBy;
    private final OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    private Order(long id, long clientId, String clientNameSnapshot, String clientPhoneSnapshot,
            List<OrderItem> items, boolean vatIncluded, long totalAmount, LocalDate dueDate,
            DeliverySnapshot delivery,
            String note, OrderStatus status, String cancellationReason, long createdBy,
            OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        if (id < 0 || clientId <= 0 || createdBy <= 0) {
            throw new IllegalArgumentException("수주 식별자는 양수여야 합니다.");
        }
        if (totalAmount < 0) {
            throw new IllegalArgumentException("수주 금액과 수량이 올바르지 않습니다.");
        }
        this.id = id;
        this.clientId = clientId;
        this.clientNameSnapshot = requireText(clientNameSnapshot, "거래처명이 필요합니다.");
        this.clientPhoneSnapshot = normalize(clientPhoneSnapshot);
        this.items = validateItems(items);
        this.vatIncluded = vatIncluded;
        this.totalAmount = totalAmount;
        this.dueDate = Objects.requireNonNull(dueDate, "납기일이 필요합니다.");
        this.delivery = Objects.requireNonNull(delivery, "배송 정보가 필요합니다.");
        this.note = normalize(note);
        this.status = Objects.requireNonNull(status, "수주 상태가 필요합니다.");
        this.cancellationReason = normalizeNullable(cancellationReason);
        if (status == OrderStatus.CANCELLED && this.cancellationReason == null) {
            throw new IllegalArgumentException("취소 사유가 필요합니다.");
        }
        this.createdBy = createdBy;
        this.createdAt = Objects.requireNonNull(createdAt, "등록 시각이 필요합니다.");
        this.updatedAt = Objects.requireNonNull(updatedAt, "수정 시각이 필요합니다.");
    }

    public static Order place(long clientId, String clientName, String clientPhone,
            List<OrderItem> items, boolean vatIncluded, LocalDate dueDate,
            DeliverySnapshot delivery, String note,
            long createdBy, OffsetDateTime now) {
        long totalAmount = calculateTotal(items, vatIncluded);
        return new Order(0, clientId, clientName, clientPhone, items,
                vatIncluded, totalAmount, dueDate, delivery,
                note, OrderStatus.PENDING_PAYMENT, null, createdBy, now, now);
    }

    public static Order reconstitute(long id, long clientId, String clientName, String clientPhone,
            List<OrderItem> items, boolean vatIncluded, long totalAmount, LocalDate dueDate,
            DeliverySnapshot delivery,
            String note, OrderStatus status, String cancellationReason, long createdBy,
            OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        return new Order(id, clientId, clientName, clientPhone, items,
                vatIncluded, totalAmount, dueDate, delivery,
                note, status, cancellationReason, createdBy, createdAt, updatedAt);
    }

    public void update(List<OrderItem> items, boolean vatIncluded, LocalDate dueDate,
            DeliverySnapshot delivery, String note,
            OffsetDateTime now) {
        ensurePendingPayment();
        this.items = validateItems(items);
        this.vatIncluded = vatIncluded;
        this.totalAmount = calculateTotal(items, vatIncluded);
        this.dueDate = Objects.requireNonNull(dueDate, "납기일이 필요합니다.");
        this.delivery = Objects.requireNonNull(delivery, "배송 정보가 필요합니다.");
        this.note = normalize(note);
        updatedAt = Objects.requireNonNull(now);
    }

    public void confirm(OffsetDateTime now) {
        ensurePendingPayment();
        status = OrderStatus.CONFIRMED;
        updatedAt = Objects.requireNonNull(now);
    }

    public void cancel(String reason, OffsetDateTime now) {
        ensurePendingPayment();
        cancellationReason = requireText(reason, "취소 사유가 필요합니다.");
        status = OrderStatus.CANCELLED;
        updatedAt = Objects.requireNonNull(now);
    }

    private void ensurePendingPayment() {
        if (status != OrderStatus.PENDING_PAYMENT) {
            throw new OrderException(OrderErrorCode.INVALID_ORDER_STATUS,
                    "입금 대기 상태의 주문만 변경할 수 있습니다.");
        }
    }

    static long calculateTotal(List<OrderItem> items, boolean vatIncluded) {
        List<OrderItem> validated = validateItems(items);
        long subtotal;
        try {
            subtotal = validated.stream().mapToLong(OrderItem::lineAmount)
                    .reduce(0L, Math::addExact);
        } catch (ArithmeticException exception) {
            throw new OrderException(OrderErrorCode.INVALID_ORDER_REQUEST, "계산된 수주 금액이 너무 큽니다.");
        }
        BigDecimal amount = BigDecimal.valueOf(subtotal);
        if (vatIncluded) amount = amount.multiply(new BigDecimal("1.1"));
        try {
            return amount.setScale(0, RoundingMode.HALF_UP).longValueExact();
        } catch (ArithmeticException exception) {
            throw new OrderException(OrderErrorCode.INVALID_ORDER_REQUEST, "계산된 수주 금액이 너무 큽니다.");
        }
    }

    private static List<OrderItem> validateItems(List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("주문 품목이 하나 이상 필요합니다.");
        }
        List<OrderItem> copy = List.copyOf(items);
        HashSet<Long> productIds = new HashSet<>();
        if (copy.stream().anyMatch(item -> !productIds.add(item.productId()))) {
            throw new IllegalArgumentException("같은 품목을 중복해서 추가할 수 없습니다.");
        }
        return copy;
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(message);
        return value.trim();
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private static String normalizeNullable(String value) {
        String normalized = normalize(value);
        return normalized.isEmpty() ? null : normalized;
    }

    public long id() { return id; }
    public long clientId() { return clientId; }
    public String clientNameSnapshot() { return clientNameSnapshot; }
    public String clientPhoneSnapshot() { return clientPhoneSnapshot; }
    public List<OrderItem> items() { return items; }
    public boolean vatIncluded() { return vatIncluded; }
    public long totalAmount() { return totalAmount; }
    public LocalDate dueDate() { return dueDate; }
    public DeliverySnapshot delivery() { return delivery; }
    public String deliveryAddress() { return delivery.addressLine1(); }
    public String note() { return note; }
    public OrderStatus status() { return status; }
    public String cancellationReason() { return cancellationReason; }
    public long createdBy() { return createdBy; }
    public OffsetDateTime createdAt() { return createdAt; }
    public OffsetDateTime updatedAt() { return updatedAt; }
}

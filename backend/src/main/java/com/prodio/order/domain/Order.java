package com.prodio.order.domain;

import com.prodio.order.exception.OrderErrorCode;
import com.prodio.order.exception.OrderException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Objects;

public final class Order {
    private final long id;
    private final long clientId;
    private final String clientNameSnapshot;
    private final String clientPhoneSnapshot;
    private final long productId;
    private final String productNameSnapshot;
    private final long unitPriceSnapshot;
    private final int quantity;
    private final boolean vatIncluded;
    private final long totalAmount;
    private final LocalDate dueDate;
    private final String deliveryAddress;
    private final String note;
    private OrderStatus status;
    private boolean paymentConfirmed;
    private final long createdBy;
    private final OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    private Order(long id, long clientId, String clientNameSnapshot, String clientPhoneSnapshot,
            long productId, String productNameSnapshot, long unitPriceSnapshot, int quantity,
            boolean vatIncluded, long totalAmount, LocalDate dueDate, String deliveryAddress,
            String note, OrderStatus status, boolean paymentConfirmed, long createdBy,
            OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        if (id < 0 || clientId <= 0 || productId <= 0 || createdBy <= 0) {
            throw new IllegalArgumentException("수주 식별자는 양수여야 합니다.");
        }
        if (unitPriceSnapshot < 0 || quantity <= 0 || totalAmount < 0) {
            throw new IllegalArgumentException("수주 금액과 수량이 올바르지 않습니다.");
        }
        this.id = id;
        this.clientId = clientId;
        this.clientNameSnapshot = requireText(clientNameSnapshot, "거래처명이 필요합니다.");
        this.clientPhoneSnapshot = normalize(clientPhoneSnapshot);
        this.productId = productId;
        this.productNameSnapshot = requireText(productNameSnapshot, "품목명이 필요합니다.");
        this.unitPriceSnapshot = unitPriceSnapshot;
        this.quantity = quantity;
        this.vatIncluded = vatIncluded;
        this.totalAmount = totalAmount;
        this.dueDate = Objects.requireNonNull(dueDate, "납기일이 필요합니다.");
        this.deliveryAddress = normalize(deliveryAddress);
        this.note = normalize(note);
        this.status = Objects.requireNonNull(status, "수주 상태가 필요합니다.");
        this.paymentConfirmed = paymentConfirmed;
        this.createdBy = createdBy;
        this.createdAt = Objects.requireNonNull(createdAt, "등록 시각이 필요합니다.");
        this.updatedAt = Objects.requireNonNull(updatedAt, "수정 시각이 필요합니다.");
    }

    public static Order place(long clientId, String clientName, String clientPhone,
            long productId, String productName, long unitPrice, int quantity,
            boolean vatIncluded, LocalDate dueDate, String deliveryAddress, String note,
            long createdBy, OffsetDateTime now) {
        long totalAmount = calculateTotal(unitPrice, quantity, vatIncluded);
        return new Order(0, clientId, clientName, clientPhone, productId, productName,
                unitPrice, quantity, vatIncluded, totalAmount, dueDate, deliveryAddress,
                note, OrderStatus.PENDING, false, createdBy, now, now);
    }

    public static Order reconstitute(long id, long clientId, String clientName, String clientPhone,
            long productId, String productName, long unitPrice, int quantity,
            boolean vatIncluded, long totalAmount, LocalDate dueDate, String deliveryAddress,
            String note, OrderStatus status, boolean paymentConfirmed, long createdBy,
            OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        return new Order(id, clientId, clientName, clientPhone, productId, productName,
                unitPrice, quantity, vatIncluded, totalAmount, dueDate, deliveryAddress,
                note, status, paymentConfirmed, createdBy, createdAt, updatedAt);
    }

    public void startProduction(OffsetDateTime now) {
        if (status != OrderStatus.PENDING) {
            throw new OrderException(OrderErrorCode.INVALID_ORDER_STATUS);
        }
        status = OrderStatus.IN_PRODUCTION;
        updatedAt = Objects.requireNonNull(now);
    }

    public void changePaymentConfirmation(boolean confirmed, OffsetDateTime now) {
        paymentConfirmed = confirmed;
        updatedAt = Objects.requireNonNull(now);
    }

    static long calculateTotal(long unitPrice, int quantity, boolean vatIncluded) {
        if (unitPrice < 0 || quantity <= 0) {
            throw new IllegalArgumentException("단가와 수량이 올바르지 않습니다.");
        }
        BigDecimal amount = BigDecimal.valueOf(unitPrice).multiply(BigDecimal.valueOf(quantity));
        if (vatIncluded) amount = amount.multiply(new BigDecimal("1.1"));
        try {
            return amount.setScale(0, RoundingMode.HALF_UP).longValueExact();
        } catch (ArithmeticException exception) {
            throw new OrderException(OrderErrorCode.INVALID_ORDER_REQUEST, "계산된 수주 금액이 너무 큽니다.");
        }
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(message);
        return value.trim();
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    public long id() { return id; }
    public long clientId() { return clientId; }
    public String clientNameSnapshot() { return clientNameSnapshot; }
    public String clientPhoneSnapshot() { return clientPhoneSnapshot; }
    public long productId() { return productId; }
    public String productNameSnapshot() { return productNameSnapshot; }
    public long unitPriceSnapshot() { return unitPriceSnapshot; }
    public int quantity() { return quantity; }
    public boolean vatIncluded() { return vatIncluded; }
    public long totalAmount() { return totalAmount; }
    public LocalDate dueDate() { return dueDate; }
    public String deliveryAddress() { return deliveryAddress; }
    public String note() { return note; }
    public OrderStatus status() { return status; }
    public boolean paymentConfirmed() { return paymentConfirmed; }
    public long createdBy() { return createdBy; }
    public OffsetDateTime createdAt() { return createdAt; }
    public OffsetDateTime updatedAt() { return updatedAt; }
}

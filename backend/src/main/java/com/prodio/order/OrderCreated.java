package com.prodio.order;

import com.prodio.order.domain.Order;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/** 생산 시작이 승인된 수주의 공개 도메인 이벤트. 이름은 현재 프로젝트 API 계약을 따른다. */
public record OrderCreated(long orderId, long clientId, String clientName,
        String clientPhone, long productId, String productName, int quantity,
        long totalAmount, LocalDate dueDate, OffsetDateTime occurredAt) {

    public static OrderCreated from(Order order) {
        return new OrderCreated(order.id(), order.clientId(), order.clientNameSnapshot(),
                order.clientPhoneSnapshot(), order.productId(), order.productNameSnapshot(),
                order.quantity(), order.totalAmount(), order.dueDate(), order.updatedAt());
    }
}

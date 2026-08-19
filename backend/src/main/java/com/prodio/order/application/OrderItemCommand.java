package com.prodio.order.application;

public record OrderItemCommand(long productId, int quantity) {
}

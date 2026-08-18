package com.prodio.stat.domain;

/** 품목별 수주 분포 — 필터 조건에 해당하는 주문을 품목별로 묶은 건수/수량. */
public record ProductDistribution(
        long productId,
        String productName,
        long orderCount,
        long totalQuantity
) {}

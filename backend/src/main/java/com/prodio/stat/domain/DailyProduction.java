package com.prodio.stat.domain;

import java.time.LocalDate;

/** 완료된 주문 기준, 하루 단위 생산량(수량 합). */
public record DailyProduction(LocalDate date, long quantity) {}

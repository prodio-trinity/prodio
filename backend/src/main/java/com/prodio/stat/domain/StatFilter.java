package com.prodio.stat.domain;

import java.time.LocalDate;

/** 대시보드/품목별 분포 조회 공통 필터. from/to/status 모두 선택값이다. */
public record StatFilter(LocalDate from, LocalDate to, OrderViewStatus status) {}

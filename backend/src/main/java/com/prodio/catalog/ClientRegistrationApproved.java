package com.prodio.catalog;

/** 거래처 등록 신청 승인됨 — user 모듈이 구독해서 역할을 PENDING → CLIENT로 승격 */
public record ClientRegistrationApproved(long userId) {
}
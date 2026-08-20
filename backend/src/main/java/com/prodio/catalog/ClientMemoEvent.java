package com.prodio.catalog;

/** 거래처 memo가 실제로 변경됐을 때만 발행 */
public record ClientMemoEvent(Long clientId, String companyName, String memo) {
}
package com.prodio.catalog.application;

import com.prodio.catalog.domain.ClientRegistration;

/** ADMIN 목록 화면용 — ClientRegistration + user 모듈에서 조회한 신청자 이메일. */
public record ClientRegistrationListItem(ClientRegistration registration, String userEmail) {
}
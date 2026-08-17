package com.prodio.user.application;

import java.util.Optional;

/** 로그인 인증(Spring Security {@code UserDetailsService})에서만 쓰는 조회 창구. */
public interface UserCredentialRepository {
    Optional<UserCredential> findByEmail(String email);
}

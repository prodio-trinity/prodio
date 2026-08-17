package com.prodio.user.application;

import java.util.Set;
import com.prodio.user.UserRole;
import com.prodio.user.domain.UserAccount;

/**
 * 인증 전용 읽기 모델. 비밀번호 해시를 담고 있어 공개 {@link com.prodio.user.UserDirectory}와는
 * 별도 포트({@link UserCredentialRepository})로만 노출한다.
 */
public record UserCredential(long id, String email, String passwordHash, String name,
        UserAccount.Status status, Set<UserRole> roles) {
}

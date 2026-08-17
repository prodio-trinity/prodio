package com.prodio.user.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import com.prodio.shared.error.BusinessException;
import com.prodio.shared.error.ErrorCode;
import com.prodio.user.application.UserAccountRepository;
import com.prodio.user.domain.UserAccount;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class JpaUserAccountRepository implements UserAccountRepository {
    private final SpringDataUserAccountRepository userAccounts;
    private final EntityManager entityManager;

    @Override
    public Optional<UserAccount> findByEmail(String email) {
        return userAccounts.findByEmailIgnoreCase(email).map(UserAccountEntity::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userAccounts.existsByEmailIgnoreCase(email);
    }

    @Override
    public UserAccount insert(String email, String passwordHash, String name) {
        // 동시 가입 요청에도 email 유일성을 안전하게 지키기 위한 원자적 삽입이다.
        @SuppressWarnings("unchecked")
        List<UserAccountEntity> inserted = entityManager.createNativeQuery("""
                INSERT INTO user_accounts (email, password_hash, name, status)
                VALUES (:email, :passwordHash, :name, 'ACTIVE')
                ON CONFLICT (email) DO NOTHING
                RETURNING id, email, password_hash, name, status
                """, UserAccountEntity.class)
                .setParameter("email", email)
                .setParameter("passwordHash", passwordHash)
                .setParameter("name", name)
                .getResultList();
        return inserted.stream().findFirst().map(UserAccountEntity::toDomain)
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_EMAIL_ALREADY_EXISTS, "이미 가입된 이메일입니다."));
    }
}

package com.prodio.user.infrastructure.persistence;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import com.prodio.user.UserRole;
import com.prodio.user.application.UserCredential;
import com.prodio.user.application.UserCredentialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class JpaUserCredentialRepository implements UserCredentialRepository {
    private final SpringDataUserAccountRepository userAccounts;

    @Override
    public Optional<UserCredential> findByEmail(String email) {
        return userAccounts.findWithRolesByEmail(email).map(JpaUserCredentialRepository::toCredential);
    }

    private static UserCredential toCredential(UserAccountEntity user) {
        Set<UserRole> roles = new LinkedHashSet<>();
        user.getRoles().stream().map(UserRoleEntity::getCode).forEach(roles::add);
        return new UserCredential(user.getId(), user.getEmail(), user.getPasswordHash(), user.getName(),
                user.getStatus(), roles);
    }
}

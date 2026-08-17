package com.prodio.user.application;

import java.util.Optional;
import com.prodio.user.domain.UserAccount;

public interface UserAccountRepository {
    Optional<UserAccount> findByEmail(String email);
    boolean existsByEmail(String email);
    UserAccount insert(String email, String passwordHash, String name);
}

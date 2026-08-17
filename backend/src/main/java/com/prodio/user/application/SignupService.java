package com.prodio.user.application;

import com.prodio.shared.error.BusinessException;
import com.prodio.shared.error.ErrorCode;
import com.prodio.user.domain.UserAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SignupService {
    private final UserAccountRepository userAccountRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserAccount signup(SignupCommand command) {
        if (userAccountRepository.existsByEmail(command.email())) {
            throw new BusinessException(ErrorCode.AUTH_EMAIL_ALREADY_EXISTS, "이미 가입된 이메일입니다.");
        }
        String passwordHash = passwordEncoder.encode(command.password());
        UserAccount created = userAccountRepository.insert(command.email(), passwordHash, command.name());
        userRoleRepository.assignDefaultRole(created.id());
        return created;
    }
}

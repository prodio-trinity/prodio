package com.prodio.user.infrastructure.security;

import com.prodio.user.application.UserCredential;
import com.prodio.user.application.UserCredentialRepository;
import com.prodio.user.domain.UserAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class ProdioUserDetailsService implements UserDetailsService {
    private final UserCredentialRepository userCredentialRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserCredential credential = userCredentialRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("이메일 또는 비밀번호가 올바르지 않습니다."));
        if (credential.status() != UserAccount.Status.ACTIVE) {
            throw new DisabledException("비활성화된 사용자입니다.");
        }
        return ProdioPrincipal.from(credential);
    }
}

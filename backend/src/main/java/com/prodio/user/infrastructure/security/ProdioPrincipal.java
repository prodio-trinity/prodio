package com.prodio.user.infrastructure.security;

import java.util.List;
import java.util.Set;
import com.prodio.user.UserRole;
import com.prodio.user.application.UserCredential;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/** Spring Security {@code UserDetails} 구현체. 세션에 저장되는 인증 principal이다. */
public final class ProdioPrincipal implements UserDetails {
    private final long userId;
    private final String email;
    private final String name;
    private final String passwordHash;
    private final Set<UserRole> roles;

    public ProdioPrincipal(long userId, String email, String name, String passwordHash, Set<UserRole> roles) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.passwordHash = passwordHash;
        this.roles = Set.copyOf(roles);
    }

    static ProdioPrincipal from(UserCredential credential) {
        return new ProdioPrincipal(credential.id(), credential.email(), credential.name(),
                credential.passwordHash(), credential.roles());
    }

    public long userId() {
        return userId;
    }

    public String email() {
        return email;
    }

    public String name() {
        return name;
    }

    @Override
    public List<GrantedAuthority> getAuthorities() {
        return roles.stream().map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role.name())).toList();
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }
}

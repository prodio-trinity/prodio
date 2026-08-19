package com.prodio.user.domain;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import com.prodio.user.UserRole;

/** 한 사용자가 가진 권한 집합. */
public final class UserRoles {
    private final Set<UserRole> codes;

    private UserRoles(Set<UserRole> codes) {
        this.codes = codes;
    }

    public static UserRoles of(Collection<UserRole> requested) {
        EnumSet<UserRole> normalized = EnumSet.noneOf(UserRole.class);
        if (requested != null) {
            for (UserRole code : requested) {
                if (code != null) normalized.add(code);
            }
        }
        if (normalized.isEmpty()) normalized.add(UserRole.PENDING);
        return new UserRoles(normalized);
    }

    public boolean has(UserRole code) {
        return codes.contains(code);
    }

    public Set<UserRole> codes() {
        return EnumSet.copyOf(codes);
    }
}

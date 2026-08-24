package com.prodio.user.infrastructure.security;

import com.prodio.user.application.UserSessionInvalidator;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Component;

@Component
class SessionRegistryInvalidator implements UserSessionInvalidator {

    private final SessionRegistry sessionRegistry;

    SessionRegistryInvalidator(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public void invalidateSessionsOf(long userId) {
        sessionRegistry.getAllPrincipals().stream()
                .filter(p -> p instanceof ProdioPrincipal pp && pp.userId() == userId)
                .flatMap(p -> sessionRegistry.getAllSessions(p, false).stream())
                .forEach(session -> session.expireNow());
    }
}

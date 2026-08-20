package com.prodio.user.application;

import com.prodio.catalog.ClientRegistrationApproved;
import com.prodio.user.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class ClientRegistrationApprovedListener {
    private final UserRoleRepository userRoleRepository;

    @ApplicationModuleListener
    public void handle(ClientRegistrationApproved event) {
        userRoleRepository.replaceRoles(event.userId(), Set.of(UserRole.CLIENT));
    }
}
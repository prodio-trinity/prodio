package com.prodio.user.application;

import com.prodio.user.UserRole;
import com.prodio.user.domain.UserAccount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminMemberServiceTest {

    @Mock private AdminMemberQueryRepository queryRepository;
    @Mock private UserRoleRepository userRoleRepository;
    @Mock private UserSessionInvalidator sessionInvalidator;

    private AdminMemberService service;

    @BeforeEach
    void setUp() {
        service = new AdminMemberService(queryRepository, userRoleRepository, sessionInvalidator);
    }

    @Test
    @DisplayName("역할 변경 시 대상 유저의 세션이 즉시 무효화된다")
    void updateRoles_세션_무효화() {
        // Arrange
        long targetId = 2L;
        long actorId = 1L;
        AdminMember target = new AdminMember(targetId, "user@prodio.com", "테스트유저",
                UserAccount.Status.ACTIVE, Set.of(UserRole.PENDING));
        when(queryRepository.findById(targetId)).thenReturn(Optional.of(target));

        // Act
        service.updateRoles(targetId, Set.of(UserRole.CLIENT), actorId);

        // Assert
        verify(sessionInvalidator).invalidateSessionsOf(targetId);
    }

    @Test
    @DisplayName("본인의 ADMIN 권한 해제 시도 시 예외가 발생한다")
    void updateRoles_본인_ADMIN_해제_예외() {
        // Arrange
        long actorId = 1L;
        AdminMember target = new AdminMember(actorId, "admin@prodio.com", "관리자",
                UserAccount.Status.ACTIVE, Set.of(UserRole.ADMIN));
        when(queryRepository.findById(actorId)).thenReturn(Optional.of(target));

        // Act & Assert
        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> service.updateRoles(actorId, Set.of(UserRole.CLIENT), actorId))
                .isInstanceOf(com.prodio.shared.error.BusinessException.class);
    }
}

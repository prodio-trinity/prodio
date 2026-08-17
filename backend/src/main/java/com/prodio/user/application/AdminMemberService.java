package com.prodio.user.application;

import java.util.Collection;
import com.prodio.shared.error.BusinessException;
import com.prodio.shared.error.ErrorCode;
import com.prodio.user.UserRole;
import com.prodio.user.domain.UserRoles;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminMemberService {
    private final AdminMemberQueryRepository queryRepository;
    private final UserRoleRepository userRoleRepository;

    public AdminMemberPage list(String query, int page, int size) {
        return queryRepository.findMembers(query, page, size);
    }

    @Transactional
    public AdminMember updateRoles(long targetUserId, Collection<UserRole> requested, long actorUserId) {
        AdminMember target = queryRepository.findById(targetUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_MEMBER_NOT_FOUND, "회원을 찾을 수 없습니다."));

        UserRoles roles = UserRoles.of(requested);
        if (targetUserId == actorUserId && !roles.has(UserRole.ADMIN)) {
            throw new BusinessException(ErrorCode.ADMIN_SELF_DEMOTION,
                    "본인의 ADMIN 권한은 해제할 수 없습니다.");
        }

        userRoleRepository.replaceRoles(targetUserId, roles.codes());

        return new AdminMember(target.id(), target.email(), target.name(), target.status(), roles.codes());
    }
}

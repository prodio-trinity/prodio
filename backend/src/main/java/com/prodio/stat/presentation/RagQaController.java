package com.prodio.stat.presentation;

import com.prodio.shared.ApiResponse;
import com.prodio.stat.application.AiQueryLogPage;
import com.prodio.stat.application.RagQaService;
import com.prodio.stat.domain.AiQueryLog;
import com.prodio.stat.domain.SourceType;
import com.prodio.stat.exception.StatErrorCode;
import com.prodio.stat.exception.StatException;
import com.prodio.user.UserDirectory;
import com.prodio.user.UserRef;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
class RagQaController {

    private final RagQaService ragQaService;
    private final UserDirectory userDirectory;

    @PostMapping("/ask")
    ApiResponse<AskResponse> ask(@Valid @RequestBody AskRequest request, Authentication authentication) {
        long adminId = currentAdmin(authentication).id();

        return ApiResponse.success(AskResponse.from(ragQaService.ask(adminId, request.question())));
    }

    @GetMapping("/ask/logs")
    ApiResponse<AskLogPageResponse> askLogs(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        Authentication authentication
    ) {
        long adminId = currentAdmin(authentication).id();

        return ApiResponse.success(AskLogPageResponse.from(ragQaService.getAskLogs(adminId, page, size)));
    }

    /** 질의응답 이력은 관리자 개인 것만 조회 가능해야 해서, 세션의 이메일로 요청자를 확정한다. */
    private UserRef currentAdmin(Authentication authentication) {
        return userDirectory.findActiveByEmail(authentication.getName())
                .orElseThrow(() -> new StatException(StatErrorCode.STAT_REQUESTER_NOT_FOUND));
    }

    record AskRequest(@NotBlank String question) {}

    record AskResponse(
        UUID id,
        SourceType sourceType,
        String question,
        String response,
        OffsetDateTime requestedAt
    ) {
        static AskResponse from(AiQueryLog log) {
            return new AskResponse(log.id(), log.sourceType(), log.question(), log.response(), log.requestedAt());
        }
    }

    record AskLogPageResponse(
        List<AskResponse> logs,
        int page,
        int size,
        long totalElements
    ) {
        static AskLogPageResponse from(AiQueryLogPage aiQueryLogPage) {
            List<AskResponse> logs = aiQueryLogPage.logs().stream().map(AskResponse::from).toList();

            return new AskLogPageResponse(logs, aiQueryLogPage.page(), aiQueryLogPage.size(), aiQueryLogPage.totalElements());
        }
    }
}

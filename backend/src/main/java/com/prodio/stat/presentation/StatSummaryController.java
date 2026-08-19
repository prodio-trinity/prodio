package com.prodio.stat.presentation;

import com.prodio.shared.ApiResponse;
import com.prodio.stat.application.AiQueryLogPage;
import com.prodio.stat.application.StatSummaryService;
import com.prodio.stat.domain.AiQueryLog;
import com.prodio.stat.domain.StatFilter;
import com.prodio.stat.exception.StatErrorCode;
import com.prodio.stat.exception.StatException;
import com.prodio.user.UserDirectory;
import com.prodio.user.UserRef;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
class StatSummaryController {

    private final StatSummaryService statSummaryService;
    private final UserDirectory userDirectory;

    @PostMapping("/summary")
    ApiResponse<SummaryResponse> summary(
        @RequestParam(required = false) LocalDate from,
        @RequestParam(required = false) LocalDate to,
        @RequestParam(required = false) String status,
        Authentication authentication
    ) {
        StatFilter filter = new StatFilter(from, to, StatFilterSupport.parseStatus(status));
        long adminId = currentAdmin(authentication).id();

        return ApiResponse.success(SummaryResponse.from(statSummaryService.summarize(adminId, filter)));
    }

    @GetMapping("/summary/logs")
    ApiResponse<SummaryLogPageResponse> summaryLogs(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        Authentication authentication
    ) {
        long adminId = currentAdmin(authentication).id();

        return ApiResponse.success(SummaryLogPageResponse.from(statSummaryService.getSummaryLogs(adminId, page, size)));
    }

    /** 요약 이력은 관리자 개인 것만 조회 가능해야 해서, 세션의 이메일로 요청자를 확정한다. */
    private UserRef currentAdmin(Authentication authentication) {
        return userDirectory.findActiveByEmail(authentication.getName())
                .orElseThrow(() -> new StatException(StatErrorCode.STAT_REQUESTER_NOT_FOUND));
    }

    record SummaryResponse(
        UUID id,
        String question,
        String response,
        OffsetDateTime requestedAt
    ) {
        static SummaryResponse from(AiQueryLog log) {
            return new SummaryResponse(log.id(), log.question(), log.response(), log.requestedAt());
        }
    }

    record SummaryLogPageResponse(
        List<SummaryResponse> logs,
        int page,
        int size,
        long totalElements
    ) {
        static SummaryLogPageResponse from(AiQueryLogPage aiQueryLogPage) {
            List<SummaryResponse> logs = aiQueryLogPage.logs().stream().map(SummaryResponse::from).toList();

            return new SummaryLogPageResponse(
                logs,
                aiQueryLogPage.page(),
                aiQueryLogPage.size(),
                aiQueryLogPage.totalElements()
            );
        }
    }
}

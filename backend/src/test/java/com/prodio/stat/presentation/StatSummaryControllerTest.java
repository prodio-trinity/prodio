package com.prodio.stat.presentation;

import com.prodio.shared.ApiResponse;
import com.prodio.stat.application.AiQueryLogPage;
import com.prodio.stat.application.StatSummaryService;
import com.prodio.stat.domain.AiQueryLog;
import com.prodio.stat.domain.OrderViewStatus;
import com.prodio.stat.domain.QueryType;
import com.prodio.stat.domain.StatFilter;
import com.prodio.stat.exception.StatErrorCode;
import com.prodio.stat.exception.StatException;
import com.prodio.user.UserDirectory;
import com.prodio.user.UserRef;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("StatSummaryController")
class StatSummaryControllerTest {

    @Mock private StatSummaryService statSummaryService;
    @Mock private UserDirectory userDirectory;
    @Mock private Authentication authentication;
    private StatSummaryController controller;

    @BeforeEach
    void setUp() {
        controller = new StatSummaryController(statSummaryService, userDirectory);
    }

    @Test
    @DisplayName("쿼리 파라미터로 StatFilter를 만들고 로그인한 관리자 id로 AI 요약을 생성한다")
    void summaryBuildsFilterFromQueryParams() {
        LocalDate from = LocalDate.parse("2026-08-01");
        LocalDate to = LocalDate.parse("2026-08-31");
        UUID id = UUID.randomUUID();
        OffsetDateTime requestedAt = OffsetDateTime.parse("2026-08-20T10:00:00+09:00");
        when(authentication.getName()).thenReturn("admin@prodio.com");
        when(userDirectory.findActiveByEmail("admin@prodio.com"))
                .thenReturn(Optional.of(new UserRef(42L, "admin@prodio.com", "관리자")));
        AiQueryLog log = new AiQueryLog(id, QueryType.STATS_SUMMARY, null, 42L,
                "2026-08-01 ~ 2026-08-31, status=COMPLETED", "이번 달 요약입니다.", requestedAt);
        when(statSummaryService.summarize(42L, new StatFilter(from, to, OrderViewStatus.COMPLETED)))
                .thenReturn(log);

        ApiResponse<StatSummaryController.SummaryResponse> response =
                controller.summary(from, to, "completed", authentication);

        assertThat(response.data())
                .isEqualTo(new StatSummaryController.SummaryResponse(
                        id, "2026-08-01 ~ 2026-08-31, status=COMPLETED", "이번 달 요약입니다.", requestedAt));
    }

    @Test
    @DisplayName("알 수 없는 status 값이면 예외를 던진다")
    void unknownStatusThrows() {
        assertThatThrownBy(() -> controller.summary(null, null, "알수없음", authentication))
                .isInstanceOfSatisfying(StatException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(StatErrorCode.STAT_INVALID_FILTER));
    }

    @Test
    @DisplayName("세션 이메일로 활성 관리자를 찾지 못하면 예외를 던진다")
    void unknownRequesterThrows() {
        when(authentication.getName()).thenReturn("ghost@prodio.com");
        when(userDirectory.findActiveByEmail("ghost@prodio.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.summary(null, null, null, authentication))
                .isInstanceOfSatisfying(StatException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(StatErrorCode.STAT_REQUESTER_NOT_FOUND));
    }

    @Test
    @DisplayName("page/size와 로그인한 관리자 id를 그대로 전달해 요약 로그 페이지를 조회한다")
    void summaryLogsDelegatesToService() {
        UUID id = UUID.randomUUID();
        OffsetDateTime requestedAt = OffsetDateTime.parse("2026-08-20T10:00:00+09:00");
        when(authentication.getName()).thenReturn("admin@prodio.com");
        when(userDirectory.findActiveByEmail("admin@prodio.com"))
                .thenReturn(Optional.of(new UserRef(42L, "admin@prodio.com", "관리자")));
        AiQueryLog log = new AiQueryLog(id, QueryType.STATS_SUMMARY, null, 42L, "질문", "응답", requestedAt);
        when(statSummaryService.getSummaryLogs(42L, 1, 20))
                .thenReturn(new AiQueryLogPage(List.of(log), 1, 20, 21));

        ApiResponse<StatSummaryController.SummaryLogPageResponse> response =
                controller.summaryLogs(1, 20, authentication);

        assertThat(response.data()).isEqualTo(new StatSummaryController.SummaryLogPageResponse(
                List.of(new StatSummaryController.SummaryResponse(id, "질문", "응답", requestedAt)),
                1, 20, 21));
    }
}

package com.prodio.stat.presentation;

import com.prodio.shared.ApiResponse;
import com.prodio.stat.application.AiQueryLogPage;
import com.prodio.stat.application.RagQaService;
import com.prodio.stat.domain.AiQueryLog;
import com.prodio.stat.domain.QueryType;
import com.prodio.stat.domain.SourceType;
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

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RagQaController")
class RagQaControllerTest {

    @Mock private RagQaService ragQaService;
    @Mock private UserDirectory userDirectory;
    @Mock private Authentication authentication;
    private RagQaController controller;

    @BeforeEach
    void setUp() {
        controller = new RagQaController(ragQaService, userDirectory);
    }

    @Test
    @DisplayName("로그인한 관리자 id로 질문을 던지고 응답을 반환한다")
    void asksWithCurrentAdminId() {
        UUID id = UUID.randomUUID();
        OffsetDateTime requestedAt = OffsetDateTime.parse("2026-08-20T10:00:00+09:00");
        when(authentication.getName()).thenReturn("admin@prodio.com");
        when(userDirectory.findActiveByEmail("admin@prodio.com"))
                .thenReturn(Optional.of(new UserRef(42L, "admin@prodio.com", "관리자")));
        AiQueryLog log = new AiQueryLog(id, QueryType.RAG_QA, SourceType.ORDER_NOTE, 42L,
                "납기 언제야?", "10월 15일입니다.", requestedAt);
        when(ragQaService.ask(42L, "납기 언제야?")).thenReturn(log);

        ApiResponse<RagQaController.AskResponse> response =
                controller.ask(new RagQaController.AskRequest("납기 언제야?"), authentication);

        assertThat(response.data()).isEqualTo(
                new RagQaController.AskResponse(id, SourceType.ORDER_NOTE, "납기 언제야?", "10월 15일입니다.", requestedAt));
    }

    @Test
    @DisplayName("세션 이메일로 활성 관리자를 찾지 못하면 예외를 던지고 질의하지 않는다")
    void unknownRequesterThrows() {
        when(authentication.getName()).thenReturn("ghost@prodio.com");
        when(userDirectory.findActiveByEmail("ghost@prodio.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.ask(new RagQaController.AskRequest("질문"), authentication))
                .isInstanceOfSatisfying(StatException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(StatErrorCode.STAT_REQUESTER_NOT_FOUND));
    }

    @Test
    @DisplayName("page/size와 로그인한 관리자 id를 그대로 전달해 질의응답 로그 페이지를 조회한다")
    void askLogsDelegatesToService() {
        UUID id = UUID.randomUUID();
        OffsetDateTime requestedAt = OffsetDateTime.parse("2026-08-20T10:00:00+09:00");
        when(authentication.getName()).thenReturn("admin@prodio.com");
        when(userDirectory.findActiveByEmail("admin@prodio.com"))
                .thenReturn(Optional.of(new UserRef(42L, "admin@prodio.com", "관리자")));
        AiQueryLog log = new AiQueryLog(id, QueryType.RAG_QA, null, 42L, "질문", "응답", requestedAt);
        when(ragQaService.getAskLogs(42L, 1, 20))
                .thenReturn(new AiQueryLogPage(List.of(log), 1, 20, 21));

        ApiResponse<RagQaController.AskLogPageResponse> response = controller.askLogs(1, 20, authentication);

        assertThat(response.data()).isEqualTo(new RagQaController.AskLogPageResponse(
                List.of(new RagQaController.AskResponse(id, null, "질문", "응답", requestedAt)),
                1, 20, 21));
    }
}

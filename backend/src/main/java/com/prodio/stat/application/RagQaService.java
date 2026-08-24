package com.prodio.stat.application;

import com.prodio.infra.exception.InfraErrorCode;
import com.prodio.infra.exception.InfraException;
import com.prodio.stat.domain.AiQueryLog;
import com.prodio.stat.domain.CancelledOrderDetail;
import com.prodio.stat.domain.DashboardSummary;
import com.prodio.stat.domain.OrderStatView;
import com.prodio.stat.domain.OrderViewStatus;
import com.prodio.stat.domain.ProductDistribution;
import com.prodio.stat.domain.QueryType;
import com.prodio.stat.domain.SourceType;
import com.prodio.stat.domain.StatFilter;
import com.prodio.stat.embedding.application.ClientEmbeddingRepository;
import com.prodio.stat.embedding.application.EmbeddingMatch;
import com.prodio.stat.embedding.application.EmbeddingRepository;
import com.prodio.stat.embedding.application.OrderEmbeddingRepository;
import com.prodio.stat.embedding.application.ProductionEmbeddingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 사용자 질문에 대해 searchNotes/queryOrderStats 중 필요한 도구를 Gemini가 골라 쓰도록 오케스트레이션하고,
 * 결과를 AiQueryLog로 남긴다. 두 도구의 순수 로직은 SearchNotesSupport/QueryOrderStatsSupport에,
 * 실제 조회(I/O)는 이 클래스가 리포지토리를 직접 호출해 수행한다(다른 @Service를 거치지 않는다).
 */
@Service
@RequiredArgsConstructor
public class RagQaService {

    private static final String SEARCH_NOTES = "searchNotes";
    private static final String QUERY_ORDER_STATS = "queryOrderStats";

    private final AiClient aiClient;
    private final OrderEmbeddingRepository orderEmbeddingRepository;
    private final ClientEmbeddingRepository clientEmbeddingRepository;
    private final ProductionEmbeddingRepository productionEmbeddingRepository;
    private final StatDashboardRepository statDashboardRepository;
    private final OrderStatViewRepository orderStatViewRepository;
    private final AiQueryLogRepository aiQueryLogRepository;

    public AiQueryLog ask(long adminId, String question) {
        Set<SourceType> usedSourceTypes = EnumSet.noneOf(SourceType.class);

        String response = aiClient.ask(question, tools(), toolCall -> execute(toolCall, usedSourceTypes));

        AiQueryLog log = AiQueryLog.ragQa(adminId, resolveSourceType(usedSourceTypes), question, response);
        return aiQueryLogRepository.save(log);
    }

    /**
     * 함수 호출 라우팅만 평가하기 위한 dry-run. Gemini가 어떤 tool을 어떤 인자로 부르는지만 기록하고,
     * 실제 검색/조회(DB, 임베딩)는 전혀 실행하지 않는다 — 그래서 검색 품질과 무관하게 라우팅 정확도만
     * 순수하게 잰다. AiQueryLog에도 남기지 않는다(실사용 질의가 아니라 평가용 호출이므로).
     */
    public List<ToolCall> route(String question) {
        List<ToolCall> recordedCalls = new ArrayList<>();
        aiClient.ask(question, tools(), toolCall -> {
            recordedCalls.add(toolCall);
            return "[평가용 응답] 요청하신 내용을 확인했습니다.";
        });
        return recordedCalls;
    }

    public AiQueryLogPage getAskLogs(long adminId, int page, int size) {
        return aiQueryLogRepository.findPage(adminId, QueryType.RAG_QA, page, size);
    }

    private String execute(ToolCall call, Set<SourceType> usedSourceTypes) {
        if (call.name() == null) {
            throw new InfraException(InfraErrorCode.AI_REQUEST_FAILED);
        }

        return switch (call.name()) {
            case SEARCH_NOTES -> {
                SourceType sourceType = parseSourceType(call.args().get("sourceType"));
                usedSourceTypes.add(sourceType);
                yield searchNotes(call.args().get("query"), sourceType);
            }
            case QUERY_ORDER_STATS -> queryOrderStats(
                    call.args().get("from"), call.args().get("to"), call.args().get("status"));
            default -> throw new InfraException(InfraErrorCode.AI_REQUEST_FAILED);
        };
    }

    private String searchNotes(String query, SourceType sourceType) {
        float[] queryVector = aiClient.embed(query);

        Map<SourceType, List<EmbeddingMatch>> matchesByType = new EnumMap<>(SourceType.class);
        for (SourceType target : SearchNotesSupport.targetsFor(sourceType)) {
            matchesByType.put(target, repositoryFor(target).search(queryVector, SearchNotesSupport.TOP_K));
        }

        List<SearchNotesSupport.LabeledMatch> matches =
                SearchNotesSupport.mergeTopK(matchesByType, SearchNotesSupport.TOP_K);
        return SearchNotesSupport.format(matches, currentStatusByOrderId(matches));
    }

    /**
     * ORDER_NOTE/PRODUCTION_MEMO 매치의 refId(=orderId)로 현재 주문 상태를 조회한다.
     * 임베딩 텍스트엔 노트/메모를 남긴 시점의 문맥만 남아있어 이후 상태 변화(배송 시작, 취소 등)를
     * 반영 못 할 수 있으니, 답변 시점에 최신 상태를 매번 새로 붙여 stale한 텍스트만으로 잘못 판단하지
     * 않게 한다. 재임베딩 이벤트를 늘리는 대신 답변 시점 조회 쪽을 택했다 — 상태가 바뀔 때마다 다시
     * 임베딩(=Gemini 호출)하지 않아도 항상 최신값을 보장할 수 있다. CLIENT_MEMO(refId=clientId)는 대상 아님.
     */
    private Map<Long, OrderViewStatus> currentStatusByOrderId(List<SearchNotesSupport.LabeledMatch> matches) {
        Map<Long, OrderViewStatus> statusByOrderId = new HashMap<>();
        for (SearchNotesSupport.LabeledMatch labeled : matches) {
            if (labeled.sourceType() == SourceType.CLIENT_MEMO) {
                continue;
            }
            long orderId = labeled.match().refId();
            statusByOrderId.computeIfAbsent(orderId, id -> orderStatViewRepository.findAllByOrderId(id).stream()
                    .findFirst().map(OrderStatView::status).orElse(null));
        }
        return statusByOrderId;
    }

    private EmbeddingRepository repositoryFor(SourceType sourceType) {
        return switch (sourceType) {
            case ORDER_NOTE -> orderEmbeddingRepository;
            case CLIENT_MEMO -> clientEmbeddingRepository;
            case PRODUCTION_MEMO -> productionEmbeddingRepository;
            case ALL -> throw new IllegalArgumentException("ALL은 개별 검색 대상이 아닙니다.");
        };
    }

    private String queryOrderStats(String from, String to, String status) {
        StatFilter filter = new StatFilter(
                QueryOrderStatsSupport.parseDate(from),
                QueryOrderStatsSupport.parseDate(to),
                QueryOrderStatsSupport.parseStatus(status));
        QueryOrderStatsSupport.validate(filter);

        DashboardSummary summary = statDashboardRepository.summarize(filter);
        List<ProductDistribution> distribution = statDashboardRepository.productDistribution(filter);
        List<CancelledOrderDetail> cancelledDetails = statDashboardRepository.cancelledOrderDetails(filter);

        return QueryOrderStatsSupport.format(filter, summary, distribution, cancelledDetails);
    }

    private SourceType parseSourceType(String value) {
        if (value == null || value.isBlank()) {
            return SourceType.ALL;
        }
        try {
            return SourceType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            return SourceType.ALL;
        }
    }

    /** searchNotes가 한 번도 안 쓰였으면 null, 한 종류만 쓰였으면 그 값, 여러 종류가 쓰였으면 ALL로 기록한다. */
    private SourceType resolveSourceType(Set<SourceType> usedSourceTypes) {
        if (usedSourceTypes.isEmpty()) {
            return null;
        }
        return usedSourceTypes.size() == 1 ? usedSourceTypes.iterator().next() : SourceType.ALL;
    }

    private List<ToolSpec> tools() {
        return List.of(
                new ToolSpec(SEARCH_NOTES,
                        "주문 노트, 고객 메모, 생산 메모 등 비정형 텍스트에서 질문과 의미적으로 관련된 내용을 검색한다. "
                                + "과거 기록, 특이사항, 문의 내용 등 정형 통계로 답할 수 없는 질문에 사용한다. "
                                + "ORDER_NOTE/PRODUCTION_MEMO 결과는 '(현재 주문 상태: ...)'를 함께 반환하는데, "
                                + "이건 노트를 남긴 시점이 아니라 지금 시점의 실제 상태다 — '아직 배송 안 된', "
                                + "'취소된 것 빼고'처럼 상태 조건이 섞인 질문은 텍스트 내용만으로 판단하지 말고 "
                                + "이 상태값을 기준으로 걸러라.",
                        List.of(
                                new ToolParam("query", "검색할 질문 또는 키워드", true),
                                new ToolParam("sourceType",
                                        "검색 대상: ORDER_NOTE(주문 노트), CLIENT_MEMO(고객 메모), "
                                                + "PRODUCTION_MEMO(생산 메모), ALL(전체) 중 하나. 생략하면 ALL로 검색한다. "
                                                + "질문에 등장하는 단어(예: '주문', '거래처')만으로 어떤 메모 종류인지 "
                                                + "단정하지 말 것 — 셋 중 어디에 관련 내용이 있을지 확실하지 않으면 "
                                                + "ALL을 사용한다. 좁혀서 검색했다가 실제로는 다른 종류에 있는 내용을 "
                                                + "놓치는 것이, ALL로 검색해 관련 없는 결과가 조금 섞이는 것보다 나쁘다.",
                                        false)
                        )),
                new ToolSpec(QUERY_ORDER_STATS,
                        "특정 기간/상태의 주문 건수, 생산량 등 정형 통계를 조회한다. 건수/생산량 등 숫자 질문에 사용한다. "
                                + "이 도구는 원화 매출액(금액)을 계산하지 않는다 — 건수와 생산 수량만 준다. "
                                + "status를 CANCELLED로 지정하면 해당 기간에 취소된 개별 주문의 취소 사유 목록도 "
                                + "함께 반환한다 — '이번 달 취소 사유 알려줘' 같은 질문은 searchNotes로 자유 검색하지 "
                                + "말고 이 도구를 status=CANCELLED, 정확한 기간과 함께 호출해라.",
                        List.of(
                                new ToolParam("from", "조회 시작일(YYYY-MM-DD). 생략 가능", false),
                                new ToolParam("to", "조회 종료일(YYYY-MM-DD). 생략 가능", false),
                                new ToolParam("status",
                                        "주문 상태 필터: PENDING/IN_PRODUCTION/IN_DELIVERY/COMPLETED/CANCELLED 중 하나. 생략 가능",
                                        false)
                        ))
        );
    }
}

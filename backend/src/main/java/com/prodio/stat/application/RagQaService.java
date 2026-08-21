package com.prodio.stat.application;

import com.prodio.infra.exception.InfraErrorCode;
import com.prodio.infra.exception.InfraException;
import com.prodio.stat.domain.AiQueryLog;
import com.prodio.stat.domain.DashboardSummary;
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

import java.util.EnumMap;
import java.util.EnumSet;
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
    private final AiQueryLogRepository aiQueryLogRepository;

    public AiQueryLog ask(long adminId, String question) {
        Set<SourceType> usedSourceTypes = EnumSet.noneOf(SourceType.class);

        String response = aiClient.ask(question, tools(), toolCall -> execute(toolCall, usedSourceTypes));

        AiQueryLog log = AiQueryLog.ragQa(adminId, resolveSourceType(usedSourceTypes), question, response);
        return aiQueryLogRepository.save(log);
    }

    public AiQueryLogPage getAskLogs(long adminId, int page, int size) {
        return aiQueryLogRepository.findPage(adminId, QueryType.RAG_QA, page, size);
    }

    private String execute(ToolCall call, Set<SourceType> usedSourceTypes) {
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

        return SearchNotesSupport.format(SearchNotesSupport.mergeTopK(matchesByType, SearchNotesSupport.TOP_K));
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

        return QueryOrderStatsSupport.format(filter, summary, distribution);
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
                                + "과거 기록, 특이사항, 문의 내용 등 정형 통계로 답할 수 없는 질문에 사용한다.",
                        List.of(
                                new ToolParam("query", "검색할 질문 또는 키워드", true),
                                new ToolParam("sourceType",
                                        "검색 대상: ORDER_NOTE(주문 노트), CLIENT_MEMO(고객 메모), "
                                                + "PRODUCTION_MEMO(생산 메모), ALL(전체) 중 하나. 생략하면 ALL로 검색한다.",
                                        false)
                        )),
                new ToolSpec(QUERY_ORDER_STATS,
                        "특정 기간/상태의 주문 건수, 생산량 등 정형 통계를 조회한다. 매출/건수/생산량 등 숫자 질문에 사용한다.",
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

package com.prodio.stat.application;

import com.prodio.stat.domain.OrderViewStatus;
import com.prodio.stat.domain.SourceType;
import com.prodio.stat.embedding.application.EmbeddingMatch;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/** searchNotes 도구의 순수 로직(대상 결정/병합/포맷)만 모아둔 정적 유틸. I/O(임베딩·검색)는 RagQaService가 직접 수행한다. */
final class SearchNotesSupport {

    static final int TOP_K = 5;

    private SearchNotesSupport() {}

    static List<SourceType> targetsFor(SourceType sourceType) {
        return sourceType == SourceType.ALL
                ? List.of(SourceType.ORDER_NOTE, SourceType.CLIENT_MEMO, SourceType.PRODUCTION_MEMO)
                : List.of(sourceType);
    }

    static List<LabeledMatch> mergeTopK(Map<SourceType, List<EmbeddingMatch>> matchesByType, int topK) {
        return matchesByType.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream()
                        .map(match -> new LabeledMatch(entry.getKey(), match)))
                .sorted(Comparator.comparingDouble(labeled -> labeled.match().distance()))
                .limit(topK)
                .toList();
    }

    /**
     * statusByOrderId는 ORDER_NOTE/PRODUCTION_MEMO의 refId(=orderId)를 현재 주문 상태로 매핑한 것.
     * 임베딩 텍스트 자체엔 그 순간의 상태만 남아있어(예: 취소 전 노트) 최신 상태와 어긋날 수 있으므로,
     * 답변 시점에 조회한 실제 상태를 매치마다 별도로 붙여준다 — CLIENT_MEMO(refId=clientId)는 대상 아님.
     */
    static String format(List<LabeledMatch> matches, Map<Long, OrderViewStatus> statusByOrderId) {
        if (matches.isEmpty()) {
            return "관련된 노트/메모를 찾지 못했습니다.";
        }

        StringBuilder result = new StringBuilder();
        for (LabeledMatch labeled : matches) {
            result.append("[").append(labeled.sourceType()).append(" #").append(labeled.match().refId()).append("]");
            OrderViewStatus status = statusByOrderId.get(labeled.match().refId());
            if (status != null) {
                result.append(" (현재 주문 상태: ").append(status).append(")");
            }
            result.append(" ").append(labeled.match().text()).append("\n");
        }

        return result.toString();
    }

    record LabeledMatch(SourceType sourceType, EmbeddingMatch match) {
    }
}

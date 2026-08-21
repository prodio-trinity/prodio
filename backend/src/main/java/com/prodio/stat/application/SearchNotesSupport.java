package com.prodio.stat.application;

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

    static String format(List<LabeledMatch> matches) {
        if (matches.isEmpty()) {
            return "관련된 노트/메모를 찾지 못했습니다.";
        }

        StringBuilder result = new StringBuilder();
        for (LabeledMatch labeled : matches) {
            result.append("[").append(labeled.sourceType()).append(" #").append(labeled.match().refId()).append("] ")
                    .append(labeled.match().text()).append("\n");
        }

        return result.toString();
    }

    record LabeledMatch(SourceType sourceType, EmbeddingMatch match) {
    }
}

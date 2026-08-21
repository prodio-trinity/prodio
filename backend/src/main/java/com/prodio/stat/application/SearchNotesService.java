package com.prodio.stat.application;

import com.prodio.stat.domain.SourceType;
import com.prodio.stat.embedding.application.ClientEmbeddingRepository;
import com.prodio.stat.embedding.application.EmbeddingMatch;
import com.prodio.stat.embedding.application.EmbeddingRepository;
import com.prodio.stat.embedding.application.OrderEmbeddingRepository;
import com.prodio.stat.embedding.application.ProductionEmbeddingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/**
 * 질문을 임베딩해 order/client/production 노트·메모 중 의미가 가까운 것을 찾아 텍스트로 정리한다.
 * RAG QA의 searchNotes 도구가 실행할 실제 검색 로직.
 */
@Service
@RequiredArgsConstructor
public class SearchNotesService {

    private static final int TOP_K = 5;

    private final AiClient aiClient;
    private final OrderEmbeddingRepository orderEmbeddingRepository;
    private final ClientEmbeddingRepository clientEmbeddingRepository;
    private final ProductionEmbeddingRepository productionEmbeddingRepository;

    public String searchNotes(String query, SourceType sourceType) {
        float[] queryVector = aiClient.embed(query);

        List<LabeledMatch> matches = targetsFor(sourceType).stream()
                .flatMap(type -> repositoryFor(type).search(queryVector, TOP_K).stream()
                        .map(match -> new LabeledMatch(type, match)))
                .sorted(Comparator.comparingDouble(labeled -> labeled.match().distance()))
                .limit(TOP_K)
                .toList();

        return format(matches);
    }

    private List<SourceType> targetsFor(SourceType sourceType) {
        return sourceType == SourceType.ALL
                ? List.of(SourceType.ORDER_NOTE, SourceType.CLIENT_MEMO, SourceType.PRODUCTION_MEMO)
                : List.of(sourceType);
    }

    private EmbeddingRepository repositoryFor(SourceType sourceType) {
        return switch (sourceType) {
            case ORDER_NOTE -> orderEmbeddingRepository;
            case CLIENT_MEMO -> clientEmbeddingRepository;
            case PRODUCTION_MEMO -> productionEmbeddingRepository;
            case ALL -> throw new IllegalArgumentException("ALL은 개별 검색 대상이 아닙니다.");
        };
    }

    private String format(List<LabeledMatch> matches) {
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

    private record LabeledMatch(SourceType sourceType, EmbeddingMatch match) {
    }
}

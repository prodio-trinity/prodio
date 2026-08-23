package com.prodio.stat.application;

import com.prodio.stat.domain.OrderViewStatus;
import com.prodio.stat.domain.SourceType;
import com.prodio.stat.embedding.application.EmbeddingMatch;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SearchNotesSupport")
class SearchNotesSupportTest {

    @Test
    @DisplayName("sourceType이 ALL이면 세 소스 전부를 대상으로 한다")
    void targetsForAllReturnsAllThreeSources() {
        assertThat(SearchNotesSupport.targetsFor(SourceType.ALL))
                .containsExactly(SourceType.ORDER_NOTE, SourceType.CLIENT_MEMO, SourceType.PRODUCTION_MEMO);
    }

    @Test
    @DisplayName("sourceType이 특정 값이면 그 하나만 대상으로 한다")
    void targetsForSpecificSourceReturnsOnlyThat() {
        assertThat(SearchNotesSupport.targetsFor(SourceType.CLIENT_MEMO))
                .containsExactly(SourceType.CLIENT_MEMO);
    }

    @Test
    @DisplayName("여러 소스의 매치를 거리순으로 병합해 top-K만 남긴다")
    void mergeTopKSortsByDistanceAndLimits() {
        Map<SourceType, List<EmbeddingMatch>> matchesByType = Map.of(
                SourceType.ORDER_NOTE, List.of(new EmbeddingMatch(1L, "order-a", 0.30)),
                SourceType.CLIENT_MEMO, List.of(new EmbeddingMatch(2L, "client-a", 0.10)),
                SourceType.PRODUCTION_MEMO, List.of(new EmbeddingMatch(3L, "production-a", 0.20))
        );

        List<SearchNotesSupport.LabeledMatch> merged = SearchNotesSupport.mergeTopK(matchesByType, 2);

        assertThat(merged).hasSize(2);
        assertThat(merged.get(0).sourceType()).isEqualTo(SourceType.CLIENT_MEMO);
        assertThat(merged.get(1).sourceType()).isEqualTo(SourceType.PRODUCTION_MEMO);
    }

    @Test
    @DisplayName("매치가 없으면 안내 문구를 반환한다")
    void formatReturnsFallbackMessageWhenEmpty() {
        assertThat(SearchNotesSupport.format(List.of(), Map.of())).isEqualTo("관련된 노트/메모를 찾지 못했습니다.");
    }

    @Test
    @DisplayName("매치를 [sourceType #refId] text 형태로 정리한다")
    void formatBuildsLabeledSnippets() {
        List<SearchNotesSupport.LabeledMatch> matches = List.of(
                new SearchNotesSupport.LabeledMatch(SourceType.ORDER_NOTE, new EmbeddingMatch(1L, "납기 요청", 0.1))
        );

        assertThat(SearchNotesSupport.format(matches, Map.of())).contains("[ORDER_NOTE #1] 납기 요청");
    }

    @Test
    @DisplayName("refId에 해당하는 현재 상태가 있으면 함께 표기한다")
    void formatIncludesCurrentStatusWhenKnown() {
        List<SearchNotesSupport.LabeledMatch> matches = List.of(
                new SearchNotesSupport.LabeledMatch(SourceType.ORDER_NOTE, new EmbeddingMatch(36L, "행사 준비로 촉박", 0.1))
        );

        String result = SearchNotesSupport.format(matches, Map.of(36L, OrderViewStatus.IN_DELIVERY));

        assertThat(result).contains("[ORDER_NOTE #36] (현재 주문 상태: IN_DELIVERY) 행사 준비로 촉박");
    }

    @Test
    @DisplayName("상태를 모르면 상태 표기 없이 텍스트만 남긴다")
    void formatOmitsStatusWhenUnknown() {
        List<SearchNotesSupport.LabeledMatch> matches = List.of(
                new SearchNotesSupport.LabeledMatch(SourceType.CLIENT_MEMO, new EmbeddingMatch(2L, "우수 거래처", 0.1))
        );

        String result = SearchNotesSupport.format(matches, Map.of());

        assertThat(result).contains("[CLIENT_MEMO #2] 우수 거래처").doesNotContain("현재 주문 상태");
    }
}

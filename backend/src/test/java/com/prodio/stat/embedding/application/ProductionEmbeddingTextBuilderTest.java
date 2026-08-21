package com.prodio.stat.embedding.application;

import com.prodio.stat.domain.OrderStatView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ProductionEmbeddingTextBuilder")
class ProductionEmbeddingTextBuilderTest {

    @Test
    @DisplayName("OrderStatView에서 품목/거래처명을 조회해 memo와 조합한다")
    void fromCombinesContextFromOrderStatView() {
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-08-18T10:00:00+09:00");
        OrderStatView shaft = OrderStatView.create(1L, 2L, "○○상사", 3L, "정밀 샤프트", 3, 25_500L, createdAt);
        OrderStatView bearing = OrderStatView.create(1L, 2L, "○○상사", 4L, "베어링", 5, 25_000L, createdAt);

        String result = ProductionEmbeddingTextBuilder.from(1L, List.of(shaft, bearing), "생산 과정에서 도색 이슈 발생했습니다.");

        assertThat(result).isEqualTo("[정밀 샤프트 3개, 베어링 5개, ○○상사 주문] 생산 과정에서 도색 이슈 발생했습니다.");
    }

    @Test
    @DisplayName("OrderStatView가 비어 있으면 예외를 던진다")
    void throwsWhenViewsEmpty() {
        assertThatThrownBy(() -> ProductionEmbeddingTextBuilder.from(1L, List.of(), "도색 이슈 발생했습니다."))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("1");
    }
}

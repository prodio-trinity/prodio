package com.prodio.stat.embedding.application;

import com.prodio.catalog.ClientMemoEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ClientEmbeddingTextBuilder")
class ClientEmbeddingTextBuilderTest {

    @Test
    @DisplayName("거래처명과 memo를 조합한다")
    void fromCombinesCompanyNameAndMemo() {
        ClientMemoEvent event = new ClientMemoEvent(1L, "○○상사", "결제 조건이 까다로워 매번 확인이 필요합니다.");

        String result = ClientEmbeddingTextBuilder.from(event);

        assertThat(result).isEqualTo("[○○상사] 결제 조건이 까다로워 매번 확인이 필요합니다.");
    }
}

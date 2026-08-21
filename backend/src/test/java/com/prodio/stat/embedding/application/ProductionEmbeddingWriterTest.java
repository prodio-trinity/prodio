package com.prodio.stat.embedding.application;

import com.prodio.stat.application.AiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductionEmbeddingWriter")
class ProductionEmbeddingWriterTest {
    private static final long ORDER_ID = 1L;
    private static final float[] EMBEDDING = new float[] {0.1f, 0.2f};

    @Mock private ProductionEmbeddingRepository productionEmbeddingRepository;
    @Mock private AiClient aiClient;
    private ProductionEmbeddingWriter writer;

    @BeforeEach
    void setUp() {
        writer = new ProductionEmbeddingWriter(productionEmbeddingRepository, aiClient);
    }

    @Test
    @DisplayName("텍스트를 임베딩해 저장소에 upsert한다")
    void upsertEmbedsThenSaves() {
        String text = "[정밀 샤프트 3개, ○○상사 주문] 생산 과정에서 도색 이슈 발생했습니다.";
        when(aiClient.embed(text)).thenReturn(EMBEDDING);

        writer.upsert(ORDER_ID, text);

        verify(aiClient).embed(text);
        verify(productionEmbeddingRepository).upsert(ORDER_ID, text, EMBEDDING);
    }
}

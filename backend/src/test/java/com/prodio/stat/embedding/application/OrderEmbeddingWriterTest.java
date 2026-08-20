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
@DisplayName("OrderEmbeddingWriter")
class OrderEmbeddingWriterTest {
    private static final long ORDER_ID = 1L;
    private static final float[] EMBEDDING = new float[] {0.1f, 0.2f};

    @Mock private OrderEmbeddingRepository orderEmbeddingRepository;
    @Mock private AiClient aiClient;
    private OrderEmbeddingWriter writer;

    @BeforeEach
    void setUp() {
        writer = new OrderEmbeddingWriter(orderEmbeddingRepository, aiClient);
    }

    @Test
    @DisplayName("텍스트를 임베딩해 저장소에 upsert한다")
    void upsertEmbedsThenSaves() {
        String text = "[정밀 샤프트 3개, ○○상사 주문] 급하게 부탁드려요";
        when(aiClient.embed(text)).thenReturn(EMBEDDING);

        writer.upsert(ORDER_ID, text);

        verify(aiClient).embed(text);
        verify(orderEmbeddingRepository).upsert(ORDER_ID, text, EMBEDDING);
    }
}

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
@DisplayName("ClientEmbeddingWriter")
class ClientEmbeddingWriterTest {
    private static final long CLIENT_ID = 1L;
    private static final float[] EMBEDDING = new float[] {0.1f, 0.2f};

    @Mock private ClientEmbeddingRepository clientEmbeddingRepository;
    @Mock private AiClient aiClient;
    private ClientEmbeddingWriter writer;

    @BeforeEach
    void setUp() {
        writer = new ClientEmbeddingWriter(clientEmbeddingRepository, aiClient);
    }

    @Test
    @DisplayName("텍스트를 임베딩해 저장소에 upsert한다")
    void upsertEmbedsThenSaves() {
        String text = "[○○상사] 결제 조건이 까다로워 매번 확인이 필요합니다.";
        when(aiClient.embed(text)).thenReturn(EMBEDDING);

        writer.upsert(CLIENT_ID, text);

        verify(aiClient).embed(text);
        verify(clientEmbeddingRepository).upsert(CLIENT_ID, text, EMBEDDING);
    }
}

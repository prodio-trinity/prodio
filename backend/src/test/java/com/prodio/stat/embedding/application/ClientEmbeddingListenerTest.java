package com.prodio.stat.embedding.application;

import com.prodio.catalog.ClientMemoEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClientEmbeddingListener")
class ClientEmbeddingListenerTest {
    private static final long CLIENT_ID = 1L;

    @Mock private ClientEmbeddingRepository clientEmbeddingRepository;
    @Mock private ClientEmbeddingWriter clientEmbeddingWriter;
    private ClientEmbeddingListener listener;

    @BeforeEach
    void setUp() {
        listener = new ClientEmbeddingListener(clientEmbeddingRepository, clientEmbeddingWriter);
    }

    @Test
    @DisplayName("memo가 있으면 조합한 텍스트를 writer에 넘긴다")
    void memoPresentDelegatesToWriter() {
        when(clientEmbeddingRepository.findText(CLIENT_ID)).thenReturn(Optional.empty());

        listener.handle(new ClientMemoEvent(CLIENT_ID, "○○상사", "결제 조건이 까다로워 매번 확인이 필요합니다."));

        verify(clientEmbeddingWriter).upsert(CLIENT_ID, "[○○상사] 결제 조건이 까다로워 매번 확인이 필요합니다.");
    }

    @Test
    @DisplayName("memo가 비어 있으면 스킵한다")
    void memoBlankSkips() {
        listener.handle(new ClientMemoEvent(CLIENT_ID, "○○상사", ""));

        verifyNoInteractions(clientEmbeddingRepository, clientEmbeddingWriter);
    }

    @Test
    @DisplayName("조합한 텍스트가 기존 저장값과 같으면 재임베딩을 스킵한다")
    void skipsWhenTextUnchanged() {
        when(clientEmbeddingRepository.findText(CLIENT_ID))
                .thenReturn(Optional.of("[○○상사] 결제 조건이 까다로워 매번 확인이 필요합니다."));

        listener.handle(new ClientMemoEvent(CLIENT_ID, "○○상사", "결제 조건이 까다로워 매번 확인이 필요합니다."));

        verifyNoInteractions(clientEmbeddingWriter);
    }
}

package com.prodio.stat.embedding.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.function.Supplier;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("MeaningfulTextEmbedder")
class MeaningfulTextEmbedderTest {
    private static final long REF_ID = 1L;

    @Mock private EmbeddingRepository repository;
    @Mock private AbstractEmbeddingWriter writer;
    @Mock private Supplier<String> textSupplier;

    @Test
    @DisplayName("rawText가 비어 있으면 아무것도 조회/저장하지 않는다")
    void skipsWhenRawTextBlank() {
        MeaningfulTextEmbedder.upsertIfMeaningful(repository, writer, REF_ID, "", textSupplier);
        MeaningfulTextEmbedder.upsertIfMeaningful(repository, writer, REF_ID, null, textSupplier);

        verifyNoInteractions(repository, writer, textSupplier);
    }

    @Test
    @DisplayName("조합한 텍스트가 기존 저장값과 같으면 writer를 호출하지 않는다")
    void skipsWhenTextUnchanged() {
        when(textSupplier.get()).thenReturn("변하지 않은 텍스트");
        when(repository.findText(REF_ID)).thenReturn(Optional.of("변하지 않은 텍스트"));

        MeaningfulTextEmbedder.upsertIfMeaningful(repository, writer, REF_ID, "메모", textSupplier);

        verify(writer, never()).upsert(REF_ID, "변하지 않은 텍스트");
    }

    @Test
    @DisplayName("조합한 텍스트가 기존과 다르면 writer.upsert를 호출한다")
    void delegatesToWriterWhenTextChanged() {
        when(textSupplier.get()).thenReturn("새 텍스트");
        when(repository.findText(REF_ID)).thenReturn(Optional.of("이전 텍스트"));

        MeaningfulTextEmbedder.upsertIfMeaningful(repository, writer, REF_ID, "메모", textSupplier);

        verify(writer).upsert(REF_ID, "새 텍스트");
    }
}

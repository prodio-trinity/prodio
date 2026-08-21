package com.prodio.stat.embedding.infrastructure.persistence;

import com.prodio.stat.embedding.application.ClientEmbeddingRepository;
import com.prodio.stat.embedding.application.EmbeddingMatch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
class JpaClientEmbeddingRepository implements ClientEmbeddingRepository {

    private static final String TABLE = "statistics_client_embeddings";
    private static final String REF_COLUMN = "client_id";
    private static final String TEXT_COLUMN = "memo_text";

    private final VectorEmbeddingStore store;

    @Override
    public Optional<String> findText(long clientId) {
        return store.findText(TABLE, REF_COLUMN, TEXT_COLUMN, clientId);
    }

    @Override
    public void upsert(long clientId, String memoText, float[] embedding) {
        store.upsert(TABLE, REF_COLUMN, TEXT_COLUMN, clientId, memoText, embedding);
    }

    @Override
    public List<EmbeddingMatch> search(float[] queryVector, int topK) {
        return store.search(TABLE, REF_COLUMN, TEXT_COLUMN, queryVector, topK);
    }
}

package com.prodio.stat.embedding.infrastructure.persistence;

import com.prodio.stat.embedding.application.OrderEmbeddingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
class JpaOrderEmbeddingRepository implements OrderEmbeddingRepository {

    private static final String TABLE = "statistics_order_embeddings";
    private static final String REF_COLUMN = "order_id";
    private static final String TEXT_COLUMN = "note_text";

    private final VectorEmbeddingStore store;

    @Override
    public Optional<String> findText(long orderId) {
        return store.findText(TABLE, REF_COLUMN, TEXT_COLUMN, orderId);
    }

    @Override
    public void upsert(long orderId, String noteText, float[] embedding) {
        store.upsert(TABLE, REF_COLUMN, TEXT_COLUMN, orderId, noteText, embedding);
    }
}

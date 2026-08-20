package com.prodio.stat.embedding.application;

import java.util.Optional;

public interface OrderEmbeddingRepository {
    Optional<String> findNoteText(long orderId);
    void upsert(long orderId, String noteText, float[] embedding);
}

package com.prodio.stat.embedding.application;

import java.util.Optional;

public interface EmbeddingRepository {
    Optional<String> findText(long refId);
    void upsert(long refId, String text, float[] embedding);
}

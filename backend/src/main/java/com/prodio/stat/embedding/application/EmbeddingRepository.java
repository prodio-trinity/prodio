package com.prodio.stat.embedding.application;

import java.util.List;
import java.util.Optional;

public interface EmbeddingRepository {
    Optional<String> findText(long refId);
    void upsert(long refId, String text, float[] embedding);

    /** queryVector와 코사인 거리가 가까운 순으로 상위 topK건을 반환한다. */
    List<EmbeddingMatch> search(float[] queryVector, int topK);
}

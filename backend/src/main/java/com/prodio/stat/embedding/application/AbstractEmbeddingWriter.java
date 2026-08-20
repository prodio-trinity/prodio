package com.prodio.stat.embedding.application;

import com.prodio.stat.application.AiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * embed 호출은 NOT_SUPPORTED로 분리해 트랜잭션과 얽히지 않게 하고,
 * 실제 upsert만 레포지토리 쪽의 짧은 트랜잭션으로 처리한다.
 */
@RequiredArgsConstructor
abstract class AbstractEmbeddingWriter {

    private final EmbeddingRepository repository;
    private final AiClient aiClient;

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void upsert(long refId, String text) {
        float[] embedding = aiClient.embed(text);
        repository.upsert(refId, text, embedding);
    }
}

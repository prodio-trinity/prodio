package com.prodio.stat.embedding.application;

import com.prodio.stat.application.AiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * OrderEmbeddingListener의 핸들러는 @ApplicationModuleListener라 메서드 전체가
 * REQUIRES_NEW 트랜잭션으로 묶인다. 그 안에서 곧바로 Gemini를 호출하면(수 초 걸릴 수 있음)
 * 그 구간 내내 DB 트랜잭션을 붙들게 되므로, embed 호출은 NOT_SUPPORTED로 분리해 트랜잭션과
 * 얽히지 않게 하고, 실제 upsert만 별도 트랜잭션(JpaOrderEmbeddingRepository)으로 짧게 처리한다.
 */
@Component
@RequiredArgsConstructor
class OrderEmbeddingWriter {

    private final OrderEmbeddingRepository orderEmbeddingRepository;
    private final AiClient aiClient;

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void upsert(long orderId, String text) {
        float[] embedding = aiClient.embed(text);

        orderEmbeddingRepository.upsert(orderId, text, embedding);
    }
}

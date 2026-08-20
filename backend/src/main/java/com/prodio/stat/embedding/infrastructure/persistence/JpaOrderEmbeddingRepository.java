package com.prodio.stat.embedding.infrastructure.persistence;

import com.prodio.stat.embedding.application.OrderEmbeddingRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Hibernate엔 pgvector용 타입이 없어서, embedding 컬럼은 JPA 엔티티 매핑 없이
 * native query로만 다룬다. float[]는 "[0.1,0.2,...]" 형태의 pgvector 리터럴로 바꿔 전달한다.
 */
@Repository
@RequiredArgsConstructor
class JpaOrderEmbeddingRepository implements OrderEmbeddingRepository {
    private final EntityManager entityManager;

    @Override
    public Optional<String> findNoteText(long orderId) {
        @SuppressWarnings("unchecked")
        List<String> result = entityManager.createNativeQuery(
                "SELECT note_text FROM statistics_order_embeddings WHERE order_id = :orderId", String.class)
                .setParameter("orderId", orderId)
                .getResultList();
        return result.stream().findFirst();
    }

    @Override
    public void upsert(long orderId, String noteText, float[] embedding) {
        entityManager.createNativeQuery("""
                INSERT INTO statistics_order_embeddings (order_id, note_text, embedding)
                VALUES (:orderId, :noteText, CAST(:embedding AS vector))
                ON CONFLICT (order_id) DO UPDATE
                SET note_text = EXCLUDED.note_text, embedding = EXCLUDED.embedding
                """)
                .setParameter("orderId", orderId)
                .setParameter("noteText", noteText)
                .setParameter("embedding", toVectorLiteral(embedding))
                .executeUpdate();
    }

    private static String toVectorLiteral(float[] embedding) {
        StringBuilder literal = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            if (i > 0) literal.append(',');
            literal.append(embedding[i]);
        }
        return literal.append(']').toString();
    }
}

package com.prodio.stat.embedding.infrastructure.persistence;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * order/client/production 임베딩 테이블(statistics_*_embeddings)은 전부
 * (id, <ref>_id UNIQUE, <text>_text, embedding, created_at) 구조가 동일해서
 * pgvector native query 처리를 여기 한 곳에 모은다. Hibernate엔 pgvector용 타입이
 * 없어 JPA 엔티티 매핑 없이 native query로만 다루고, float[]는
 * "[0.1,0.2,...]" 형태의 pgvector 리터럴로 바꿔 전달한다.
 *
 * table/refColumn/textColumn은 호출부(각 도메인 레포지토리)가 하드코딩한 상수만 넘기고
 * 외부 입력이 SQL에 직접 섞이는 경로가 없어, 문자열 결합으로 쿼리를 구성해도 안전하다.
 */
@Component
@RequiredArgsConstructor
class VectorEmbeddingStore {

    private final EntityManager entityManager;

    Optional<String> findText(String table, String refColumn, String textColumn, long refId) {
        @SuppressWarnings("unchecked")
        List<String> result = entityManager.createNativeQuery(
                "SELECT " + textColumn + " FROM " + table + " WHERE " + refColumn + " = :refId", String.class)
                .setParameter("refId", refId)
                .getResultList();

        return result.stream().findFirst();
    }

    /** 호출부가 NOT_SUPPORTED로 트랜잭션을 비운 채 부르는 경우가 있어, upsert만의 짧은 트랜잭션을 새로 연다. */
    @Transactional
    void upsert(String table, String refColumn, String textColumn, long refId, String text, float[] embedding) {
        entityManager.createNativeQuery(
                "INSERT INTO " + table + " (" + refColumn + ", " + textColumn + ", embedding) "
                + "VALUES (:refId, :text, CAST(:embedding AS vector)) "
                + "ON CONFLICT (" + refColumn + ") DO UPDATE "
                + "SET " + textColumn + " = EXCLUDED." + textColumn + ", embedding = EXCLUDED.embedding")
                .setParameter("refId", refId)
                .setParameter("text", text)
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

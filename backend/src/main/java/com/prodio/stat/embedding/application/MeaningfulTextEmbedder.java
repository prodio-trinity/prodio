package com.prodio.stat.embedding.application;

import java.util.function.Supplier;

/**
 * order/client/production 리스너가 공통으로 쓰는 판단 로직
 *
 * rawText(note/memo)가 비어 있으면 임베딩할 자유 텍스트가 없어 스킵하고,
 * 조합한 텍스트가 기존 저장값과 같으면 재임베딩을 생략한다(불필요한 Gemini 호출 방지).
 */
final class MeaningfulTextEmbedder {

    private MeaningfulTextEmbedder() {}

    static void upsertIfMeaningful(EmbeddingRepository repository, AbstractEmbeddingWriter writer,
            long refId, String rawText, Supplier<String> textSupplier) {
        if (rawText == null || rawText.isBlank()) {
            return;
        }

        String text = textSupplier.get();
        if (repository.findText(refId).map(text::equals).orElse(false)) {
            return;
        }

        writer.upsert(refId, text);
    }
}

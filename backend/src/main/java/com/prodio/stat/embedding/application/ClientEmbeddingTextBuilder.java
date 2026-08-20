package com.prodio.stat.embedding.application;

import com.prodio.catalog.ClientMemoEvent;

/**
 * 거래처 메모는 companyName + memo
 */
final class ClientEmbeddingTextBuilder {

    private ClientEmbeddingTextBuilder() {}

    static String from(ClientMemoEvent event) {
        return "[" + event.companyName() + "] " + event.memo();
    }
}

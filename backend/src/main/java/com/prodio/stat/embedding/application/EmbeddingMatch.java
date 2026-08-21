package com.prodio.stat.embedding.application;

/** 코사인 유사도 검색 결과 한 건. distance는 낮을수록 유사하다. */
public record EmbeddingMatch(long refId, String text, double distance) {
}

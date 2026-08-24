package com.prodio.stat.application;

/** searchNotes 검색 품질 평가(search-eval)용 dry-run 결과 한 건. distance는 낮을수록 유사하다. */
public record SearchMatch(String sourceType, long refId, double distance) {
}

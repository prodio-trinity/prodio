package com.prodio.shared;

import java.util.List;
import java.util.function.Function;

/** 목록 조회 결과 공통 포장지. records 안의 타입만 바뀌고 나머지(page/size/totalElements/totalPages)는
 * 항상 같은 모양이라, 도메인용/응답(DTO)용 어디서든 이거 하나로 재사용한다. */
public record PageResult<T>(List<T> records, int page, int size, long totalElements, int totalPages) {
    public <R> PageResult<R> map(Function<T, R> mapper) {
        return new PageResult<>(records.stream().map(mapper).toList(), page, size, totalElements, totalPages);
    }
}

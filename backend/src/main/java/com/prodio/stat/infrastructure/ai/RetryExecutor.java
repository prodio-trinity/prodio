package com.prodio.stat.infrastructure.ai;

import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * 지수 백오프 기반 재시도 유틸.
 *
 * 예) maxRetries=3, initialBackoffMillis=500이면 최대 4번 시도한다.
 *   1번째 시도 실패 → 500ms 대기
 *   2번째 시도 실패 → 1000ms 대기
 *   3번째 시도 실패 → 2000ms 대기
 *   4번째 시도 실패 → 더 재시도하지 않고 그 예외를 그대로 던진다
 */
class RetryExecutor {
    private final int maxRetries;
    private final long initialBackoffMillis;

    RetryExecutor(int maxRetries, long initialBackoffMillis) {
        this.maxRetries = maxRetries;
        this.initialBackoffMillis = initialBackoffMillis;
    }

    <T> T execute(Supplier<T> action, Predicate<RuntimeException> retryable) {
        for (int attempt = 0; ; attempt++) {
            try {
                return action.get();
            } catch (RuntimeException exception) {
                if (!retryable.test(exception) || attempt >= maxRetries) throw exception;
                sleep(initialBackoffMillis * (1L << attempt));
            }
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("재시도 대기 중 인터럽트가 발생했습니다.", exception);
        }
    }
}

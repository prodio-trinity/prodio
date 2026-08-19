package com.prodio.stat.infrastructure.ai;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("RetryExecutor")
class RetryExecutorTest {

    @Test
    @DisplayName("재시도 대상 예외가 나면 성공할 때까지 다시 시도한다")
    void retriesUntilSuccess() {
        RetryExecutor executor = new RetryExecutor(3, 1);
        AtomicInteger attempts = new AtomicInteger();

        String result = executor.execute(() -> {
            if (attempts.incrementAndGet() < 3) throw new IllegalStateException("일시적 오류");
            return "성공";
        }, exception -> true);

        assertThat(result).isEqualTo("성공");
        assertThat(attempts.get()).isEqualTo(3);
    }

    @Test
    @DisplayName("최대 재시도 횟수를 넘기면 마지막 예외를 그대로 던진다")
    void throwsAfterExceedingMaxRetries() {
        RetryExecutor executor = new RetryExecutor(2, 1);
        AtomicInteger attempts = new AtomicInteger();

        assertThatThrownBy(() -> executor.execute(() -> {
            attempts.incrementAndGet();
            throw new IllegalStateException("계속 실패");
        }, exception -> true))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("계속 실패");

        assertThat(attempts.get()).isEqualTo(3); // 최초 시도 1 + 재시도 2
    }

    @Test
    @DisplayName("재시도 대상이 아닌 예외는 즉시 던지고 재시도하지 않는다")
    void doesNotRetryWhenNotRetryable() {
        RetryExecutor executor = new RetryExecutor(3, 1);
        AtomicInteger attempts = new AtomicInteger();

        assertThatThrownBy(() -> executor.execute(() -> {
            attempts.incrementAndGet();
            throw new IllegalArgumentException("재시도 대상 아님");
        }, exception -> false))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(attempts.get()).isEqualTo(1);
    }
}

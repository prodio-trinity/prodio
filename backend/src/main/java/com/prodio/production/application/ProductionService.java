package com.prodio.production.application;

import com.prodio.production.domain.ProductionRecord;
import com.prodio.production.event.OrderCompleted;
import com.prodio.production.event.OrderShipped;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductionService {
    private final ProductionRepository repository;
    private final ApplicationEventPublisher eventPublisher;
    private final SmsSender smsSender;

    @Transactional
    public boolean ship(Long productionId) {
        ProductionRecord shipped = repository.markShipped(productionId);
        boolean smsSent = notify(shipped, "배송이 시작되었습니다");
        eventPublisher.publishEvent(
                new OrderShipped(shipped.orderId(), toOffsetDateTime(shipped.shippedAt()))
        );
        return smsSent;
    }

    @Transactional
    public boolean complete(Long productionId) {
        ProductionRecord completed = repository.markCompleted(productionId);
        boolean smsSent = notify(completed, "배송이 완료되었습니다");
        eventPublisher.publishEvent(
                new OrderCompleted(completed.orderId(), toOffsetDateTime(completed.completedAt()))
        );
        return smsSent;
    }

    /** SMS 발송을 시도하고 성공 여부를 리턴한다. 실패해도 예외를 던지지 않고 로그만 남긴다. */
    private boolean notify(ProductionRecord record, String action) {
        try {
            smsSender.send(record.phone(), "[prodio] %s (주문번호: %d)".formatted(action, record.orderId()));
            return true;
        } catch (Exception e) {
            log.error("{} SMS 발송 실패. orderId={}", action, record.orderId(), e);
            return false;
        }
    }

    private OffsetDateTime toOffsetDateTime(LocalDateTime time) {
        return time.atZone(ZoneId.systemDefault()).toOffsetDateTime();
    }
}

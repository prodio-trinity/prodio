package com.prodio.production.application;

import com.prodio.production.event.OrderShipped;
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
        ShipInfo shipInfo = repository.updateShipInfo(productionId);

        boolean smsSent = true;
        try {
            smsSender.send(shipInfo.phone(), "[prodio] 배송이 시작되었습니다 (주문번호: %d)".formatted(shipInfo.orderId()));
        } catch (Exception e) {
            smsSent = false;
            log.error("배송 시작 SMS 발송 실패. productionId={}, orderId={}", productionId, shipInfo.orderId(), e);
        }

        eventPublisher.publishEvent(
                new OrderShipped(shipInfo.orderId(), shipInfo.shippedAt().atZone(ZoneId.systemDefault()).toOffsetDateTime())
        );

        return smsSent;
    }
}

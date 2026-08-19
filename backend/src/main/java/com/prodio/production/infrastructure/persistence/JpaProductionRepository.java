package com.prodio.production.infrastructure.persistence;

import com.prodio.production.application.ProductionRepository;
import com.prodio.production.application.ShipInfo;
import com.prodio.production.domain.ProductionRecord;
import com.prodio.production.exception.ProductionErrorCode;
import com.prodio.production.exception.ProductionException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JpaProductionRepository implements ProductionRepository {
    private final SpringDataProductionRepository springDataProductionRepository;

    @Override
    public ProductionRecord save(ProductionRecord productionRecord) {
        ProductionRecordEntity entity = ProductionRecordEntity.from(productionRecord);
        ProductionRecordEntity saved = springDataProductionRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public boolean existsByOrderId(Long orderId) {
        return springDataProductionRepository.existsByOrderId(orderId);
    }

    @Override
    public ShipInfo updateShipInfo(Long productionId) {
        ProductionRecordEntity entity = springDataProductionRepository.findById(productionId)
                .orElseThrow(() -> new ProductionException(ProductionErrorCode.PRODUCTION_NOT_FOUND));

        ProductionRecord shipped = entity.toDomain().markShipped();
        springDataProductionRepository.save(ProductionRecordEntity.from(shipped));

        return new ShipInfo(shipped.orderId(), shipped.phone(), shipped.shippedAt());
    }
}

package com.prodio.production.infrastructure.persistence;

import com.prodio.production.application.ProductionRepository;
import com.prodio.production.domain.ProductionRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

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
}
